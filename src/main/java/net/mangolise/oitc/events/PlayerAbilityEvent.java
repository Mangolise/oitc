package net.mangolise.oitc.events;

import net.mangolise.oitc.abilities.AbilityType;
import net.mangolise.oitc.features.AbilitiesFeature;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerAbilityEvent implements PlayerEvent {
    private final Player player;
    private final AbilityType abilityType;

    public PlayerAbilityEvent(Player player) {
        this.player = player;
        abilityType = player.getTag(AbilitiesFeature.PLAYER_SELECTED_ABILITY);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public AbilityType getAbilityType() {
        return abilityType;
    }
}
