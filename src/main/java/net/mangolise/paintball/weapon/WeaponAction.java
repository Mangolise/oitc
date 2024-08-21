package net.mangolise.paintball.weapon;

import net.mangolise.gamesdk.entity.ProjectileEntity;
import net.mangolise.paintball.OITC;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public interface WeaponAction {
    void execute(Context context);

    interface Context {
        OITC game();
        Player player();
        Instance instance();

        Pos eyePosition();

        default void shootProjectile(Block display, double scale, double speed) {
            ProjectileEntity projectile = new ProjectileEntity(display, scale);
            projectile.setInstance(instance(), eyePosition());

            // add some velocity
            Vec velocity = eyePosition().direction().mul(speed);
            projectile.setVelocity(velocity);
        }
    }
}
