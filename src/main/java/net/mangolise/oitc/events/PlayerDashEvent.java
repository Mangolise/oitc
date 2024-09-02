package net.mangolise.oitc.events;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

public class PlayerDashEvent implements Event {
    private final Player player;

    public PlayerDashEvent(Player player) {
        this.player = player;
    }

    public Player player() {
        return player;
    }
}
