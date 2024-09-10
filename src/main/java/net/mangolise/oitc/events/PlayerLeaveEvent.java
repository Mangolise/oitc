package net.mangolise.oitc.events;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

public class PlayerLeaveEvent implements Event {
    private final Player player;

    public PlayerLeaveEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
