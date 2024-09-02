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
import net.mangolise.gamesdk.util.Timer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;

import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OITC extends BaseGame<OITC.Config> {

    public OITC(Config config) {
        super(config);
    }

    public static final Tag<Integer> PLAYERS_AMMO_TAG = Tag.Integer("player_ammo").defaultValue(1);
    public static final Tag<Particle> PLAYER_ARROW_PARTICLE = Tag.<Particle>Transient("particle").defaultValue(ParticleMenu.particles.getFirst().particle());
    public static final Tag<Color> PLAYER_ARROW_COLOR = Tag.<Color>Transient("player_arrow_color").defaultValue(ParticleMenu.particles.getFirst().color());
    public static final Tag<Integer> PLAYER_KILL_STREAK = Tag.Integer("kill_streak").defaultValue(0);
    public static final Tag<Boolean> PARTICLE_MENU_IS_OPEN = Tag.Boolean("particle_menu_is_open").defaultValue(false);
    public static final Tag<Sidebar> PLAYER_SIDEBAR = Tag.Transient("player_sidebar");

    public static final ItemStack crossbow = ItemStack.of(Material.CROSSBOW)
            .withCustomName(Component.text("Crossbow").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD));
    public static final ItemStack arrow = ItemStack.of(Material.ARROW)
            .withCustomName(Component.text("Arrow").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD));
    public static final ItemStack chargedCrossbow = crossbow.with(ItemComponent.CHARGED_PROJECTILES, List.of(arrow))
            .withCustomName(Component.text("Crossbow").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD));

    Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer(GameSdkUtils.getPolarLoaderFromResource("worlds/fruit.polar"));
    Map<UUID, CompletableFuture<Void>> arrowCountdown = new HashMap<>();
    Map<UUID, Integer> kills = new HashMap<>();

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

            kills.put(player.getUuid(), 0);

            Sidebar sidebar = new Sidebar(Component.text("One in the Chamber").decorate(TextDecoration.BOLD).color(TextColor.color(255, 172, 0)));
            sidebar.addViewer(player);
            player.setTag(PLAYER_SIDEBAR, sidebar);
            for (Player player1 : instance.getPlayers()) {
                ScoreboardFeature.updateSidebar(player1, instance, kills);
            }

            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1000);

            player.setRespawnPoint(GameSdkUtils.getSpawnPosition(instance));
            player.teleport(randomSpawn().add(0, 1, 0));

            player.getInventory().addItemStack(chargedCrossbow);
            player.getInventory().addItemStack(ItemStack.of(Material.IRON_SWORD)
                    .withCustomName(Component.text("Iron Sword").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY)));
            player.getInventory().setItemStack(8, ItemStack.of(Material.CHEST)
                    .withCustomName(Component.text("Particle Menu").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA)));

            setAmmo(e.getPlayer(), 1);
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, e -> {
            kills.remove(e.getPlayer().getUuid());
            for (Player player1 : instance.getPlayers()) {
                ScoreboardFeature.updateSidebar(player1, instance, kills);
            }
        });
        MinecraftServer.getGlobalEventHandler().addListener(ItemDropEvent.class, e -> e.setCancelled(true));

        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, e -> {
            if (e.getInventory() != null && e.getInventory().getTag(PARTICLE_MENU_IS_OPEN)) {
                e.setCancelled(true);
            }

            if (e.getClickedItem().material().equals(Material.CHEST)) {
                ParticleMenu.openMenu(e.getPlayer());
                e.setCancelled(true);
                return;
            }

            if (e.getClickedItem().material().equals(Material.ARROW)) {
                e.setCancelled(true);
            }

            ParticleMenu.handlePreClickEvent(e);
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, e -> {
            Player player = e.getPlayer();
            ItemStack heldItem = e.getItemStack();

            if (heldItem.material().equals(Material.CHEST)) {
                ParticleMenu.openMenu(player);
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

            instance.playSound(Sound.sound(SoundEvent.ITEM_CROSSBOW_SHOOT, Sound.Source.PLAYER, 2f, 1f), player.getPosition());

            setAmmo(player, player.getTag(PLAYERS_AMMO_TAG) - 1);
        });

        MinecraftServer.getGlobalEventHandler().addListener(ProjectileCollideWithEntityEvent.class, e -> {
            if (!(e.getEntity() instanceof ArrowEntity arrowEntity)) return;
            if (!(e.getTarget() instanceof Player player)) return;

            if (arrowEntity.getShooter() instanceof Player shooter) {
                attacked(player, shooter, false);
            }
            e.getEntity().remove();
        });

        MinecraftServer.getGlobalEventHandler().addListener(EntityAttackEvent.class, e -> {
            Entity entity = e.getTarget();

            if (entity instanceof Player player && e.getEntity() instanceof Player attacker && attacker.getItemInMainHand().material() == Material.IRON_SWORD) {
                attacked(player, attacker, true);
            }
        });
    }

    public void attacked(Player victim, Player attacker, boolean fromSword) {
        // player spawn is above y level 22
        if (victim.getPosition().y() > 22.0 || attacker.getPosition().y() > 22.0 ||
                victim.getGameMode().equals(GameMode.SPECTATOR) || attacker.getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }

        int killCount = kills.get(attacker.getUuid()) + 1;
        victim.setTag(PLAYER_KILL_STREAK, 0);
        attacker.updateTag(PLAYER_KILL_STREAK, streak -> streak + 1);

        // instead of killing player, this fakes players death by teleporting them.
        KillMessages.sendDeathMessage(instance, victim, attacker, fromSword);
        victim.setGameMode(GameMode.SPECTATOR);
        victim.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_DEATH, Sound.Source.PLAYER, 1f, 1f));

        Timer.countDownForPlayer(3, victim).thenRun(() -> {
            victim.setGameMode(GameMode.ADVENTURE);
            victim.teleport(randomSpawn());
        });

        attacker.playSound(Sound.sound(SoundEvent.BLOCK_AMETHYST_BLOCK_BREAK, Sound.Source.PLAYER, 1f, 2f));
        setAmmo(attacker, attacker.getTag(PLAYERS_AMMO_TAG) + 1);

        setAmmo(victim, 1);

        kills.put(attacker.getUuid(), killCount);

        for (Player player : instance.getPlayers()) {
            ScoreboardFeature.updateSidebar(player, instance, kills);
        }

        int killStreak = attacker.getTag(PLAYER_KILL_STREAK);
        KillMessages.sendKillStreakMessage(killStreak, attacker, instance);

        KillEvent killEvent = new KillEvent(victim, attacker, killCount);
        EventDispatcher.call(killEvent);

        Particle particle = Particle.POOF;
        poof(particle, victim, 0.1f);
    }

    public void setAmmo(Player player, int amount) {
        player.setTag(PLAYERS_AMMO_TAG, amount);

        // cancels a timer if the player gets a kill.
        if (arrowCountdown.containsKey(player.getUuid())) {
            CompletableFuture<Void> timer = arrowCountdown.get(player.getUuid());
            timer.complete(null);
            player.sendActionBar(Component.text());
        }

        if (amount <= 0) {
            CompletableFuture<Void> timer = Timer.countDown(10, i -> {
                player.sendActionBar(Component.text(i).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
            });
            timer.thenRun(() -> {
                setAmmo(player, amount + 1);
                player.playSound(Sound.sound(SoundEvent.ITEM_CROSSBOW_QUICK_CHARGE_3, Sound.Source.PLAYER, 1f, 2f));
            });
            arrowCountdown.put(player.getUuid(), timer);
            player.getInventory().setItemStack(findCrossbow(player), crossbow);
        } else {
            player.getInventory().setItemStack(findCrossbow(player), chargedCrossbow);
        }

        ParticleMenu.updateAmmoDisplay(player, amount);
    }

    public int findCrossbow(Player player) {
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItemStack(i).material() == Material.CROSSBOW) {
                return i;
            }
        }

        return  -1;
    }

    public Pos randomSpawn() {
        Random random = new Random();

        List<Pos> spawnPositions = List.of(
                new Pos(-0.5, 29, 36.5, 180, 0),
                new Pos(-0.5, 29, -43.5, 0, 0),
                new Pos(38.5, 29, -0.5, 90, 0),
                new Pos(-45.5, 28, -0.5, -90, 0)
        );

        return spawnPositions.get(random.nextInt(0, 4));
    }

    public void poof(Particle particle, Player victim, float ExplosionSpeed) {
        Pos playerPos = victim.getPosition();

        ParticlePacket packet = new ParticlePacket(particle, true, playerPos.x(), playerPos.y() + 1.5, playerPos.z(), 0, 0, 0, ExplosionSpeed, 30);
        instance.sendGroupedPacket(packet);
    }

    @Override
    public List<Feature<?>> features() {
        return List.of(new AdminCommandsFeature(), new NoCollisionFeature(), new AbilitiesFeature(), new ScoreboardFeature());
    }

    public record Config() {
    }
}
