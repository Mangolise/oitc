package net.mangolise.oitc.abilities;

import net.kyori.adventure.sound.Sound;
import net.mangolise.gamesdk.util.Timer;
import net.mangolise.oitc.events.PlayerAbilityEvent;
import net.mangolise.oitc.features.AbilitiesFeature;
import net.mangolise.oitc.features.AttackedFeature;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerTeleportAbility {
    public static void playerTeleportAbility(PlayerSwapItemEvent e) {
        e.setCancelled(true);
        Instance instance = e.getInstance();
        Player player = e.getPlayer();

        if (e.getPlayer().getTag(AbilitiesFeature.PLAYER_CAN_USE_ABILITY)) {
            MinecraftServer.getGlobalEventHandler().call(new PlayerAbilityEvent(player));

            Entity pearl = new EntityProjectile(player, EntityType.ENDER_PEARL);

            pearl.setInstance(instance).thenRun(() -> {
                pearl.teleport(player.getPosition().add(0, player.getEyeHeight(), 0));
                pearl.setVelocity(player.getPosition().direction().mul(50));
            });

            pearlParticle(pearl, instance);

            pearl.eventNode().addListener(ProjectileCollideWithBlockEvent.class, hallo -> {
                player.teleport(hallo.getCollisionPosition().add(0, 0.5, 0).withView(player.getPosition()));
                AttackedFeature.poof(Particle.DRAGON_BREATH, player, 0.2f, instance, 150);
                instance.playSound(Sound.sound(SoundEvent.ENTITY_ENDERMAN_TELEPORT, Sound.Source.PLAYER, 3f, 1f), player.getPosition());
                pearl.remove();
            });

            player.setTag(AbilitiesFeature.PLAYER_CAN_USE_ABILITY, false);
            instance.playSound(Sound.sound(SoundEvent.ENTITY_WIND_CHARGE_THROW, Sound.Source.PLAYER, 3f, 1f), player.getPosition());

            CompletableFuture<Void> timer = Timer.countDown(8 * 20, 1, i -> {
                player.setExp(1 - ((float) i / (8f * 20f)));

                if (i % 20 == 0) {
                    player.setLevel(i / 20);
                }
            });
            timer.thenRun(() -> {
                player.setTag(AbilitiesFeature.PLAYER_CAN_USE_ABILITY, true);
                player.setLevel(0);
                player.setExp(1);
                player.playSound(Sound.sound(SoundEvent.BLOCK_END_PORTAL_FRAME_FILL, Sound.Source.PLAYER, 2f, 1f));
            });
            AbilitiesFeature.abilityCountDown.put(player.getUuid(), timer);
        }
    }

    public static void pearlParticle(Entity pearl, Instance instance) {
        Particle particle = Particle.SCULK_CHARGE_POP;

        pearl.eventNode().addListener(EntityTickEvent.class, e -> {
            Pos entityPos = pearl.getPosition();
            ParticlePacket packet = new ParticlePacket(particle, true, entityPos.x(), entityPos.y(), entityPos.z(), 0, 0, 0, 0.1f, 5);
            instance.sendGroupedPacket(packet);
        });
    }
}
