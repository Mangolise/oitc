package net.mangolise.oitc.features;

import net.mangolise.gamesdk.Game;
import net.mangolise.gamesdk.util.GameSdkUtils;
import net.mangolise.oitc.OITC;
import net.mangolise.oitc.abilities.AbilityType;
import net.mangolise.oitc.abilities.PlayerDashAbility;
import net.mangolise.oitc.abilities.PlayerSpeedAbility;
import net.mangolise.oitc.abilities.PlayerTeleportAbility;
import net.mangolise.oitc.events.KillEvent;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.tag.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AbilitiesFeature implements Game.Feature<OITC> {
    public static final Tag<Boolean> PLAYER_CAN_USE_ABILITY = Tag.Boolean("player_ability").defaultValue(true);
    public static final Tag<CompletableFuture<Void>> PLAYER_CURRENT_ABILITY = Tag.Transient("player_current_ability");
    public static final Tag<AbilityType> PLAYER_SELECTED_ABILITY = Tag.<AbilityType>Transient("player_selected_ability").defaultValue(AbilityType.DASH);
    public static Map<UUID, CompletableFuture<Void>> abilityCountDown = new HashMap<>();

    @Override
    public void setup(Context<OITC> context) {
        context.eventNode().addListener(PlayerSpawnEvent.class, e -> {
            e.getPlayer().setExp(1);
        });

        context.eventNode().addListener(KillEvent.class, e -> {
            playerAbilityReset(e.getPlayer(), true);
            playerAbilityReset(e.getKiller(), false);
        });

        context.eventNode().addListener(PlayerSwapItemEvent.class, e -> {
            switch (e.getPlayer().getTag(PLAYER_SELECTED_ABILITY)) {
                case DASH -> PlayerDashAbility.playerDashAbility(e);
                case SPEED -> PlayerSpeedAbility.playerSpeedAbility(e);
                case TELEPORT -> PlayerTeleportAbility.playerTeleportAbility(e);
            }
        });
    }

    public void playerAbilityReset(Player player, boolean cancelAbility) {
        if (cancelAbility && player.hasTag(AbilitiesFeature.PLAYER_CURRENT_ABILITY)) {
            CompletableFuture<Void> sprintDuration = player.getTag(AbilitiesFeature.PLAYER_CURRENT_ABILITY);
            if (!sprintDuration.isDone()) {
                sprintDuration.cancel(true);
                player.setExp(1);
                player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
            }
        }

        player.setTag(PLAYER_CAN_USE_ABILITY, true);
        if (abilityCountDown.containsKey(player.getUuid())) {
            CompletableFuture<Void> timer = abilityCountDown.get(player.getUuid());
            timer.complete(null);

            AbilityType abilityType = player.getTag(AbilitiesFeature.PLAYER_SELECTED_ABILITY);
            String cooldown = switch (abilityType) {
                case TELEPORT -> "teleport";
                case SPEED -> "speedability";
                case DASH -> "dash";
            };

            GameSdkUtils.stopCooldown(player, cooldown);

            if (cancelAbility && abilityType == AbilityType.SPEED) {
                GameSdkUtils.stopCooldown(player, "speed");
            }
        }
    }
}
