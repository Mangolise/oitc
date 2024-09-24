package net.mangolise.oitc.events;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class KillEvent implements PlayerEvent {
    private final Player player;
    private final Player killer;
    private final int newKillCount;
    private final boolean isRevenge;

    public KillEvent(Player player, Player killer, int newKillCount, boolean isRevenge) {
        this.player = player;
        this.killer = killer;
        this.newKillCount = newKillCount;
        this.isRevenge = isRevenge;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public Player getKiller() {
        return killer;
    }

    public int getNewKillCount() {
        return newKillCount;
    }

    public boolean isRevenge() {
        return isRevenge;
    }
}
