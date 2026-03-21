package me.alpha432.oxevy.features.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.alpha432.oxevy.event.impl.input.KeyInputEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.commands.Command;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Bind;
import me.alpha432.oxevy.manager.CommandManager;
import me.alpha432.oxevy.util.KeyboardUtil;

import static me.alpha432.oxevy.features.commands.argument.KeyArgumentType.getKey;
import static me.alpha432.oxevy.features.commands.argument.ModuleArgumentType.getModule;
import static me.alpha432.oxevy.features.commands.argument.ModuleArgumentType.module;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

public class BindCommand extends Command {
    private Module pendingModule;

    public BindCommand() {
        super("bind", "setbind");
        setDescription("Sets a key bind for a module");
        EVENT_BUS.register(this);
    }

    @Override
    public void createArgumentBuilder(LiteralArgumentBuilder<CommandManager> builder) {
        builder.then(argument("module", module(true))
                .then(argument("key", me.alpha432.oxevy.features.commands.argument.KeyArgumentType.key())
                        .executes((ctx) -> {
                            Module module = getModule(ctx, "module");
                            int keyCode = getKey(ctx, "key");
                            module.bind.setValue(new Bind(keyCode));
                            return success("{green}%s {reset}bind set to {green}%s",
                                    module.getName(),
                                    KeyboardUtil.getKeyName(keyCode));
                        }))
                .executes((ctx) -> {
                    pendingModule = getModule(ctx, "module");
                    return success("{yellow}Press any key to bind... (ESC to cancel)");
                }));
    }

    @Subscribe
    public void onKey(final KeyInputEvent event) {
        if (nullCheck() || pendingModule == null || event.getKey() == GLFW_KEY_UNKNOWN) {
            return;
        }

        if (event.getKey() == GLFW_KEY_ESCAPE) {
            pendingModule = null;
            sendMessage("Operation canceled.");
            return;
        }

        sendMessage("{green}%s {reset}bind set to {green}%s",
                pendingModule.getName(),
                KeyboardUtil.getKeyName(event.getKey()));
        pendingModule.bind.setValue(new Bind(event.getKey()));
        pendingModule = null;
    }
}
