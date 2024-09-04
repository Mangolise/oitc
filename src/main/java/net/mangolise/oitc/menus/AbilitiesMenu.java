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

public class AbilitiesMenu {
    public static void openMenu(Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, "Abilities Menu");
        inventory.setTag(OITC.MENU_IS_OPEN, true);

        inventory.setItemStack(1, ItemStack.of(Material.BLUE_CONCRETE)
                .withCustomName(Component.text("North Point").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

        player.playSound(Sound.sound(SoundEvent.BLOCK_ENDER_CHEST_OPEN, Sound.Source.PLAYER, 1f, 1f));
        player.openInventory(inventory);
    }
}
