package net.mangolise.oitc;

import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponent;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.PotionType;
import net.minestom.server.tag.Tag;

import java.util.Map;

public class ParticleMenu {
    private static final Map<Color, Particle> particleMap = Map.of(new Color(150, 255, 225), Particle.GLOW);
    static final Tag<Particle> ARROW_PARTICLE = Tag.Transient("arrow_particle");

    public static void openMenu(Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_4_ROW, "Particle Menu");

        particleMap.forEach((color, particle) -> {
          ItemStack arrow = ItemStack.of(Material.TIPPED_ARROW).with(ItemComponent.POTION_CONTENTS,
                    new PotionContents(PotionType.AWKWARD, color)).withTag(ARROW_PARTICLE, particle);
            inventory.addItemStack(arrow);
        });

        player.openInventory(inventory);
    }

    public static void handlePreClickEvent(InventoryPreClickEvent e) {
        ItemStack clickedItem = e.getClickedItem();

        if (clickedItem.material().equals(Material.TIPPED_ARROW)) {
            Particle particle = clickedItem.getTag(ARROW_PARTICLE);
            e.getPlayer().setTag(OITC.PLAYER_ARROW_PARTICLE, particle);
            e.setCancelled(true);
            e.getPlayer().closeInventory();
        }
    }
}
