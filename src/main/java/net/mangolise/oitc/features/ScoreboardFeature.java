package net.mangolise.oitc.features;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.util.ChatUtil;
import net.mangolise.gamesdk.util.SidebarBuilder;
import net.mangolise.oitc.OITC;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ScoreboardFeature implements Game.Feature<OITC> {
    private static String ip;

    public static void updateSidebar(Player player, Instance instance, Map<UUID, Integer> kills) {
        Set<Map.Entry<UUID, Integer>> killSet = kills.entrySet();
        SidebarBuilder sidebarBuilder = new SidebarBuilder();
        Sidebar sidebar = player.getTag(OITC.PLAYER_SIDEBAR);

        sidebarBuilder.addLine(Component.text("----------------").color(NamedTextColor.DARK_GRAY));

        sidebarBuilder.addLine(Component.text("Kill Streak: ").color(TextColor.color(NamedTextColor.GRAY))
                .append(Component.text(player.getTag(OITC.PLAYER_KILL_STREAK)).color(NamedTextColor.GRAY)));

        sidebarBuilder.addLine(Component.text("----------------").color(NamedTextColor.DARK_GRAY));

        killSet.stream().sorted(Comparator.comparingInt(entry -> -entry.getValue())).limit(5).forEach(entry -> {
            Player attacker = instance.getPlayerByUuid(entry.getKey());
            assert attacker != null;

            sidebarBuilder.addLine(ChatUtil.getDisplayName(attacker).append(Component.text(": " + entry.getValue()).color(NamedTextColor.GRAY)));
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
