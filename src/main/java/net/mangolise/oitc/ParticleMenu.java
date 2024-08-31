package net.mangolise.oitc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.minestom.server.color.Color;
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

import java.util.List;

public class ParticleMenu {
    private static final Tag<Integer> ARROW_PARTICLE = Tag.Integer("arrow_particle");
    private static final List<ColoredParticle> particles = List.of(
            new ColoredParticle(new Color(150, 255, 225), Particle.GLOW)
    );

    public static void openMenu(Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_4_ROW, "Particle Menu");

        for (int i = 0; i < particles.size(); i++) {
            ColoredParticle coloredParticle = particles.get(i);
            Particle particle = coloredParticle.particle();

            inventory.addItemStack(ItemStack.of(Material.TIPPED_ARROW).with(ItemComponent.POTION_CONTENTS,
                    new PotionContents(PotionType.AWKWARD, coloredParticle.color())).withTag(ARROW_PARTICLE, i)
                    .withCustomName(Component.text(GameSdkUtils.capitaliseFirstLetter(particle.key().value().replace('_', ' '))).decoration(TextDecoration.ITALIC, false)));
        }

        player.openInventory(inventory);
    }

    public static void handlePreClickEvent(InventoryPreClickEvent e) {
        ItemStack clickedItem = e.getClickedItem();

        if (clickedItem.material().equals(Material.TIPPED_ARROW)) {
            Particle particle = particles.get(clickedItem.getTag(ARROW_PARTICLE)).particle();
            e.getPlayer().setTag(OITC.PLAYER_ARROW_PARTICLE, particle);
            e.setCancelled(true);
            e.getPlayer().closeInventory();
        }
    }

    private record ColoredParticle(Color color, Particle particle) {}
}
