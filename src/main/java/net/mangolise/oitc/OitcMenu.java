package net.mangolise.oitc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class OitcMenu {
    public static void openMenu(Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, "OITC Menu");
        inventory.setTag(OITC.OITC_MENU_IS_OPEN, true);

        inventory.setItemStack(10, ItemStack.of(Material.CHEST)
                .withCustomName(Component.text("Particle Menu").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

        player.openInventory(inventory);
    }
}
