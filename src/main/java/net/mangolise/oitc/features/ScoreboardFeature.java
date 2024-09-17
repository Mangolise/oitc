package net.mangolise.oitc.features;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.util.ChatUtil;
import net.mangolise.gamesdk.util.SidebarBuilder;
import net.mangolise.oitc.OITC;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

import java.util.Comparator;

public class ScoreboardFeature implements Game.Feature<OITC> {
    private static String ip;

    public static void updateSidebar(Player player) {
        SidebarBuilder sidebarBuilder = new SidebarBuilder();
        Sidebar sidebar = player.getTag(OITC.PLAYER_SIDEBAR);
        int totalKills = player.getTag(OITC.PLAYER_KILLS);
        int deaths = player.getTag(OITC.PLAYER_DEATHS);

        sidebarBuilder.addLine(Component.text("----------------").color(NamedTextColor.DARK_GRAY));

        sidebarBuilder.addLine(Component.text("Kill Streak: ").color(TextColor.color(NamedTextColor.GRAY))
                .append(Component.text(player.getTag(OITC.PLAYER_KILL_STREAK)).color(NamedTextColor.GRAY)));

        sidebarBuilder.addLine(Component.text("Total Kills: ").color(TextColor.color(NamedTextColor.GRAY))
                .append(Component.text(totalKills).color(NamedTextColor.GRAY)));

        sidebarBuilder.addLine(Component.text("Deaths: ").color(TextColor.color(NamedTextColor.GRAY))
                .append(Component.text(deaths).color(NamedTextColor.GRAY)));

        sidebarBuilder.addLine(Component.text("----------------").color(NamedTextColor.DARK_GRAY));

        MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getTag(OITC.PLAYER_KILLS))).limit(5).forEach(attacker -> {
            sidebarBuilder.addLine(ChatUtil.getDisplayName(attacker)
                    .append(Component.text(": " + attacker.getTag(OITC.PLAYER_KILLS)).color(NamedTextColor.GRAY)));
        });

        sidebarBuilder.addLine(Component.text("----------------").color(NamedTextColor.DARK_GRAY));
        sidebarBuilder.addLine(Component.text(ip).color(NamedTextColor.GOLD));

        sidebarBuilder.apply(sidebar);
    }

    @Override
    public void setup(Context<OITC> context) {
        ip = context.game().config().serverIp();
    }
}
