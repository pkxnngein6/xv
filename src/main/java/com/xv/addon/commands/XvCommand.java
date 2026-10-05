package com.xv.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class XvCommand extends Command {
    public XvCommand() {
        super("xv", "Shows info about XV ADDON.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            info("XV ADDON v1.0.0 - modules: death-coords, hunger-warning, spawner-notifier");
            return SINGLE_SUCCESS;
        });
    }
}
