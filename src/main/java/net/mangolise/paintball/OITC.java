package net.mangolise.paintball;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mangolise.gamesdk.BaseGame;
import net.mangolise.gamesdk.log.Log;
import net.mangolise.paintball.weapon.Weapon;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket;
import net.minestom.server.tag.Tag;

import java.util.*;
import java.util.stream.Collectors;

public class OITC extends BaseGame<OITC.Config> {

    public OITC(Config config) {
        super(config);
    }

    @Override
    public void setup() {
        super.setup();

        Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("worlds/fruit"));
        instance.enableAutoChunkLoad(true);

        Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, e->{
            e.setSpawningInstance(instance);
        });

    }

    @Override
    public List<Feature<?>> features() {
        return List.of();
    }

    public record Config() {
    }
}
