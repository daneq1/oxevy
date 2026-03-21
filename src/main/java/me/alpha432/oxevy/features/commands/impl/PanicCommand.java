package me.alpha432.oxevy.features.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.commands.Command;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.manager.CommandManager;

public class PanicCommand extends Command {
    public PanicCommand() {
        super("panic", "panik");
        setDescription("Disables all modules");
    }

    @Override
    public void createArgumentBuilder(LiteralArgumentBuilder<CommandManager> builder) {
        builder.executes((ctx) -> {
            int count = 0;
            for (Module module : Oxevy.moduleManager.getModules()) {
                if (module.isEnabled()) {
                    module.disable();
                    count++;
                }
            }
            return success("{red} Disabled %s module(s)", count);
        });
    }
}
