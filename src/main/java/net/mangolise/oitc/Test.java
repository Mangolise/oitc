package net.mangolise.oitc;

import net.mangolise.gamesdk.util.GameSdkUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.bungee.BungeeCordProxy;

// This is a dev server, not used in production
public class Test {
    public static void main(String[] args) {

        MinecraftServer server = MinecraftServer.init();
        MinecraftServer.getConnectionManager().setUuidProvider((connection, username) -> GameSdkUtils.createFakeUUID(username));

        OITC.Config config = new OITC.Config();
        OITC game = new OITC(config);
        game.setup();

        if (GameSdkUtils.useBungeeCord()) {
            BungeeCordProxy.enable();
        }

        server.start("0.0.0.0", GameSdkUtils.getConfiguredPort());
    }
}
