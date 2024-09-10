package net.mangolise.oitc.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.mangolise.gamesdk.features.commands.MangoliseCommand;
import net.mangolise.gamesdk.util.ChatUtil;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.particle.Particle;

import static net.mangolise.oitc.OITC.PLAYER_ARROW_PARTICLE;

public class ParticleCommand extends MangoliseCommand {

    public ParticleCommand() {
        super("selectarrowparticle");
        var names = Particle.values().stream().map(Particle::name).map(str -> str.replace("minecraft:", "")).toList();
        addSyntax(this::execute, ArgumentType.Word("particle").from(names.toArray(String[]::new)));
    }

    private void execute(CommandSender sender, CommandContext context) {
        String particle = context.get("particle");
        String particleLabel = particle;
        particle = "minecraft:" + particle;
        for (Particle value : Particle.values()) {
            if (particle.equalsIgnoreCase(value.name())) {
                sender.setTag(PLAYER_ARROW_PARTICLE, value);
                sender.sendMessage(Component.text("Selected particle: ").decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.GREEN).append(Component.text(ChatUtil.snakeCaseToTitleCase(particleLabel))
                                .color(NamedTextColor.GOLD)));
                break;
            }
        }
    }

    @Override
    protected String getPermission() {
        return "oitc.commands.particle";
    }
}
