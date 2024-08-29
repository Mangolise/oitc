package net.mangolise.oitc;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.particle.Particle;

import java.util.Set;

import static net.mangolise.oitc.OITC.PLAYER_ARROW_PARTICLE;

public class ParticleCommand extends Command {
    private static final Set<Particle> particleSelection = Set.of(
            Particle.TOTEM_OF_UNDYING,
            Particle.WITCH,
            Particle.CHERRY_LEAVES,
            Particle.SPORE_BLOSSOM_AIR,
            Particle.CRIMSON_SPORE,
            Particle.BUBBLE_COLUMN_UP,
            Particle.DRAGON_BREATH,
            Particle.GLOW,
            Particle.FLAME,
            Particle.TRIAL_SPAWNER_DETECTION,
            Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
            Particle.WAX_ON,
            Particle.MYCELIUM,
            Particle.SCULK_CHARGE_POP,
            Particle.ENCHANT,
            Particle.WARPED_SPORE,
            Particle.VIBRATION,
            Particle.ELECTRIC_SPARK,
            Particle.WHITE_ASH,
            Particle.END_ROD,
            Particle.DRIPPING_LAVA,
            Particle.DRIPPING_WATER,
            Particle.DRIPPING_HONEY,
            Particle.DRIPPING_OBSIDIAN_TEAR,
            Particle.FIREWORK,
            Particle.RAIN,
            Particle.FISHING,
            Particle.INFESTED,
            Particle.LAVA,
            Particle.REVERSE_PORTAL,
            Particle.SMALL_FLAME,
            Particle.SMALL_GUST,
            Particle.CRIT
    );

    public ParticleCommand() {
        super("selectarrowparticle");
        var names = particleSelection.stream().map(Particle::name).map(str -> str.replace("minecraft:", "")).toList();
        addSyntax(this::execute, ArgumentType.Word("particle").from(names.toArray(String[]::new)));
    }

    private void execute(CommandSender sender, CommandContext context) {
        String particle = context.get("particle");
        particle = "minecraft:" + particle;
        for (Particle value : particleSelection) {
            if (particle.equalsIgnoreCase(value.name())) {
                sender.setTag(PLAYER_ARROW_PARTICLE, value);
                break;
            }
        }
    }
}
