package net.mangolise.oitc.features;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.util.Timer;
import net.mangolise.oitc.KillMessages;
import net.mangolise.oitc.OITC;
import net.mangolise.oitc.menus.ParticleMenu;
import net.mangolise.oitc.events.KillEvent;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AttackedFeature implements Game.Feature<OITC> {
    public void setup(Game.Feature.Context<OITC> context) {

    }

    public static void attacked(Player victim, Player attacker, boolean fromSword, Instance instance, Map<UUID, Integer> kills) {
        // player spawn is above y level 22
        if (victim.getPosition().y() > 22.0 || attacker.getPosition().y() > 22.0 ||
                victim.getGameMode().equals(GameMode.SPECTATOR) || attacker.getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }

        int killCount = kills.get(attacker.getUuid()) + 1;
        victim.setTag(OITC.PLAYER_KILL_STREAK, 0);
        attacker.updateTag(OITC.PLAYER_KILL_STREAK, streak -> streak + 1);

        // instead of killing player, this fakes players death by teleporting them.
        KillMessages.sendDeathMessage(instance, victim, attacker, fromSword);
        victim.setGameMode(GameMode.SPECTATOR);
        victim.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_DEATH, Sound.Source.PLAYER, 1f, 1f));

        Timer.countDownForPlayer(3, victim).thenRun(() -> {
            victim.setGameMode(GameMode.ADVENTURE);
            victim.teleport(OITC.randomSpawn());
        });

        attacker.playSound(Sound.sound(SoundEvent.BLOCK_AMETHYST_BLOCK_BREAK, Sound.Source.PLAYER, 1f, 2f));
        setAmmo(attacker, attacker.getTag(OITC.PLAYERS_AMMO_TAG) + 1);

        setAmmo(victim, 1);

        kills.put(attacker.getUuid(), killCount);

        for (Player player : instance.getPlayers()) {
            ScoreboardFeature.updateSidebar(player, instance, kills);
        }

        int killStreak = attacker.getTag(OITC.PLAYER_KILL_STREAK);
        KillMessages.sendKillStreakMessage(killStreak, attacker, instance);

        KillEvent killEvent = new KillEvent(victim, attacker, killCount);
        EventDispatcher.call(killEvent);

        Particle particle = Particle.POOF;
        poof(particle, victim, 0.1f, instance, 30);
    }

    public static void setAmmo(Player player, int amount) {
        player.setTag(OITC.PLAYERS_AMMO_TAG, amount);

        // cancels a timer if the player gets a kill.
        if (OITC.arrowCountdown.containsKey(player.getUuid())) {
            CompletableFuture<Void> timer = OITC.arrowCountdown.get(player.getUuid());
            timer.complete(null);
            player.sendActionBar(Component.text());
        }

        if (amount <= 0) {
            CompletableFuture<Void> timer = Timer.countDown(10, i -> {
                player.sendActionBar(Component.text(i).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            });
            timer.thenRun(() -> {
                setAmmo(player, amount + 1);
                player.playSound(Sound.sound(SoundEvent.ITEM_CROSSBOW_QUICK_CHARGE_3, Sound.Source.PLAYER, 1f, 2f));
            });
            OITC.arrowCountdown.put(player.getUuid(), timer);
            player.getInventory().setItemStack(findCrossbow(player), OITC.crossbow);
        } else {
            player.getInventory().setItemStack(findCrossbow(player), OITC.chargedCrossbow);
        }

        ParticleMenu.updateAmmoDisplay(player, amount);
    }

    public static int findCrossbow(Player player) {
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItemStack(i).material() == Material.CROSSBOW) {
                return i;
            }
        }

        return  -1;
    }

    public static void poof(Particle particle, Player victim, float ExplosionSpeed, Instance instance, int particleAmount) {
        Pos playerPos = victim.getPosition().sub(0, 1, 0);

        ParticlePacket packet = new ParticlePacket(particle, true, playerPos.x(), playerPos.y() + 1.5, playerPos.z(), 0, 0, 0, ExplosionSpeed, particleAmount);
        instance.sendGroupedPacket(packet);
    }
}
