package net.mangolise.oitc.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.oitc.OITC;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class OitcMenu {
    public static void openMenu(Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, "OITC Menu");
        inventory.setTag(OITC.MENU_IS_OPEN, true);

        inventory.setItemStack(10, ItemStack.of(Material.CHEST)
                .withCustomName(Component.text("Particle Menu").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

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
}
