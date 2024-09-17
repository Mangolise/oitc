package net.mangolise.oitc.menus;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.oitc.OITC;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class StatsMenu {
    public static void openMenu(Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, "Stats Menu");
        inventory.setTag(OITC.MENU_ID, "stats_menu");

        ItemStack playerStats = ItemStack.of(Material.PLAYER_HEAD)
                .withCustomName(Component.text("Your Stats").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_AQUA));

        Component swordDeathLore = Component.text("Deaths ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED)
                .append(Component.text("by ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY))
                .append(Component.text("Sword: ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD))
                .append(Component.text(player.getTag(OITC.PLAYER_DEATHS_BY_SWORD)).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));

        Component crossbowDeathLore = Component.text("Deaths ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED)
                .append(Component.text("by ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY))
                .append(Component.text("Crossbow: ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD))
                .append(Component.text(player.getTag(OITC.PLAYER_DEATHS_BY_CROSSBOW)).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));

        Component swordKillsLore = Component.text("Kills ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED)
                .append(Component.text("with ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY))
                .append(Component.text("Sword: ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD))
                .append(Component.text(player.getTag(OITC.PLAYER_SWORD_KILLS)).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));

        Component crossbowKillsLore = Component.text("Kills ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED)
                .append(Component.text("with ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY))
                .append(Component.text("Crossbow: ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD))
                .append(Component.text(player.getTag(OITC.PLAYER_CROSSBOW_KILLS)).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));

        inventory.setItemStack(4, playerStats.withLore(swordDeathLore, crossbowDeathLore, swordKillsLore, crossbowKillsLore));

        player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1f, 1f));
        player.openInventory(inventory);
    }
}