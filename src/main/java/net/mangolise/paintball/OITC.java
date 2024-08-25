package net.mangolise.paintball;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.mangolise.gamesdk.BaseGame;
import net.mangolise.gamesdk.util.Util;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OITC extends BaseGame<OITC.Config> {

    public OITC(Config config) {
        super(config);
    }

    static final Tag<Integer> PLAYERS_AMMO_TAG = Tag.Integer("player_ammo").defaultValue(1);
    static final Tag<Boolean> PLAYER_INVINCIBLE = Tag.Boolean("player_invincibility").defaultValue(false);

    Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("worlds/fruit"));
    ItemStack crossbow = ItemStack.of(Material.CROSSBOW);
    ItemStack arrow = ItemStack.of(Material.ARROW);
    ItemStack chargedCrossbow = crossbow.with(ItemComponent.CHARGED_PROJECTILES, List.of(arrow));

    Sidebar sidebar = new Sidebar(Component.text("Scoreboard"));
    Map<UUID, Integer> kills = new HashMap<>();

    @Override
    public void setup() {
        super.setup();

        instance.enableAutoChunkLoad(true);

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, e -> {
            Player player = e.getPlayer();

            e.setSpawningInstance(instance);

            player.setGameMode(GameMode.ADVENTURE);
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, e -> {
            Player player = e.getPlayer();

            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1000);

            player.teleport(Util.getSpawnPosition(instance));
            player.setRespawnPoint(Util.getSpawnPosition(instance));

            player.getInventory().addItemStack(chargedCrossbow);
            player.getInventory().addItemStack(ItemStack.of(Material.IRON_SWORD));

            setAmmo(e.getPlayer(), 1);
            sidebar.addViewer(player);
            kills.put(player.getUuid(), 0);
            updateSidebar();
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, e -> kills.remove(e.getPlayer().getUuid()));
        MinecraftServer.getGlobalEventHandler().addListener(ItemDropEvent.class, e -> e.setCancelled(true));

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

            Entity arrow = new EntityProjectile(player, EntityType.ARROW);
            arrow.setInstance(instance, spawnPosition);

            arrow.setVelocity(player.getPosition().direction().mul(75));

            setAmmo(player, player.getTag(PLAYERS_AMMO_TAG) - 1);
        });

        MinecraftServer.getGlobalEventHandler().addListener(ProjectileCollideWithEntityEvent.class, e -> {
            Entity entity = e.getTarget();

            if (entity instanceof Player player) {
                if (e.getEntity() instanceof EntityProjectile projectile && projectile.getShooter() instanceof Player shooter) {
                    attacked(player, shooter);
                }
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
        if (victim.getTag(PLAYER_INVINCIBLE)) {
            return;
        }

        victim.sendMessage("You were Killed!");
        victim.setGameMode(GameMode.SPECTATOR);
        victim.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_DEATH, Sound.Source.PLAYER, 1f, 1f));

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            victim.setGameMode(GameMode.ADVENTURE);
            victim.teleport(randomSpawn().withView(victim.getPosition()));

            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                victim.setTag(PLAYER_INVINCIBLE, false);
            }, TaskSchedule.seconds(5), TaskSchedule.stop());
        }, TaskSchedule.seconds(3), TaskSchedule.stop());

        setAmmo(victim, 1);

        attacker.playSound(Sound.sound(SoundEvent.BLOCK_AMETHYST_BLOCK_BREAK, Sound.Source.PLAYER, 1f, 2f));
        setAmmo(attacker, attacker.getTag(PLAYERS_AMMO_TAG) + 1);

        kills.put(attacker.getUuid(), kills.get(attacker.getUuid()) + 1);
        updateSidebar();

        victim.setTag(PLAYER_INVINCIBLE, true);
    }

    public void setAmmo(Player player, int amount) {
        player.setTag(PLAYERS_AMMO_TAG, amount);
        if (amount <= 0) {
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

        int radius = 20;

        int posX = random.nextInt(radius);
        int posZ = random.nextInt(radius);
        int posY = Util.getHighestBlock(instance, posX, posZ) + 1;

        return new Pos(posX, posY, posZ);
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

    @Override
    public List<Feature<?>> features() {
        return List.of();
    }


    public record Config() {
    }
}
