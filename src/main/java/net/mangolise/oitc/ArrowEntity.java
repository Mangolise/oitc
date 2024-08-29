package net.mangolise.oitc;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.SweepResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.projectile.ProjectileMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.thread.Acquirable;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArrowEntity extends Entity {

    private final Player shooter;
    private boolean wasStuck;
    private Particle particle;

    public ArrowEntity(@Nullable Player shooter) {
        super(EntityType.ARROW);
        this.shooter = shooter;
        setup();

        List<Particle> particles = List.copyOf(Particle.values());
        particle = particles.get(ThreadLocalRandom.current().nextInt(particles.size()));

        this.shooter.sendMessage("Particle: " + particle.name());
    }

    private void setup() {
        super.hasPhysics = false;
        if (getEntityMeta() instanceof ProjectileMeta) {
            ((ProjectileMeta) getEntityMeta()).setShooter(this.shooter);
        }
    }

    @Nullable
    public Entity getShooter() {
        return this.shooter;
    }

    public void shoot(Point to, double power, double spread) {
        final EntityShootEvent shootEvent = new EntityShootEvent(this.shooter, this, to, power, spread);
        EventDispatcher.call(shootEvent);
        if (shootEvent.isCancelled()) {
            remove();
            return;
        }
        final Pos from = this.shooter.getPosition().add(0D, this.shooter.getEyeHeight(), 0D);
        shoot(from, to, shootEvent.getPower(), shootEvent.getSpread());
    }

    private void shoot(@NotNull Point from, @NotNull Point to, double power, double spread) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();
        if (!hasNoGravity()) {
            final double xzLength = Math.sqrt(dx * dx + dz * dz);
            dy += xzLength * 0.20000000298023224D;
        }

        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= length;
        dy /= length;
        dz /= length;
        Random random = ThreadLocalRandom.current();
        spread *= 0.007499999832361937D;
        dx += random.nextGaussian() * spread;
        dy += random.nextGaussian() * spread;
        dz += random.nextGaussian() * spread;

        final double mul = 20 * power;
        this.velocity = new Vec(dx * mul, dy * mul, dz * mul);
        sendPacketToViewersAndSelf(getVelocityPacket());
        setView(
                (float) Math.toDegrees(Math.atan2(dx, dz)),
                (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)))
        );
    }

    @Override
    public void tick(long time) {
        final Pos posBefore = getPosition();
        super.tick(time);
        if (super.isRemoved()) return;

        final Pos posNow = getPosition();

        double step = 0.1;

        double travelled = 0;
        double total = posNow.distance(posBefore);

        while (travelled < total) {
            Pos pos = posBefore.add(posNow.sub(posBefore).asVec().normalize().mul(travelled));
            ParticlePacket packet = new ParticlePacket(particle, pos, Vec.ZERO, 0, 1);
            PacketUtils.sendGroupedPacket(this.getViewers(), packet);
            travelled += step;
        }

        if (isStuck(posBefore, posNow)) {
            if (super.onGround) {
                return;
            }
            super.onGround = true;
            this.velocity = Vec.ZERO;
            sendPacketToViewersAndSelf(getVelocityPacket());
            setNoGravity(true);
            wasStuck = true;
        } else {
            if (!wasStuck) return;
            wasStuck = false;
            setNoGravity(super.onGround);
            super.onGround = false;
            EventDispatcher.call(new ProjectileUncollideEvent(this));
        }
    }

    /**
     * Checks whether an arrow is stuck in block / hit an entity.
     *
     * @param pos    position right before current tick.
     * @param posNow position after current tick.
     * @return if an arrow is stuck in block / hit an entity.
     */
    @SuppressWarnings("ConstantConditions")
    private boolean isStuck(Pos pos, Pos posNow) {
        final Instance instance = getInstance();
        if (pos.samePoint(posNow)) {
            return instance.getBlock(pos).isSolid();
        }

        Chunk chunk = null;
        Collection<LivingEntity> entities = null;
        final BoundingBox bb = getBoundingBox();

        /*
          What we're about to do is to discretely jump from a previous position to the new one.
          For each point we will be checking blocks and entities we're in.
         */
        final double part = bb.width() / 2;
        final Vec dir = posNow.sub(pos).asVec();
        final int parts = (int) Math.ceil(dir.length() / part);
        final Pos direction = dir.normalize().mul(part).asPosition();
        final long aliveTicks = getAliveTicks();
        Block block = null;
        Point blockPos = null;
        for (int i = 0; i < parts; ++i) {
            // If we're at last part, we can't just add another direction-vector, because we can exceed the end point.
            pos = (i == parts - 1) ? posNow : pos.add(direction);
            if (block == null || !pos.sameBlock(blockPos)) {
                block = instance.getBlock(pos);
                blockPos = pos;
            }
            if (block.isSolid()) {
                final ProjectileCollideWithBlockEvent event = new ProjectileCollideWithBlockEvent(this, pos, block);
                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    teleport(pos);
                    return true;
                }
            }
            if (currentChunk != chunk) {
                chunk = currentChunk;
                entities = instance.getChunkEntities(chunk)
                        .stream()
                        .filter(entity -> entity instanceof LivingEntity)
                        .map(entity -> (LivingEntity) entity)
                        .collect(Collectors.toSet());
            }
            final Point currentPos = pos;
            Stream<LivingEntity> victimsStream = entities.stream()
                    .filter(entity -> {
                        SweepResult result = new SweepResult(Double.MAX_VALUE, 0, 0, 0, null, 0, 0, 0);
                        return entity.getBoundingBox().intersectBoxSwept(currentPos, this.getVelocity(), entity.getPosition(), bb, result);
                    });
            /*
              We won't check collisions with a shooter for first ticks of arrow's life, because it spawns in him
              and will immediately deal damage.
             */
            if (aliveTicks < 3 && shooter != null) {
                victimsStream = victimsStream.filter(entity -> entity != shooter);
            }
            final Optional<LivingEntity> victimOptional = victimsStream.findAny();
            if (victimOptional.isPresent()) {
                final LivingEntity target = victimOptional.get();
                final ProjectileCollideWithEntityEvent event = new ProjectileCollideWithEntityEvent(this, pos, target);
                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    return super.onGround;
                }
            }
        }
        return false;
    }

    @ApiStatus.Experimental
    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Acquirable<? extends ArrowEntity> acquirable() {
        return (Acquirable<? extends ArrowEntity>) super.acquirable();
    }
}
