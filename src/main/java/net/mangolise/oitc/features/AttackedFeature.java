package net.mangolise.oitc.features;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.util.GameSdkUtils;
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

import java.util.concurrent.CompletableFuture;

public class AttackedFeature implements Game.Feature<OITC> {
    public void setup(Game.Feature.Context<OITC> context) {

    }

    public static void attacked(Player victim, Player attacker, boolean fromSword, Instance instance) {
        boolean isRevenge = false;
        // player spawn is above y level 22
        if (victim.getPosition().y() > 22.0 || attacker.getPosition().y() > 22.0 ||
                victim.getGameMode().equals(GameMode.SPECTATOR) || attacker.getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }

        int killCount = attacker.getTag(OITC.PLAYER_KILLS);

        if (victim != attacker) {
            killCount = attacker.getAndUpdateTag(OITC.PLAYER_KILLS, kills -> kills + 1) + 1;
            attacker.updateTag(OITC.PLAYER_KILLS_PER_SESSION, sessionKills -> sessionKills + 1);
            attacker.updateTag(OITC.PLAYER_KILL_STREAK, streak -> streak + 1);

            if (attacker.getTag(OITC.PLAYER_KILL_STREAK) >= attacker.getTag(OITC.PLAYER_HIGHEST_KILL_STREAK)) {
                attacker.setTag(OITC.PLAYER_HIGHEST_KILL_STREAK, attacker.getTag(OITC.PLAYER_KILL_STREAK));
            }

            victim.updateTag(OITC.PLAYER_DEATHS, deaths -> deaths + 1);
            victim.setTag(OITC.PLAYER_KILL_STREAK, 0);

            victim.setTag(OITC.PLAYER_LAST_KILLER, attacker.getUuid());

            if (fromSword) {
                victim.updateTag(OITC.PLAYER_DEATHS_BY_SWORD, swordDeaths -> swordDeaths + 1);
                attacker.updateTag(OITC.PLAYER_SWORD_KILLS, swordKills -> swordKills + 1);
            } else {
                victim.updateTag(OITC.PLAYER_DEATHS_BY_CROSSBOW, crossbowDeaths -> crossbowDeaths + 1);
                attacker.updateTag(OITC.PLAYER_CROSSBOW_KILLS, crossbowKills -> crossbowKills + 1);
            }

            if (victim.getUuid().equals(attacker.getTag(OITC.PLAYER_LAST_KILLER))) {
                attacker.updateTag(OITC.PLAYER_REVENGE_KILLS, revenge_kills -> revenge_kills + 1);
                attacker.removeTag(OITC.PLAYER_LAST_KILLER);
                isRevenge = true;
            }
        } else {
            if (victim.getUuid().equals(attacker.getTag(OITC.PLAYER_LAST_KILLER))) {
                isRevenge = true;
            }

            victim.setTag(OITC.PLAYER_LAST_KILLER, attacker.getUuid());
            victim.updateTag(OITC.PLAYER_SUICIDE, suicides -> suicides + 1);
            victim.updateTag(OITC.PLAYER_DEATHS, deaths -> deaths + 1);
            victim.setTag(OITC.PLAYER_KILL_STREAK, 0);
        }

        // instead of killing player, this fakes players death by teleporting them.
        victim.setGameMode(GameMode.SPECTATOR);
        victim.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_DEATH, Sound.Source.PLAYER, 1f, 1f));

        Timer.countDownForPlayer(3, victim).thenRun(() -> {
            victim.setGameMode(GameMode.ADVENTURE);
            victim.teleport(OITC.randomSpawn());
            setAmmo(victim, 1);
        });

        setAmmo(attacker, attacker.getTag(OITC.PLAYERS_AMMO_TAG) + 1);

        if (isRevenge) {
            attacker.playSound(Sound.sound(SoundEvent.BLOCK_BELL_USE, Sound.Source.PLAYER, 0.75f, 1.5f));
        } else {
            attacker.playSound(Sound.sound(SoundEvent.BLOCK_AMETHYST_BLOCK_BREAK, Sound.Source.PLAYER, 1f, 2f));
        }

        for (Player player : instance.getPlayers()) {
            ScoreboardFeature.updateSidebar(player);
        }

        KillMessages.sendDeathMessage(victim, attacker, isRevenge);
        KillMessages.sendKillMessage(attacker, victim, isRevenge);

        int killStreak = attacker.getTag(OITC.PLAYER_KILL_STREAK);
        KillMessages.sendKillStreakMessage(killStreak, attacker, instance);

        KillEvent killEvent = new KillEvent(victim, attacker, killCount, isRevenge);
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
            GameSdkUtils.stopCooldown(player, "arrow");
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
            GameSdkUtils.startCooldown(player, "arrow", Material.ARROW, 10 * 1000);
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

        ParticlePacket packet = new ParticlePacket(particle, true, playerPos.x(), playerPos.y() + 1.5, playerPos.z(),
                0, 0, 0, ExplosionSpeed, particleAmount);
        instance.sendGroupedPacket(packet);
    }
}
