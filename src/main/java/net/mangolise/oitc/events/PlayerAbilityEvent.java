package net.mangolise.oitc.events;

import net.mangolise.oitc.abilities.AbilityType;
import net.mangolise.oitc.features.AbilitiesFeature;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerAbilityEvent implements PlayerEvent {
    private final Player player;
    private final AbilityType abilityType;
    private final int cooldown;

    public PlayerAbilityEvent(Player player, int cooldown) {
        this.player = player;
        this.cooldown = cooldown;
        abilityType = player.getTag(AbilitiesFeature.PLAYER_SELECTED_ABILITY);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public AbilityType getAbilityType() {
        return abilityType;
    }

    public int getCooldown() {
        return cooldown;
    }
}
