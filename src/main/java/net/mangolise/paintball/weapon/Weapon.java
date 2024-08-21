package net.mangolise.paintball.weapon;

import net.kyori.adventure.text.Component;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

import java.util.Locale;

public enum Weapon {
    GOOP_DE_GOOP(
            Component.text("Goop de Goop"),
            Material.SLIME_BALL,
            context -> context.shootProjectile(Block.SLIME_BLOCK, 0.1, 10)
    ),
    ;

    private final Component displayName;
    private final ItemStack displayItem;
    private final WeaponAction action;

    private static final Tag<String> WEAPON_ID_TAG = Tag.String("weaponId");

    Weapon(Component displayName, Material displayMaterial, WeaponAction action) {
        this.displayName = displayName;
        this.displayItem = ItemStack.of(displayMaterial)
                .withCustomName(displayName)
                .withTag(Tag.String("weaponId"), name().toLowerCase(Locale.ROOT));
        this.action = action;
    }

    public static Weapon weaponFromItemStack(ItemStack itemStack) {
        return valueOf(itemStack.getTag(Tag.String("weaponId")).toUpperCase(Locale.ROOT));
    }

    public Component displayName() {
        return displayName;
    }

    public ItemStack displayItem() {
        return displayItem;
    }

    public WeaponAction action() {
        return action;
    }
}
