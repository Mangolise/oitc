package net.mangolise.oitc;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.particle.Particle;

import static net.mangolise.oitc.OITC.PLAYER_ARROW_PARTICLE;

public class ParticleCommand extends Command {
    public ParticleCommand() {
        super("selectarrowparticle");
        var names = Particle.values().stream().map(Particle::name).map(str -> str.replace("minecraft:", "")).toList();
        addSyntax(this::execute, ArgumentType.Word("particle").from(names.toArray(String[]::new)));
    }

    private void execute(CommandSender sender, CommandContext context) {
        String particle = context.get("particle");
        particle = "minecraft:" + particle;
        for (Particle value : Particle.values()) {
            if (particle.equalsIgnoreCase(value.name())) {
                sender.setTag(PLAYER_ARROW_PARTICLE, value);
                break;
            }
        }
    }
}
