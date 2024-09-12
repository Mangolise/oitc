package net.mangolise.oitc.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.oitc.OITC;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class OitcMenu {
    public static void openMenu(Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, "OITC Menu");
        inventory.setTag(OITC.MENU_ID, "oitc_menu");

        inventory.setItemStack(10, ItemStack.of(Material.CHEST)
                .withCustomName(Component.text("Particle Menu").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

        if (player.getPosition().y() < 22.0) {
            inventory.setItemStack(14, ItemStack.of(Material.BARRIER)
                    .withCustomName(Component.text("Abilities Menu").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED))
                    .withLore(Component.text("Unavailable while in battlefield").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_RED)));
        } else {
            inventory.setItemStack(14, ItemStack.of(Material.ECHO_SHARD)
                    .withCustomName(Component.text("Abilities Menu").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));
        }

        inventory.setItemStack(16, ItemStack.of(Material.NETHER_STAR)
                .withCustomName(Component.text("Leave Menu").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

        if (player.getPosition().y() < 22.0) {
            inventory.setItemStack(12, ItemStack.of(Material.BARRIER)
                    .withCustomName(Component.text("Spawn Point Menu").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED))
                    .withLore(Component.text("Unavailable while in battlefield").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_RED)));
        } else {
            inventory.setItemStack(12, ItemStack.of(Material.ENDER_CHEST)
                    .withCustomName(Component.text("Spawn Point Menu").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));
        }

        player.openInventory(inventory);
    }

    public static void handlePreClickEvent(InventoryPreClickEvent e) {
        if (!"oitc_menu".equals(e.getInventory().getTag(OITC.MENU_ID))) {
            return;
        }

        if (e.getClickedItem().material().equals(Material.CHEST)) {
            ParticleMenu.openMenu(e.getPlayer());
            e.setCancelled(true);
        } else if (e.getClickedItem().material().equals(Material.COMPASS)) {
            OitcMenu.openMenu(e.getPlayer());
            e.setCancelled(true);
        } else if (e.getClickedItem().material().equals(Material.ENDER_CHEST) && e.getPlayer().getPosition().y() > 22.0) {
            SpawnMenu.openMenu(e.getPlayer());
            e.setCancelled(true);
        } else if (e.getClickedItem().material().equals(Material.ECHO_SHARD) && e.getPlayer().getPosition().y() > 22.0) {
            AbilitiesMenu.openMenu(e.getPlayer());
            e.setCancelled(true);
        } else if (e.getClickedItem().material().equals(Material.NETHER_STAR)) {
            LeaveMenu.openMenu(e.getPlayer());
            e.setCancelled(true);
        }
    }
}
