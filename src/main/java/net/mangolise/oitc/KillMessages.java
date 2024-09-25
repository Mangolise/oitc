package net.mangolise.oitc;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.util.ChatUtil;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

public class KillMessages {
    public static void sendKillStreakMessage(int killStreak, Player attacker, Instance instance) {
        switch (killStreak) {
            case 5 -> killStreakMessage(instance, attacker, " is on ", "Fire", TextColor.color(255, 165, 43), 1f, SoundEvent.ENTITY_WARDEN_SONIC_BOOM);
            case 10 -> killStreakMessage(instance, attacker, " is ", "Killing it", TextColor.color(163, 33, 0), 1f, SoundEvent.ENTITY_WARDEN_ROAR);
            case 15 -> killStreakMessage(instance, attacker, " is ", "Dominating", TextColor.color(92, 0, 2), 1f, SoundEvent.ENTITY_LIGHTNING_BOLT_THUNDER);
            case 20 -> killStreakMessage(instance, attacker, " is on a ", "Rampage", TextColor.color(255, 55, 30), 1f, SoundEvent.ENTITY_WARDEN_SONIC_BOOM);
            case 30 -> killStreakMessage(instance, attacker, " on a ", "Tear", TextColor.color(218, 98, 0), 1f, SoundEvent.ENTITY_WARDEN_DEATH);
            case 40 -> killStreakMessage(instance, attacker, " is a ", "Killing Machine", TextColor.color(232, 184, 2), 1f, SoundEvent.ENTITY_WITHER_HURT);
            case 50 -> killStreakMessage(instance, attacker, " is ", "Unstoppable", TextColor.color(0, 228, 222), 1f, SoundEvent.ENTITY_ELDER_GUARDIAN_CURSE);
            case 60 -> killStreakMessage(instance, attacker, " is ", "Wreaking Havoc", TextColor.color(0, 218, 173), 1f, SoundEvent.ENTITY_ILLUSIONER_CAST_SPELL);
            case 70 -> killStreakMessage(instance, attacker, " in a ", "Force of Nature", TextColor.color(0, 246, 145), 1f, SoundEvent.ENTITY_BREEZE_DEATH);
            case 80 -> killStreakMessage(instance, attacker, " is a ", "Juggernaut", TextColor.color(138, 240, 0), 1f, SoundEvent.ENTITY_IRON_GOLEM_DEATH);
            case 90 -> killStreakMessage(instance, attacker, " is a ", "Relentless Force", TextColor.color(183, 255, 0), 1f, SoundEvent.ENTITY_RAVAGER_DEATH);
            case 100 -> killStreakMessage(instance, attacker, "", "MAY Chaos Take the WORLD!", TextColor.color(255, 198, 0), 1f, SoundEvent.ENTITY_WITHER_DEATH);
            case 150 -> killStreakMessage(instance, attacker, " in on ", "One Man Army", TextColor.color(140, 0, 255), 1f, SoundEvent.ENTITY_PHANTOM_DEATH);
            case 200 -> killStreakMessage(instance, attacker, " is the ", "Chaos Bringer", TextColor.color(232, 0, 255), 1f, SoundEvent.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM);
            case 250 -> killStreakMessage(instance, attacker, " is a ", "Doom Bringer", TextColor.color(255, 0, 161), 1f, SoundEvent.ENTITY_EVOKER_CAST_SPELL);
            case 300 -> killStreakMessage(instance, attacker, " is one of the ", "Four Horsemen of the Apocalypse", TextColor.color(59, 59, 59), 1f, SoundEvent.ENTITY_EVOKER_PREPARE_ATTACK);
            case 350 -> killStreakMessage(instance, attacker, " I'm running out of ", "IDEAS", TextColor.color(75, 180, 193), 1f, SoundEvent.ENTITY_EVOKER_PREPARE_WOLOLO);
            case 450 -> killStreakMessage(instance, attacker, " is ", "Hacking", TextColor.color(118, 64, 123), 1f, SoundEvent.ENTITY_EVOKER_PREPARE_SUMMON);
            case 500 -> killStreakMessage(instance, attacker, " is playing ", "Fruit Ninja", TextColor.color(72, 206, 71), 1f, SoundEvent.ENTITY_ILLUSIONER_PREPARE_BLINDNESS);
            case 550 -> killStreakMessage(instance, attacker, " if you made it this far Nice but ", "Theres nothing left until 1000.", TextColor.color(255, 0, 0), 1f, SoundEvent.ENTITY_ILLUSIONER_PREPARE_MIRROR);
            case 1000 -> killStreakMessage(instance, attacker, " Needs to go ", "Outside", TextColor.color(0, 108, 4), 1f, SoundEvent.BLOCK_END_PORTAL_SPAWN);
        }
    }

    public static void sendDeathMessage(Player victim, Player attacker, boolean revenge) {
        if (victim == attacker) {
            if (revenge) {
                victim.sendMessage(Component.text("REVENGE SUICIDE! ").color(NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD)
                        .append(ChatUtil.getDisplayName(attacker).decoration(TextDecoration.BOLD, false)));
                return;
            }
            victim.sendMessage(Component.text("SUICIDE! ").color(NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD)
                    .append(ChatUtil.getDisplayName(attacker).decoration(TextDecoration.BOLD, false)));
            return;
        }

        victim.sendMessage(Component.text("DEATH! ").color(NamedTextColor.RED).decorate(TextDecoration.BOLD)
                .append(ChatUtil.getDisplayName(attacker).decoration(TextDecoration.BOLD, false)));
    }

    public static void sendKillMessage(Player victim, Player attacker, boolean revenge) {
        if (victim == attacker) {
            return;
        }

        if (revenge) {
            victim.sendMessage(Component.text("REVENGE KILL! ").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                    .append(ChatUtil.getDisplayName(attacker).decoration(TextDecoration.BOLD, false)));
            return;
        }

        victim.sendMessage(Component.text("KILL! ").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                .append(ChatUtil.getDisplayName(attacker).decoration(TextDecoration.BOLD, false)));
    }

    public static void killStreakMessage(Instance instance, Player attacker, String middle, String end, TextColor color, float pitch, SoundEvent soundEvent) {
        instance.sendMessage(ChatUtil.getDisplayName(attacker).decorate(TextDecoration.BOLD)
                .append(Component.text(middle).decoration(TextDecoration.BOLD, false).color(NamedTextColor.WHITE))
                .append(Component.text(end).decorate(TextDecoration.BOLD).color(color)));
        attacker.playSound(Sound.sound(soundEvent, Sound.Source.PLAYER, 1f, pitch));
    }
}
