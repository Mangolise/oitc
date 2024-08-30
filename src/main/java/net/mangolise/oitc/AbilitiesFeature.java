package net.mangolise.oitc;

import net.kyori.adventure.sound.Sound;
import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.util.Timer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.concurrent.atomic.AtomicReference;

public class AbilitiesFeature extends OITC implements Game.Feature<OITC> {
    static final Tag<Boolean> PLAYER_CAN_DASH = Tag.Boolean("player_dash").defaultValue(true);

    public AbilitiesFeature(Config config) {
        super(config);
    }

    @Override
    public void setup(Context<OITC> context) {
        context.eventNode().addListener(PlayerSpawnEvent.class, e -> {
            e.getPlayer().setExp(1);
        });

        context.eventNode().addListener(PlayerSwapItemEvent.class, e -> {
            e.setCancelled(true);
            Instance instance = e.getInstance();
            Player player = e.getPlayer();

            if (e.getPlayer().getTag(PLAYER_CAN_DASH)) {
                Vec pos = player.getPosition().direction();
                player.setVelocity(pos.mul(40, 20, 40));
                player.setTag(PLAYER_CAN_DASH, false);
                instance.playSound(Sound.sound(SoundEvent.ENTITY_BREEZE_JUMP, Sound.Source.PLAYER, 1f, 1f), player.getPosition());

                playerDashParticle(player, instance);

                Timer.countDown(5, i -> {
                    player.setLevel(i);
                    player.setExp(1 - ((float) i / 5f));
                }).thenRun(() -> {
                    player.setTag(PLAYER_CAN_DASH, true);
                    player.setLevel(0);
                    player.setExp(1);
                    player.playSound(Sound.sound(SoundEvent.ITEM_BOTTLE_FILL_DRAGONBREATH, Sound.Source.PLAYER, 1f, 1f));
                });
            }
        });
    }

    public void playerDashParticle(Player player, Instance instance) {
        Particle particle = Particle.WHITE_SMOKE;

        AtomicReference<Task> task = new AtomicReference<>();
        task.set(MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            Pos playerPos = player.getPosition();
            ParticlePacket packet = new ParticlePacket(particle, true, playerPos.x(), playerPos.y(), playerPos.z(), 0, 0, 0, 0.1f, 5);
            instance.sendGroupedPacket(packet);
        }, TaskSchedule.nextTick(), TaskSchedule.tick(1)));

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            task.get().cancel();
        }, TaskSchedule.millis(500), TaskSchedule.stop());
    }
}
