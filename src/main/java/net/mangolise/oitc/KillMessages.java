package net.mangolise.oitc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.util.ChatUtil;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class KillMessages {
    private static final List<String> randomMessagesGeneric = List.of(
            " was killed by ",
            " met their end at the hands of ",
            " was defeated by ",
            " was outwitted by ",
            " suffered a fatal encounter with ",
            " was sent to the afterlife by ",
            " got a surprise visit from ",
            " could not handle ",
            " had their lights turned off by ",
            " found themselves on the wrong side of ",
            " was caught in the crossfire of ",
            " fell victim to the wrath of ",
            " got a one-way ticket to the grave from ",
            " met their doom at the hands of ",
            " had their day ruined by ",
            " bit the dust courtesy of ",
            " became a resident of the underworld thanks to ",
            " became a skill issue thanks to ",
            " got diffed by ",
            " had their breath taken away by ",
            " stopped existing because of ",
            " collapsed in front of ",
            " fell at the hands of "
    );

    private static final List<String> randomMessagesSword = List.of(
            " was sliced and diced by ",
            " got decapitated by ",
            " fell victim to a blade from ",
            " was cleaved in two by ",
            " was on the wrong end of a sword with ",
            " got a sharp lesson from ",
            " met their match with a blade from ",
            " got a brutal edge from ",
            " got a point made with a sword by ",
            " learnt what a sword is from ",
            " was schooled in swordplay by ",
            " was poked to death by ",
            " got a sharp reality check from ",
            " was taught what their insides look like by ",
            " no longer has a head because of ",
            "'s body now belongs to ",
            " forgot how to W tap because of ",
            " strafed the wrong way into "
    );

    private static final List<String> randomMessagesCrossbow = List.of(
            " was pierced by an arrow from ",
            " got arrowed by ",
            " met their end with a bow from ",
            " became a pincushion for ",
            " was struck down by a well-placed shot from ",
            " caught a fatal flight from ",
            " was hit with an arrow courtesy of ",
            " became an archery target thanks to ",
            " was shot full of holes by ",
            " got a free piercing from ",
            " found out that arrows are pointy, courtesy of ",
            " was given a lesson in why you should dodge from ",
            " got 360 no-scoped by ",
            " was quick-scoped by ",
            " fell asleep at a gun range and was shot by "
    );

    private static final List<String> randomMessagesSuicide = List.of(
            " has become their own worst enemy.",
            " faced off against themselves... and lost.",
            " eliminated the biggest threat: themselves.",
            " accidentally self-destructed.",
            " was too much to handle… for themselves.",
            " met their match... in the mirror.",
            " was outplayed by their greatest rival: themselves.",
            " died to themselves, embarrassing.",
            " gave up.",
            " didn't like this world.",
            " couldn't handle their own power.",
            " got outplayed by themselves.",
            " failed the tutorial on living.",
            " forgot who the real enemy was... it’s themselves.",
            " can't win against themselves."
    );

    public static void sendKillStreakMessage(int killStreak, Player attacker, Instance instance) {
        switch (killStreak) {
            case 5 -> killStreakMessage(instance, attacker, " is on ", "Fire", TextColor.color(255, 165, 43));
            case 10 -> killStreakMessage(instance, attacker, " is ", "Killing it", TextColor.color(163, 33, 0));
            case 15 -> killStreakMessage(instance, attacker, " is ", "Dominating", TextColor.color(92, 0, 2));
            case 20 -> killStreakMessage(instance, attacker, " is on a ", "Rampage", TextColor.color(255, 55, 30));
            case 30 -> killStreakMessage(instance, attacker, " on a ", "Tear", TextColor.color(218, 98, 0));
            case 40 -> killStreakMessage(instance, attacker, " is a ", "Killing Machine", TextColor.color(232, 184, 2));
            case 50 -> killStreakMessage(instance, attacker, " is ", "Unstoppable", TextColor.color(0, 228, 222));
            case 60 -> killStreakMessage(instance, attacker, " is ", "Wreaking Havoc", TextColor.color(0, 218, 173));
            case 70 -> killStreakMessage(instance, attacker, " in a ", "Force of Nature", TextColor.color(0, 246, 145));
            case 80 -> killStreakMessage(instance, attacker, " is a ", "Juggernaut", TextColor.color(138, 240, 0));
            case 90 -> killStreakMessage(instance, attacker, " is a ", "Relentless Force", TextColor.color(183, 255, 0));
            case 100 -> killStreakMessage(instance, attacker, "", "MAY Chaos Take the WORLD!", TextColor.color(255, 198, 0));
            case 150 -> killStreakMessage(instance, attacker, " in on ", "One Man Army", TextColor.color(140, 0, 255));
            case 200 -> killStreakMessage(instance, attacker, " is the ", "Chaos Bringer", TextColor.color(232, 0, 255));
            case 250 -> killStreakMessage(instance, attacker, " is a ", "Doom Bringer", TextColor.color(255, 0, 161));
            case 300 -> killStreakMessage(instance, attacker, " is one of the ", "Four Horsemen of the Apocalypse", TextColor.color(59, 59, 59));
            case 350 -> killStreakMessage(instance, attacker, " I'm running out of ", "IDEAS", TextColor.color(75, 180, 193));
            case 450 -> killStreakMessage(instance, attacker, " is ", "Hacking", TextColor.color(118, 64, 123));
            case 500 -> killStreakMessage(instance, attacker, " is playing ", "Fruit Ninja", TextColor.color(72, 206, 71));
            case 550 -> killStreakMessage(instance, attacker, " if you made it this far Nice but ", "Theres nothing left until 1000.", TextColor.color(255, 0, 0));
            case 1000 -> killStreakMessage(instance, attacker, " Needs to go ", "Outside", TextColor.color(0, 108, 4));
        }
    }

    public static void sendDeathMessage(Instance instance, Player victim, Player attacker, boolean fromSword) {
        Random random = ThreadLocalRandom.current();
        String message;

        if (attacker == victim) {
            message = randomMessagesSuicide.get(random.nextInt(randomMessagesSuicide.size()));
            randomDeathMessageSuicide(instance, victim, message);
            return;
        }

        if (random.nextDouble() > 0.666d) {
            if (fromSword) {
                message = randomMessagesSword.get(random.nextInt(randomMessagesSword.size()));
            } else {
                message = randomMessagesCrossbow.get(random.nextInt(randomMessagesCrossbow.size()));
            }
        } else {
            message = randomMessagesGeneric.get(random.nextInt(randomMessagesGeneric.size()));
        }

        randomDeathMessage(instance, victim, attacker, message);
    }

    public static void randomDeathMessage(Instance instance, Player victim, Player attacker, String text) {
        instance.sendMessage(ChatUtil.getDisplayName(victim).decorate(TextDecoration.BOLD)
                .append(Component.text(text).decoration(TextDecoration.BOLD, false).color(NamedTextColor.WHITE))
                .append(ChatUtil.getDisplayName(attacker)).decorate(TextDecoration.BOLD));
    }

    public static void randomDeathMessageSuicide(Instance instance, Player victim, String text) {
        instance.sendMessage(ChatUtil.getDisplayName(victim).decorate(TextDecoration.BOLD)
                .append(Component.text(text).decoration(TextDecoration.BOLD, false).color(NamedTextColor.WHITE)));
    }

    public static void killStreakMessage(Instance instance, Player attacker, String middle, String end, TextColor color) {
        instance.sendMessage(ChatUtil.getDisplayName(attacker).decorate(TextDecoration.BOLD)
                .append(Component.text(middle).decoration(TextDecoration.BOLD, false).color(NamedTextColor.WHITE))
                .append(Component.text(end).decorate(TextDecoration.BOLD).color(color)));
    }
}
