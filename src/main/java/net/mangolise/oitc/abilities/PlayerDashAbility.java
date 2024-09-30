package net.mangolise.oitc.abilities;

import net.kyori.adventure.sound.Sound;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.mangolise.gamesdk.util.Timer;
import net.mangolise.oitc.events.PlayerAbilityEvent;
import net.mangolise.oitc.features.AbilitiesFeature;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerDashAbility {
    private static final int COOLDOWN_SECONDS = 5;

    public static void playerDashAbility(PlayerSwapItemEvent e) {
        e.setCancelled(true);
        Instance instance = e.getInstance();
        Player player = e.getPlayer();

        if (e.getPlayer().getTag(AbilitiesFeature.PLAYER_CAN_USE_ABILITY)) {
            MinecraftServer.getGlobalEventHandler().call(new PlayerAbilityEvent(player, COOLDOWN_SECONDS * 1000));
            GameSdkUtils.startCooldown(player, "dash", Material.FEATHER, COOLDOWN_SECONDS * 1000);
            Vec pos = player.getPosition().direction();
            player.setVelocity(pos.mul(40, 20, 40));
            player.setTag(AbilitiesFeature.PLAYER_CAN_USE_ABILITY, false);
            instance.playSound(Sound.sound(SoundEvent.ENTITY_BREEZE_JUMP, Sound.Source.PLAYER, 3f, 1f), player.getPosition());

            playerDashParticle(player, instance);

            CompletableFuture<Void> timer = Timer.countDown(COOLDOWN_SECONDS * 20, 1, i -> {
                player.setExp(1 - ((float) i / (5f * 20f)));

                if (i % 20 == 0) {
                    player.setLevel(i / 20);
                }
            });
            timer.thenRun(() -> {
                player.setTag(AbilitiesFeature.PLAYER_CAN_USE_ABILITY, true);
                player.setLevel(0);
                player.setExp(1);
                player.playSound(Sound.sound(SoundEvent.ITEM_BOTTLE_FILL_DRAGONBREATH, Sound.Source.PLAYER, 1f, 1f));
            });
            AbilitiesFeature.abilityCountDown.put(player.getUuid(), timer);
        }
    }

    public static void playerDashParticle(Player player, Instance instance) {
        Particle particleMain = Particle.WHITE_SMOKE;
        Particle particleSecondary = Particle.CLOUD;

        AtomicReference<Task> taskMain = new AtomicReference<>();
        taskMain.set(MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            Pos playerPos = player.getPosition();
            ParticlePacket packetMain = new ParticlePacket(particleMain, true, playerPos.x(), playerPos.y(), playerPos.z(),
                    0, 0, 0, 0.1f, 5);
            instance.sendGroupedPacket(packetMain);
        }, TaskSchedule.nextTick(), TaskSchedule.tick(1)));

        AtomicReference<Task> taskSecondary = new AtomicReference<>();
        taskSecondary.set(MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            Pos playerPos = player.getPosition();
            ParticlePacket packetSecondary = new ParticlePacket(particleSecondary, true, playerPos.x(), playerPos.y(), playerPos.z(),
                    0, 0, 0, 0.15f, 2);
            instance.sendGroupedPacket(packetSecondary);
        }, TaskSchedule.nextTick(), TaskSchedule.tick(5)));

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            taskMain.get().cancel();
            taskSecondary.get().cancel();
        }, TaskSchedule.millis(350), TaskSchedule.stop());
    }
}
