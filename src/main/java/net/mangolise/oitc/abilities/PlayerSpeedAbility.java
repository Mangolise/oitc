package net.mangolise.oitc.abilities;

import net.kyori.adventure.sound.Sound;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.mangolise.gamesdk.util.Timer;
import net.mangolise.oitc.events.PlayerAbilityEvent;
import net.mangolise.oitc.features.AbilitiesFeature;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerSpeedAbility {
    private static final int COOLDOWN_SECONDS = 6;

    public static void playerSpeedAbility(PlayerSwapItemEvent e) {
        e.setCancelled(true);
        Instance instance = e.getInstance();
        Player player = e.getPlayer();

        if (e.getPlayer().getTag(AbilitiesFeature.PLAYER_CAN_USE_ABILITY)) {
            if (player.hasTag(AbilitiesFeature.PLAYER_CURRENT_ABILITY)) {
                CompletableFuture<Void> sprintDuration = player.getTag(AbilitiesFeature.PLAYER_CURRENT_ABILITY);
                if (!sprintDuration.isDone()) {
                    sprintDuration.cancel(true);
                }
            }

            if (AbilitiesFeature.abilityCountDown.containsKey(player.getUuid())) {
                CompletableFuture<Void> sprintCooldown = AbilitiesFeature.abilityCountDown.get(player.getUuid());
                if (!sprintCooldown.isDone()) {
                    sprintCooldown.cancel(true);
                    AbilitiesFeature.abilityCountDown.remove(player.getUuid());
                    player.setLevel(0);
                }
            }

            MinecraftServer.getGlobalEventHandler().call(new PlayerAbilityEvent(player, COOLDOWN_SECONDS * 1000));
            GameSdkUtils.startCooldown(player, "speed", Material.BLAZE_POWDER, COOLDOWN_SECONDS * 1000);

            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.18);
            player.setTag(AbilitiesFeature.PLAYER_CAN_USE_ABILITY, false);
            instance.playSound(Sound.sound(SoundEvent.ENTITY_BREEZE_WIND_BURST, Sound.Source.PLAYER, 3f, 1f), player.getPosition());

            // 8 * 20 is converting the timer from seconds into ticks.
            CompletableFuture<Void> abilityUseDuration = Timer.countDown(COOLDOWN_SECONDS * 20, 1, i -> {
                player.setExp((float) i / (COOLDOWN_SECONDS * 20));
                spawnParticle(i, player, instance);
            });
            abilityUseDuration.thenRun(() -> {
                player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
                player.playSound(Sound.sound(SoundEvent.ENTITY_ILLUSIONER_CAST_SPELL, Sound.Source.PLAYER, 1f, 1f));

                AtomicReference<CompletableFuture<Void>> visualCoolDownTimerRef = new AtomicReference<>();
                CompletableFuture<Void> visualCoolDownTimer = Timer.countDown(10 * 20, 1, i -> {
                    if (player.getTag(AbilitiesFeature.PLAYER_CAN_USE_ABILITY)) {
                        visualCoolDownTimerRef.get().complete(null);
                        return;
                    }

                    player.setExp(1 - ((float) i / (10f * 20f)));

                    if (i % 20 == 0) {
                        player.setLevel(i / 20);
                    }
                });
                visualCoolDownTimerRef.set(visualCoolDownTimer);

                visualCoolDownTimer.thenRun(() -> {
                    player.setTag(AbilitiesFeature.PLAYER_CAN_USE_ABILITY, true);
                    player.setLevel(0);
                    player.setExp(1);
                    player.playSound(Sound.sound(SoundEvent.BLOCK_RESPAWN_ANCHOR_CHARGE, Sound.Source.PLAYER, 1f, 1f));
                });

                AbilitiesFeature.abilityCountDown.put(player.getUuid(), visualCoolDownTimer);
                GameSdkUtils.startCooldown(player, "speedability", Material.RABBIT_FOOT, 10 * 1000);
            });
            player.setTag(AbilitiesFeature.PLAYER_CURRENT_ABILITY, abilityUseDuration);
        }
    }

    private static void spawnParticle(int i, Player player, Instance instance) {
        // This doesn't run if a quarter second left on the timer.
        if (i < 5) {
            return;
        }

        Particle particleMain = Particle.FLAME;
        Particle particleSecondary = Particle.LAVA;
        Particle particleThird = Particle.SMOKE;

        Pos playerPos = player.getPosition();
        ParticlePacket packetMain = new ParticlePacket(particleMain, true, playerPos.x(), playerPos.y(), playerPos.z(),
                0, 0, 0, 0.1f, 8);
        ParticlePacket packetSecondary = new ParticlePacket(particleSecondary, true, playerPos.x(), playerPos.y(), playerPos.z(),
                0, 0, 0, 0.2f, 1);
        ParticlePacket packetThird = new ParticlePacket(particleThird, true, playerPos.x(), playerPos.y(), playerPos.z(), 0,
                0, 0, 0.15f, 4);

        instance.sendGroupedPacket(packetMain);
        instance.sendGroupedPacket(packetSecondary);
        instance.sendGroupedPacket(packetThird);
    }
}
