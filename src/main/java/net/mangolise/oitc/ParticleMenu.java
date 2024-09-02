package net.mangolise.oitc;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;

import java.util.List;

public class ParticleMenu {
    private static final Tag<Integer> ARROW_PARTICLE = Tag.Integer("arrow_particle");
    public static final List<ColoredParticle> particles = List.of(
            new ColoredParticle(new Color(250, 236, 217), Particle.CRIT),
            new ColoredParticle(new Color(222, 91, 209), Particle.WITCH),
            new ColoredParticle(new Color(242, 153, 233), Particle.CHERRY_LEAVES),
            new ColoredParticle(new Color(35, 125, 41), Particle.SPORE_BLOSSOM_AIR),
            new ColoredParticle(new Color(117, 34, 41), Particle.CRIMSON_SPORE),
            new ColoredParticle(new Color(151, 234, 240), Particle.BUBBLE_COLUMN_UP),
            new ColoredParticle(new Color(186, 131, 235), Particle.DRAGON_BREATH),
            new ColoredParticle(new Color(121, 252, 213), Particle.GLOW),
            new ColoredParticle(new Color(237, 132, 52), Particle.FLAME),
            new ColoredParticle(new Color(77, 178, 250), Particle.TRIAL_SPAWNER_DETECTION),
            new ColoredParticle(new Color(245, 171, 86), Particle.TRIAL_SPAWNER_DETECTION_OMINOUS),
            new ColoredParticle(new Color(255, 141, 10), Particle.WAX_ON),
            new ColoredParticle(new Color(50, 50, 50), Particle.MYCELIUM),
            new ColoredParticle(new Color(117, 219, 240), Particle.SCULK_CHARGE_POP),
            new ColoredParticle(new Color(255, 255, 225), Particle.ENCHANT),
            new ColoredParticle(new Color(62, 156, 176), Particle.WARPED_SPORE),
            new ColoredParticle(new Color(31, 110, 128), Particle.VIBRATION),
            new ColoredParticle(new Color(234, 172, 250), Particle.ELECTRIC_SPARK),
            new ColoredParticle(new Color(220, 211, 230), Particle.WHITE_ASH),
            new ColoredParticle(new Color(255, 255, 255), Particle.END_ROD),
            new ColoredParticle(new Color(255, 100, 28), Particle.DRIPPING_LAVA),
            new ColoredParticle(new Color(28, 160, 255), Particle.DRIPPING_WATER),
            new ColoredParticle(new Color(143, 100, 0), Particle.DRIPPING_HONEY),
            new ColoredParticle(new Color(100, 0, 143), Particle.DRIPPING_OBSIDIAN_TEAR),
            new ColoredParticle(new Color(219, 219, 219), Particle.FIREWORK),
            new ColoredParticle(new Color(89, 111, 247), Particle.RAIN),
            new ColoredParticle(new Color(72, 97, 250), Particle.FISHING),
            new ColoredParticle(new Color(145, 145, 145), Particle.INFESTED),
            new ColoredParticle(new Color(255, 132, 18), Particle.LAVA),
            new ColoredParticle(new Color(148, 4, 196), Particle.REVERSE_PORTAL),
            new ColoredParticle(new Color(232, 156, 56), Particle.SMALL_FLAME),
            new ColoredParticle(new Color(199, 193, 185), Particle.SMALL_GUST),
            new ColoredParticle(new Color(240, 197, 67), Particle.TOTEM_OF_UNDYING)
    );

    public static void openMenu(Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_4_ROW, "Particle Menu");
        inventory.setTag(OITC.PARTICLE_MENU_IS_OPEN, true);
        Particle playerParticle = player.getTag(OITC.PLAYER_ARROW_PARTICLE);

        for (int i = 0; i < particles.size(); i++) {
            ColoredParticle coloredParticle = particles.get(i);
            Particle particle = coloredParticle.particle();
            boolean glowing = particle.equals(playerParticle);

            inventory.addItemStack(ItemStack.of(Material.TIPPED_ARROW).with(ItemComponent.POTION_CONTENTS,
                    new PotionContents(PotionType.AWKWARD, coloredParticle.color())).withTag(ARROW_PARTICLE, i)
                    .withCustomName(Component.text(GameSdkUtils.capitaliseFirstLetter(particle.key().value().replace('_', ' ')))
                            .decoration(TextDecoration.ITALIC, false).color(TextColor.color(coloredParticle.color()))).withGlowing(glowing));
        }

        player.openInventory(inventory);
    }

    public static void handlePreClickEvent(InventoryPreClickEvent e) {
        ItemStack clickedItem = e.getClickedItem();

        if (clickedItem.material().equals(Material.TIPPED_ARROW)) {
            e.setCancelled(true);

            Player player = e.getPlayer();
            ColoredParticle particle = particles.get(clickedItem.getTag(ARROW_PARTICLE));

            player.setTag(OITC.PLAYER_ARROW_PARTICLE, particle.particle());
            player.setTag(OITC.PLAYER_ARROW_COLOR, particle.color());
            player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1f, 1f));
            player.closeInventory();
        }
    }

    public static void updateAmmoDisplay(Player player, int amount) {
        ItemStack item = ItemStack.of(Material.TIPPED_ARROW);

        player.getInventory().setItemStack(7, OITC.arrow.withAmount(amount));
    }

    public record ColoredParticle(Color color, Particle particle) {}
}
