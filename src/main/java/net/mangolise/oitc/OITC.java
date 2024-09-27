package net.mangolise.oitc;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.BaseGame;
import net.mangolise.gamesdk.features.AdminCommandsFeature;
import net.mangolise.gamesdk.features.NoCollisionFeature;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.mangolise.oitc.commands.ParticleCommand;
import net.mangolise.oitc.features.AbilitiesFeature;
import net.mangolise.oitc.features.AttackedFeature;
import net.mangolise.oitc.features.ScoreboardFeature;
import net.mangolise.oitc.menus.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class OITC extends BaseGame<OITC.Config> {

    public OITC(Config config) {
        super(config);
    }

    public static final Tag<Integer> PLAYERS_AMMO_TAG = Tag.Integer("player_ammo").defaultValue(1);
    public static final Tag<Particle> PLAYER_ARROW_PARTICLE = Tag.<Particle>Transient("particle").defaultValue(ParticleMenu.particles.getFirst().particle());
    public static final Tag<Color> PLAYER_ARROW_COLOR = Tag.<Color>Transient("player_arrow_color").defaultValue(ParticleMenu.particles.getFirst().color());
    public static final Tag<Integer> PLAYER_KILL_STREAK = Tag.Integer("kill_streak").defaultValue(0);
    public static final Tag<Integer> PLAYER_HIGHEST_KILL_STREAK = Tag.Integer("highest_kill_streak").defaultValue(0);
    public static final Tag<String> MENU_ID = Tag.String("menu_id");
    public static final Tag<Sidebar> PLAYER_SIDEBAR = Tag.Transient("player_sidebar");
    public static final Tag<Integer> PLAYER_KILLS = Tag.Integer("player_kills").defaultValue(0);
    public static final Tag<Integer> PLAYER_CROSSBOW_KILLS = Tag.Integer("player_crossbow_kills").defaultValue(0);
    public static final Tag<Integer> PLAYER_SWORD_KILLS = Tag.Integer("player_sword_kills").defaultValue(0);
    public static final Tag<Integer> PLAYER_DEATHS = Tag.Integer("player_deaths").defaultValue(0);
    public static final Tag<Integer> PLAYER_DEATHS_BY_SWORD = Tag.Integer("player_deaths_by_sword").defaultValue(0);
    public static final Tag<Integer> PLAYER_DEATHS_BY_CROSSBOW = Tag.Integer("player_deaths_by_crossbow").defaultValue(0);
    public static final Tag<Integer> PLAYER_REVENGE_KILLS = Tag.Integer("player_revenge_kills").defaultValue(0);
    public static final Tag<Integer> PLAYER_KILLS_PER_SESSION = Tag.Integer("player_kills_per_session").defaultValue(0);
    public static final Tag<UUID> PLAYER_LAST_KILLER = Tag.UUID("player_last_killer");

    public static final ItemStack crossbow = ItemStack.of(Material.CROSSBOW)
            .withCustomName(Component.text("Crossbow").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD));
    public static final ItemStack arrow = ItemStack.of(Material.ARROW)
            .withCustomName(Component.text("Arrow").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD));
    public static final ItemStack chargedCrossbow = crossbow.with(ItemComponent.CHARGED_PROJECTILES, List.of(arrow))
            .withCustomName(Component.text("Crossbow").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD));

    public static Map<UUID, CompletableFuture<Void>> arrowCountdown = new HashMap<>();

    Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer(GameSdkUtils.getPolarLoaderFromResource("worlds/fruit.polar"));

    @Override
    public void setup() {
        super.setup();

        instance.enableAutoChunkLoad(true);

        MinecraftServer.getCommandManager().register(new ParticleCommand());

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, e -> {
            Player player = e.getPlayer();

            e.setSpawningInstance(instance);
            instance.setTimeRate(0);

            player.setGameMode(GameMode.ADVENTURE);
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, e -> {
            Player player = e.getPlayer();

            Sidebar sidebar = new Sidebar(Component.text("One in the Chamber").decorate(TextDecoration.BOLD).color(TextColor.color(255, 172, 0)));
            sidebar.addViewer(player);
            player.setTag(PLAYER_SIDEBAR, sidebar);
            for (Player player1 : instance.getPlayers()) {
                ScoreboardFeature.updateSidebar(player1);
            }

            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1000);

            player.setRespawnPoint(GameSdkUtils.getSpawnPosition(instance));
            player.teleport(randomSpawn().add(0, 1, 0));

            player.getInventory().addItemStack(chargedCrossbow);
            player.getInventory().addItemStack(ItemStack.of(Material.IRON_SWORD)
                    .withCustomName(Component.text("Iron Sword").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY)));
            player.getInventory().setItemStack(8, ItemStack.of(Material.COMPASS)
                    .withCustomName(Component.text("OITC Menu").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

            AttackedFeature.setAmmo(e.getPlayer(), 1);
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, e -> {
            e.getPlayer().removeTag(OITC.PLAYER_KILLS_PER_SESSION);
            for (Player player1 : instance.getPlayers()) {
                ScoreboardFeature.updateSidebar(player1);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(ItemDropEvent.class, e -> e.setCancelled(true));

        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, e -> {
            if (e.getClickedItem().material().equals(Material.TIPPED_ARROW) || e.getClickedItem().material().equals(Material.COMPASS)) {
                e.setCancelled(true);
            }

            if (e.getInventory() == null) {
                return;
            }

            if (e.getInventory().hasTag(MENU_ID)) {
                e.setCancelled(true);
            }

            OitcMenu.handlePreClickEvent(e);
            ParticleMenu.handlePreClickEvent(e);
            SpawnMenu.handlePreClickEvent(e, e.getPlayer());
            AbilitiesMenu.handlePreClickEvent(e, e.getPlayer());
            UtilitiesMenu.handlePreClickEvent(e, e.getPlayer());
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, e -> {
            Player player = e.getPlayer();
            ItemStack heldItem = e.getItemStack();

            if (heldItem.material().equals(Material.CROSSBOW) && player.getItemInMainHand().material().equals(Material.COMPASS)) {
                e.setCancelled(true);
                return;
            }

            if (heldItem.material().equals(Material.COMPASS)) {
                OitcMenu.openMenu(player);
                e.setCancelled(true);
                return;
            }

            if (heldItem.material() != Material.CROSSBOW || player.getTag(PLAYERS_AMMO_TAG) <= 0) {
                return;
            }

            Pos spawnPosition = new Pos(player.getPosition().add(0, 1.5, 0));

            ArrowEntity arrow = new ArrowEntity(player);
            arrow.setInstance(instance, spawnPosition);
            arrow.setVelocity(player.getPosition().direction().mul(75));

            instance.playSound(Sound.sound(SoundEvent.ITEM_CROSSBOW_SHOOT, Sound.Source.PLAYER, 3f, 1f), player.getPosition());

            AttackedFeature.setAmmo(player, player.getTag(PLAYERS_AMMO_TAG) - 1);
        });

        MinecraftServer.getGlobalEventHandler().addListener(ProjectileCollideWithEntityEvent.class, e -> {
            if (!(e.getEntity() instanceof ArrowEntity arrowEntity)) return;
            if (!(e.getTarget() instanceof Player player)) return;

            if (arrowEntity.getShooter() instanceof Player shooter) {
                AttackedFeature.attacked(player, shooter, false, instance);
            }
            e.getEntity().remove();
        });

        MinecraftServer.getGlobalEventHandler().addListener(EntityAttackEvent.class, e -> {
            Entity entity = e.getTarget();

            if (!(entity instanceof Player victim) || !(e.getEntity() instanceof Player attacker)) {
                return;
            }

            if (victim.getPosition().y() > 22.0 || attacker.getPosition().y() > 22.0 ||
                    victim.getGameMode().equals(GameMode.SPECTATOR) || attacker.getGameMode().equals(GameMode.SPECTATOR)) {
                return;
            }

            if (attacker.getItemInMainHand().material() == Material.IRON_SWORD) {
                AttackedFeature.attacked(victim, attacker, true, instance);
            }
        });
    }

    public static Pos randomSpawn() {
        Random random = new Random();

        List<Pos> spawnPositions = List.of(
                new Pos(-0.5, 29, -42.5, 0, 0),
                new Pos(37.5, 29, -0.5, 90, 0),
                new Pos(-0.5, 29, 36.5, 180, 0),
                new Pos(-44.5, 28, -0.5, -90, 0)
        );

        return spawnPositions.get(random.nextInt(0, 4));
    }

    @Override
    public List<Feature<?>> features() {
        return List.of(new AdminCommandsFeature(), new NoCollisionFeature(), new AbilitiesFeature(), new ScoreboardFeature(), new AttackedFeature());
    }

    public record Config(String serverIp) {
    }
}
