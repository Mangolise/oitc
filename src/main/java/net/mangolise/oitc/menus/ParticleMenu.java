package net.mangolise.oitc.menus;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.util.ChatUtil;
import net.mangolise.gamesdk.util.Timer;
import net.mangolise.oitc.DisplayArrowEntity;
import net.mangolise.oitc.OITC;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.PotionType;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
            new ColoredParticle(new Color(245, 171, 86), Particle.TRIAL_SPAWNER_DETECTION),
            new ColoredParticle(new Color(77, 178, 250), Particle.TRIAL_SPAWNER_DETECTION_OMINOUS),
            new ColoredParticle(new Color(255, 141, 10), Particle.WAX_ON),
            new ColoredParticle(new Color(50, 50, 50), Particle.MYCELIUM),
            new ColoredParticle(new Color(117, 219, 240), Particle.SCULK_CHARGE_POP),
            new ColoredParticle(new Color(255, 255, 225), Particle.ENCHANT),
            new ColoredParticle(new Color(100, 50, 226), Particle.WARPED_SPORE),
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
        Inventory inventory = new Inventory(InventoryType.CHEST_5_ROW, "Particle Menu");
        inventory.setTag(OITC.MENU_ID, "particle_menu");
        Particle playerParticle = player.getTag(OITC.PLAYER_ARROW_PARTICLE);

        for (int i = 0, j = 0; j < particles.size(); i++) {
            int row = i % 9;
            if (row == 0 || row == 8 || (i / 9 == 4 && row == 1)) {
                continue;
            }

            ColoredParticle coloredParticle = particles.get(j);
            Particle particle = coloredParticle.particle();
            boolean glowing = particle.equals(playerParticle);

            Color color = coloredParticle.color();

            boolean unlocked = player.hasPermission("oitc.particle." + particle.key().value());
            if (!unlocked) {
                color = new Color(150, 150, 150);
            }

            ItemStack arrow = makeColoredArrow(particle, color).withTag(ARROW_PARTICLE, j).withGlowing(glowing);

            Component lore = Component.text("Right-Click ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD)
                    .append(Component.text("to preview").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GREEN));

            if (unlocked) {
                arrow = arrow.withLore(lore);
            } else {
                arrow = arrow.withLore(lore, Component.text("Currently Locked").decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.DARK_GRAY));
            }

            inventory.setItemStack(i, arrow);
            j++;
        }

        player.playSound(Sound.sound(SoundEvent.BLOCK_CHEST_OPEN, Sound.Source.PLAYER, 0.5f, 1f));
        player.openInventory(inventory);
    }

    public static void handlePreClickEvent(InventoryPreClickEvent e) {
        if (!"particle_menu".equals(e.getInventory().getTag(OITC.MENU_ID))) {
            return;
        }

        ItemStack clickedItem = e.getClickedItem();

        if (!clickedItem.material().equals(Material.TIPPED_ARROW) || !clickedItem.hasTag(ARROW_PARTICLE)) {
            return;
        }

        e.setCancelled(true);

        Player player = e.getPlayer();
        ColoredParticle particle = particles.get(clickedItem.getTag(ARROW_PARTICLE));

        if (e.getClickType().equals(ClickType.RIGHT_CLICK) && e.getPlayer().getPosition().y() > 22.0 && e.getPlayer().getPosition().y() < 60) {
            preview(player, particle);
            player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1f, 1f));
            return;
        }

        if (!player.hasPermission("oitc.particle." + particle.particle().key().value())) {
            return;
        }

        if (e.getClickType().equals(ClickType.RIGHT_CLICK)) {
            return;
        } else {
            player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1f, 1f));
        }
        player.setTag(OITC.PLAYER_ARROW_PARTICLE, particle.particle());
        player.setTag(OITC.PLAYER_ARROW_COLOR, particle.color());
        player.closeInventory();
        updateAmmoDisplay(player, player.getTag(OITC.PLAYERS_AMMO_TAG));
    }

    public static void updateAmmoDisplay(Player player, int amount) {
        ItemStack arrow = makeColoredArrow(player.getTag(OITC.PLAYER_ARROW_PARTICLE), player.getTag(OITC.PLAYER_ARROW_COLOR)).withAmount(amount);

        player.getInventory().setItemStack(7, arrow);
    }

    private static ItemStack makeColoredArrow(Particle particle, Color color) {
        return ItemStack.of(Material.TIPPED_ARROW).with(ItemComponent.POTION_CONTENTS, new PotionContents(PotionType.AWKWARD, color)).withoutExtraTooltip()
                .withCustomName(makeArrowName(particle, color));
    }

    private static Component makeArrowName(Particle particle, Color color) {
        return Component.text(ChatUtil.capitaliseFirstLetter(particle.key().value().replace('_', ' ')))
                .decoration(TextDecoration.ITALIC, false).color(TextColor.color(color));
    }

    public static void preview(Player player, ColoredParticle particle) {
        Pos pos = new Pos(1000, 150, 0);
        final Pos originalPos = player.getPosition();

        player.setInvisible(true);
        player.setFlying(true);
        player.setAllowFlying(true);
        player.teleport(pos);
        player.closeInventory();

        player.playSound(Sound.sound(SoundEvent.ENTITY_ENDERMAN_TELEPORT, Sound.Source.PLAYER, 1f, 1f));
        final Pos spawnPosition = new Pos(1035, 146.5, 6.5);

        CompletableFuture<Void> timer = Timer.countDownForPlayer(1, player);
        timer.thenRun(() -> {
            DisplayArrowEntity arrow = new DisplayArrowEntity(player, particle);
            arrow.updateViewableRule(viewer -> viewer.getUuid().equals(player.getUuid()));
            arrow.setInstance(player.getInstance(), spawnPosition);
            arrow.setVelocity(new Vec(-60, 15.0, 0));
        });

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            player.setInvisible(false);
            player.setFlying(false);
            player.setAllowFlying(false);
            player.teleport(originalPos);
            player.playSound(Sound.sound(SoundEvent.ENTITY_ENDERMAN_TELEPORT, Sound.Source.PLAYER, 1f, 1f));
        }, TaskSchedule.seconds(3), TaskSchedule.stop());
    }

    public record ColoredParticle(Color color, Particle particle) {}
}
