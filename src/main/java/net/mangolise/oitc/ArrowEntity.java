package net.mangolise.oitc;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.SweepResult;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.projectile.ProjectileMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

import static net.mangolise.oitc.OITC.PLAYER_ARROW_PARTICLE;

public class ArrowEntity extends Entity {
    private final Player shooter;

    public ArrowEntity(@Nullable Player shooter) {
        super(EntityType.ARROW);
        this.shooter = shooter;

        hasPhysics = false;
        if (getEntityMeta() instanceof ProjectileMeta) {
            ((ProjectileMeta) getEntityMeta()).setShooter(this.shooter);
        }

        setBoundingBox(new BoundingBox(0, 0, 0));
    }

    @Nullable
    public Entity getShooter() {
        return this.shooter;
    }

    @Override
    public void tick(long time) {
        final Pos from = getPosition();
        super.tick(time);

        if (isRemoved()) {
            return;
        }

        Pos to = getPosition();

        // Create particles
        double step = 0.1;

        double travelled = 0;
        double total = to.distance(from);

        while (travelled < total) {
            Pos pos = from.add(to.sub(from).asVec().normalize().mul(travelled));
            ParticlePacket packet = new ParticlePacket(this.shooter.getTag(PLAYER_ARROW_PARTICLE), pos, Vec.ZERO, 0, 1);
            PacketUtils.sendGroupedPacket(this.getViewers(), packet);
            travelled += step;
        }

        if (isStuck(from, to)) {
            remove();
        }
    }

    /**
     * Checks whether an arrow is stuck in block / hit an entity.
     *
     * @param from position right before current tick.
     * @param to   position after current tick.
     * @return if an arrow is stuck in block / hit an entity.
     */
    private boolean isStuck(Pos from, Pos to) {
        Instance instance = getInstance();
        if (from.samePoint(to)) {
            return true;
        }

        BoundingBox boundingBox = getBoundingBox();

        Vec movement = to.asVec().sub(from);
        double length = movement.length();
        Vec dir = movement.div(length);
        long aliveTicks = getAliveTicks();

        // Check collision with blocks
        Iterable<Point> iterable = () -> new BlockIterator(from.asVec(), dir, 0, length);
        for (Point pos : iterable) {
            Block block = instance.getBlock(pos);

            if (!block.isSolid()) {
                continue;
            }

            BlockVec posBlock = new BlockVec(pos);

            // do collision check with shape
            SweepResult result = new SweepResult(Double.MAX_VALUE, 0, 0, 0, null, 0, 0, 0);
            if (!block.registry().collisionShape().intersectBoxSwept(from, movement, posBlock.asVec(), boundingBox, result)) {
                continue;
            }

            Pos posPos = getCollidedPosition(result).asPosition().withView(from);

            ProjectileCollideWithBlockEvent event = new ProjectileCollideWithBlockEvent(this, posPos, block);
            EventDispatcher.call(event);
            if (!event.isCancelled()) {
                teleport(posPos);
                return true;
            }
        }

        // Collide with entities
        for (Entity entity : instance.getNearbyEntities(from, length * 4d)) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            if (aliveTicks < 3 && entity == shooter) {
                continue;
            }

            SweepResult result = new SweepResult(Double.MAX_VALUE, 0, 0, 0, null, 0, 0, 0);
            if (entity.getBoundingBox().intersectBoxSwept(from.asVec(), movement, entity.getPosition(), boundingBox, result)) {
                final ProjectileCollideWithEntityEvent event = new ProjectileCollideWithEntityEvent(this, from, entity);
                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    return true;
                }
            }
        }

        return false;
    }

    // Getting the collided position is package-private, so we use reflection :(
    private Vec getCollidedPosition(SweepResult result) {
        return new Vec(
            getCollidedVar(result, 'X'),
            getCollidedVar(result, 'Y'),
            getCollidedVar(result, 'Z')
        );
    }

    private double getCollidedVar(SweepResult result, char coord) {
        try {
            Field field = SweepResult.class.getDeclaredField("collidedPosition" + coord);
            field.setAccessible(true);
            return (double) field.get(result);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
