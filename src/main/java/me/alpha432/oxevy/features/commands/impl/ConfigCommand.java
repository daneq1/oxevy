package me.alpha432.oxevy.features.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.commands.Command;
import me.alpha432.oxevy.manager.ConfigManager;
import me.alpha432.oxevy.manager.CommandManager;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config");
        setDescription("Manage multiple configs");
    }

    @Override
    public void createArgumentBuilder(LiteralArgumentBuilder<CommandManager> builder) {
        builder.executes(context -> {
            ConfigManager config = Oxevy.configManager;
            List<String> configs = config.getConfigList();
            String current = config.getCurrentConfig();
            
            StringBuilder sb = new StringBuilder();
            sb.append("{gray}Available configs: {white}");
            for (int i = 0; i < configs.size(); i++) {
                String c = configs.get(i);
                sb.append(c);
                if (c.equals(current)) {
                    sb.append(" {green}[active]");
                }
                if (i < configs.size() - 1) sb.append(", ");
            }
            return success(sb.toString());
        });
        
        builder.then(literal("save")
                .executes(context -> {
                    Oxevy.configManager.save();
                    return success("{green} Config saved!");
                }));
        
        builder.then(literal("save")
                .then(argument("name", word())
                        .executes(context -> {
                            String name = getString(context, "name");
                            Oxevy.configManager.save(name);
                            return success("{green} Saved config: {white}" + name);
                        })));
        
        builder.then(literal("load")
                .executes(context -> {
                    Oxevy.configManager.load();
                    return success("{green} Config loaded!");
                }));
        
        builder.then(literal("load")
                .then(argument("name", word())
                        .executes(context -> {
                            String name = getString(context, "name");
                            Oxevy.configManager.load(name);
                            return success("{green} Loaded config: {white}" + name);
                        })));
        
        builder.then(literal("list")
                .executes(context -> {
                    ConfigManager config = Oxevy.configManager;
                    List<String> configs = config.getConfigList();
                    String current = config.getCurrentConfig();
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("{gray}Configs: {white}");
                    for (int i = 0; i < configs.size(); i++) {
                        String c = configs.get(i);
                        sb.append(c);
                        if (c.equals(current)) {
                            sb.append(" {green}[*]");
                        }
                        if (i < configs.size() - 1) sb.append(", ");
                    }
                    return success(sb.toString());
                }));
        
        builder.then(literal("delete")
                .then(argument("name", word())
                        .executes(context -> {
                            String name = getString(context, "name");
                            if (name.equals("default")) {
                                return success("{red} Cannot delete default config!");
                            }
                            Oxevy.configManager.delete(name);
                            return success("{green} Deleted config: {white}" + name);
                        })));
    }
}
