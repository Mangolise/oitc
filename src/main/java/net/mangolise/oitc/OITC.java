package net.mangolise.oitc;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.BaseGame;
import net.mangolise.gamesdk.features.AdminCommandsFeature;
import net.mangolise.gamesdk.features.NoCollisionFeature;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.mangolise.gamesdk.util.Timer;
import net.minestom.server.MinecraftServer;
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
import java.util.concurrent.atomic.AtomicInteger;

public class OITC extends BaseGame<OITC.Config> {

    public OITC(Config config) {
        super(config);
    }

    static final Tag<Integer> PLAYERS_AMMO_TAG = Tag.Integer("player_ammo").defaultValue(1);
    public static final Tag<Particle> PLAYER_ARROW_PARTICLE = Tag.<Particle>Transient("particle").defaultValue(Particle.CRIT);

    Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer(GameSdkUtils.getPolarLoaderFromResource("worlds/fruit.polar"));
    ItemStack crossbow = ItemStack.of(Material.CROSSBOW);
    ItemStack arrow = ItemStack.of(Material.ARROW);
    ItemStack chargedCrossbow = crossbow.with(ItemComponent.CHARGED_PROJECTILES, List.of(arrow));
    Map<UUID, CompletableFuture<Void>> arrowCountdown = new HashMap<>();

    Sidebar sidebar = new Sidebar(Component.text("Scoreboard"));
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

            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1000);

            player.setRespawnPoint(GameSdkUtils.getSpawnPosition(instance));
            player.teleport(randomSpawn().add(0, 1, 0));

            player.getInventory().addItemStack(chargedCrossbow);
            player.getInventory().addItemStack(ItemStack.of(Material.IRON_SWORD));

            setAmmo(e.getPlayer(), 1);
            sidebar.addViewer(player);
            kills.put(player.getUuid(), 0);
            updateSidebar();
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, e -> kills.remove(e.getPlayer().getUuid()));
        MinecraftServer.getGlobalEventHandler().addListener(ItemDropEvent.class, e -> e.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSwapItemEvent.class, e -> {
            if (e.getPlayer().getHeldSlot() == 8) {
                e.setCancelled(true);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, e -> {
            if (e.getSlot() == 8) {
                e.setCancelled(true);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, e -> {
            Player player = e.getPlayer();
            ItemStack heldItem = e.getItemStack();

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
                attacked(player, shooter);
            }
            e.getEntity().remove();
        });

        MinecraftServer.getGlobalEventHandler().addListener(EntityAttackEvent.class, e -> {
            Entity entity = e.getTarget();

            if (entity instanceof Player player && e.getEntity() instanceof Player attacker && attacker.getItemInMainHand().material() == Material.IRON_SWORD) {
                attacked(player, attacker);
            }
        });
    }


    public void attacked(Player victim, Player attacker) {
        if (victim.getPosition().y() > 22.0 || attacker.getPosition().y() > 22.0 ||
                victim.getGameMode().equals(GameMode.SPECTATOR) || attacker.getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }

        victim.sendMessage("You were Killed!");
        victim.setGameMode(GameMode.SPECTATOR);
        victim.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_DEATH, Sound.Source.PLAYER, 1f, 1f));

        Timer.countDownForPlayer(3, victim).thenRun(() -> {
            victim.setGameMode(GameMode.ADVENTURE);
            victim.teleport(randomSpawn());
        });

        attacker.playSound(Sound.sound(SoundEvent.BLOCK_AMETHYST_BLOCK_BREAK, Sound.Source.PLAYER, 1f, 2f));
        setAmmo(attacker, attacker.getTag(PLAYERS_AMMO_TAG) + 1);

        setAmmo(victim, 1);

        kills.put(attacker.getUuid(), kills.get(attacker.getUuid()) + 1);
        updateSidebar();

        KillEvent killEvent = new KillEvent(victim, attacker, kills.get(attacker.getUuid()));
        EventDispatcher.call(killEvent);

        Particle particle = Particle.POOF;
        poof(particle, victim, 0.1f);
    }

    public void setAmmo(Player player, int amount) {
        player.setTag(PLAYERS_AMMO_TAG, amount);

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

        player.getInventory().setItemStack(8, arrow.withAmount(amount));
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


    public void updateSidebar() {
        Set<Map.Entry<UUID, Integer>> killSet = kills.entrySet();
        sidebar.getLines().forEach(line -> sidebar.removeLine(line.getId()));
        AtomicInteger i = new AtomicInteger();
        killSet.stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).limit(12).forEach(entry -> {
            Player player = instance.getPlayerByUuid(entry.getKey());
            assert player != null;
            sidebar.createLine(new Sidebar.ScoreboardLine(i.toString(), Component.text(player.getUsername() + ": " + entry.getValue()), i.get()));
            i.getAndIncrement();
        });
    }


    public void poof(Particle particle, Player victim, float ExplosionSpeed) {
        Pos playerPos = victim.getPosition();

        ParticlePacket packet = new ParticlePacket(particle, true, playerPos.x(), playerPos.y() + 1.5, playerPos.z(), 0, 0, 0, ExplosionSpeed, 30);
        instance.sendGroupedPacket(packet);
    }


    @Override
    public List<Feature<?>> features() {
        return List.of(new AdminCommandsFeature(), new NoCollisionFeature(), new AbilitiesFeature());
    }


    public record Config() {
    }
}
