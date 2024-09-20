package net.mangolise.oitc.menus;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.mangolise.gamesdk.util.Timer;
import net.mangolise.oitc.OITC;
import net.mangolise.oitc.events.KillEvent;
import net.mangolise.oitc.events.PlayerLeaveEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

import java.util.concurrent.CompletableFuture;

public class UtilitiesMenu {
    public static void openMenu(Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, "Utilities Menu");
        inventory.setTag(OITC.MENU_ID, "utilities_menu");

        inventory.setItemStack(1, ItemStack.of(Material.RECOVERY_COMPASS)
                .withCustomName(Component.text("Respawn").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

        inventory.setItemStack(4, ItemStack.of(Material.ENDER_EYE)
                .withCustomName(Component.text("Stats").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

        inventory.setItemStack(7, ItemStack.of(Material.ENDER_PEARL)
                .withCustomName(Component.text("Leave").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_AQUA)));

        player.playSound(Sound.sound(SoundEvent.BLOCK_AMETHYST_CLUSTER_BREAK, Sound.Source.PLAYER, 1f, 1f));
        player.openInventory(inventory);
    }

    public static void handlePreClickEvent(InventoryPreClickEvent e, Player player) {
        if (!"utilities_menu".equals(e.getInventory().getTag(OITC.MENU_ID))) {
            return;
        }

        ItemStack clickedItem = e.getClickedItem();

        if (clickedItem.material().equals(Material.RECOVERY_COMPASS)) {
            CompletableFuture<Void> timer = Timer.countDownForPlayer(3, player);
            timer.thenRun(() -> {
                Pos respawnPoint = OITC.randomSpawn();
                player.teleport(respawnPoint);
                player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1f, 1f));
            });

            player.eventNode().addListener(GameSdkUtils.singleUseEvent(KillEvent.class, g -> {
                if (!timer.isDone()) {
                    timer.cancel(true);
                    return false;
                }
                return true;
            }));

            player.eventNode().addListener(GameSdkUtils.singleUseEvent(PlayerMoveEvent.class, f -> {
                if (f.getPlayer().getPosition().distanceSquared(f.getNewPosition()) < Vec.EPSILON) {
                    return false;
                }

                if (!timer.isDone()) {
                    player.playSound(Sound.sound(SoundEvent.BLOCK_ANVIL_LAND, Sound.Source.PLAYER, 1f, 0.75f));
                    timer.cancel(true);
                }

                return true;
            }));
        } else if (clickedItem.material().equals(Material.ENDER_EYE)) {
            StatsMenu.openMenu(player);
            return;
        } else if (clickedItem.material().equals(Material.ENDER_PEARL)) {
            MinecraftServer.getGlobalEventHandler().call(new PlayerLeaveEvent(player));
            return;
        }  else {
            return;
        }

        player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1f, 1f));
        player.closeInventory();
        e.setCancelled(true);
    }
}
