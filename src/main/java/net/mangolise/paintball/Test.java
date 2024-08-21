package net.mangolise.paintball;

import net.kyori.adventure.text.format.NamedTextColor;
import net.mangolise.gamesdk.limbo.Limbo;
import net.mangolise.gamesdk.util.Util;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.bungee.BungeeCordProxy;

import java.util.List;
import java.util.Map;
import java.util.Set;

// This is a dev server, not used in production
public class Test {
    public static void main(String[] args) {

        MinecraftServer server = MinecraftServer.init();
        MinecraftServer.getConnectionManager().setUuidProvider((connection, username) -> Util.createFakeUUID(username));

        OITC.Config config = new OITC.Config();
        OITC game = new OITC(config);
        game.setup();



        if (Util.useBungeeCord()) {
            BungeeCordProxy.enable();
        }

        server.start("0.0.0.0", Util.getConfiguredPort());
    }
}
