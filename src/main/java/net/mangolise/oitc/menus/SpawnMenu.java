package net.mangolise.oitc.menus;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.oitc.OITC;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class SpawnMenu {
    public static void openMenu(Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, "Spawn Point Menu");
        inventory.setTag(OITC.MENU_IS_OPEN, true);

        inventory.setItemStack(1, ItemStack.of(Material.BLUE_CONCRETE)
                .withCustomName(Component.text("North Point").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

        inventory.setItemStack(3, ItemStack.of(Material.GREEN_CONCRETE)
                .withCustomName(Component.text("East Point").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

        inventory.setItemStack(5, ItemStack.of(Material.RED_CONCRETE)
                .withCustomName(Component.text("South Point").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

        inventory.setItemStack(7, ItemStack.of(Material.GRAY_CONCRETE)
                .withCustomName(Component.text("West Point").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

        player.playSound(Sound.sound(SoundEvent.BLOCK_ENDER_CHEST_OPEN, Sound.Source.PLAYER, 1f, 1f));
        player.openInventory(inventory);
    }

    public static void handlePreClickEvent(InventoryPreClickEvent e, Player player) {
        if (player.getPosition().y() < 22.0) {
            return;
        }

        Pos pos;
        ItemStack clickedItem = e.getClickedItem();

        if (clickedItem.material().equals(Material.BLUE_CONCRETE)) {
            pos = new Pos(-0.5, 29, -43.5, 0, 0);
            player.teleport(pos);
        } else if (clickedItem.material().equals(Material.GREEN_CONCRETE)) {
            pos = new Pos(38.5, 29, -0.5, 90, 0);
            player.teleport(pos);
        } else if (clickedItem.material().equals(Material.RED_CONCRETE)) {
            pos = new Pos(-0.5, 29, 36.5, 180, 0);
            player.teleport(pos);
        } else if (clickedItem.material().equals(Material.GRAY_CONCRETE)) {
            pos = new Pos(-45.5, 28, -0.5, -90, 0);
            player.teleport(pos);
        } else {
            return;
        }

        player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1f, 1f));
        player.closeInventory();
        e.setCancelled(true);
    }
}
