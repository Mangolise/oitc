package net.mangolise.oitc.menus;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.oitc.OITC;
import net.mangolise.oitc.abilities.AbilityType;
import net.mangolise.oitc.features.AbilitiesFeature;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

public class AbilitiesMenu {
    public static void openMenu(Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, "Abilities Menu");
        inventory.setTag(OITC.MENU_IS_OPEN, true);

        inventory.setItemStack(2, ItemStack.of(Material.FEATHER)
                .withCustomName(Component.text("Dash").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_GREEN)));

        inventory.setItemStack(4, ItemStack.of(Material.RABBIT_FOOT)
                .withCustomName(Component.text("Speed").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

        inventory.setItemStack(6, ItemStack.of(Material.ENDER_PEARL)
                .withCustomName(Component.text("Teleport").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_PURPLE)));

        player.playSound(Sound.sound(SoundEvent.BLOCK_SCULK_SHRIEKER_HIT, Sound.Source.PLAYER, 1f, 1f));
        player.openInventory(inventory);
    }

    public static void handlePreClickEvent(InventoryPreClickEvent e, Player player) {
        if (player.getPosition().y() < 22.0) {
            return;
        }

        ItemStack clickedItem = e.getClickedItem();

        if (clickedItem.material().equals(Material.FEATHER)) {
            player.setTag(AbilitiesFeature.PLAYER_SELECTED_ABILITY, AbilityType.DASH);
        } else if (clickedItem.material().equals(Material.RABBIT_FOOT)) {
            player.setTag(AbilitiesFeature.PLAYER_SELECTED_ABILITY, AbilityType.SPEED);
        } else if (clickedItem.material().equals(Material.ENDER_PEARL)) {
            player.setTag(AbilitiesFeature.PLAYER_SELECTED_ABILITY, AbilityType.TELEPORT);
        } else {
            return;
        }

        player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1f, 1f));
        player.closeInventory();
        e.setCancelled(true);
    }
}
