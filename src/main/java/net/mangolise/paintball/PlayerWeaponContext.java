package net.mangolise.paintball;

import net.mangolise.paintball.weapon.WeaponAction;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public record PlayerWeaponContext(OITC game, Player player, Player target) implements WeaponAction.Context {

    @Override
    public Instance instance() {
        return player.getInstance();
    }

    @Override
    public Pos eyePosition() {
        return player.getPosition().add(0, player.getEyeHeight(), 0);
    }
}
