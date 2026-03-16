package me.alpha432.oxevy.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.alpha432.oxevy.features.Feature;
import me.alpha432.oxevy.features.settings.Bind;
import me.alpha432.oxevy.features.settings.EnumConverter;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.traits.Jsonable;
import net.fabricmc.loader.api.FabricLoader;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("ConfigManager");
    private static final Path OXEVEY_PATH = FabricLoader.getInstance().getGameDir().resolve("oxevy");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<Jsonable> jsonables = new LinkedList<>();
    private String currentConfig = "default";

    public void addConfig(Jsonable jsonable) {
        jsonables.add(jsonable);
    }

    public void load() {
        load(currentConfig);
    }

    public void load(String configName) {
        mkdirs();
        Path configPath = OXEVEY_PATH.resolve(configName);
        for (Jsonable jsonable : jsonables) {
            try {
                Path filePath = configPath.resolve(jsonable.getFileName());
                if (Files.exists(filePath)) {
                    String read = Files.readString(filePath);
                    jsonable.fromJson(JsonParser.parseString(read));
                }
            } catch (Throwable e) {
                LOGGER.error("Failed to load config: {}", configName, e);
            }
        }
        currentConfig = configName;
        LOGGER.info("Loaded config: {}", configName);
    }

    public void save() {
        save(currentConfig);
    }

    public void save(String configName) {
        mkdirs();
        Path configPath = OXEVEY_PATH.resolve(configName);
        if (!configPath.toFile().exists()) {
            configPath.toFile().mkdirs();
        }
        for (Jsonable jsonable : jsonables) {
            try {
                JsonElement json = jsonable.toJson();
                Files.writeString(configPath.resolve(jsonable.getFileName()), GSON.toJson(json));
            } catch (Throwable e) {
                LOGGER.error("Failed to save config: {}", configName, e);
            }
        }
        currentConfig = configName;
        LOGGER.info("Saved config: {}", configName);
    }

    public void delete(String configName) {
        if (configName.equals("default")) {
            LOGGER.warn("Cannot delete default config");
            return;
        }
        Path configPath = OXEVEY_PATH.resolve(configName);
        if (configPath.toFile().exists()) {
            deleteDirectory(configPath.toFile());
            LOGGER.info("Deleted config: {}", configName);
        }
    }

    private void deleteDirectory(java.io.File dir) {
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    public List<String> getConfigList() {
        List<String> configs = new ArrayList<>();
        if (OXEVEY_PATH.toFile().exists() && OXEVEY_PATH.toFile().isDirectory()) {
            File[] files = OXEVEY_PATH.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        configs.add(file.getName());
                    }
                }
            }
        }
        if (!configs.contains("default")) {
            configs.add(0, "default");
        }
        return configs;
    }

    public String getCurrentConfig() {
        return currentConfig;
    }

    public Path getConfigFolder() {
        return OXEVEY_PATH;
    }

    private void mkdirs() {
        if (!OXEVEY_PATH.toFile().exists()) {
            boolean success = OXEVEY_PATH.toFile().mkdirs();
            if (!success) {
                throw new RuntimeException("Failed to create needed directories!");
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setValueFromJson(Feature feature, Setting setting, JsonElement element) {
        if (element == null || element.isJsonNull()) return;
        switch (setting.getType()) {
            case "Boolean" -> setting.setValue(element.getAsBoolean());
            case "Double" -> setting.setValue(element.getAsDouble());
            case "Float" -> setting.setValue(element.getAsFloat());
            case "Integer" -> setting.setValue(element.getAsInt());
            case "String" -> setting.setValue(element.getAsString().replace("_", " "));
            case "Bind" -> setting.setValue(new Bind(element.getAsInt()));
            case "Color" -> {
                try {
                    String colorStr = element.getAsString();
                    String[] parts = colorStr.split(",");
                    if (parts.length == 4) {
                        int r = Integer.parseInt(parts[0]);
                        int g = Integer.parseInt(parts[1]);
                        int b = Integer.parseInt(parts[2]);
                        int a = Integer.parseInt(parts[3]);
                        setting.setValue(new Color(r, g, b, a));
                    }
                } catch (Exception exception) {
                    LOGGER.error("Error parsing color for: {} : {}", feature.getName(), setting.getName());
                }
            }
            case "Pos" -> {
                try {
                    String posStr = element.getAsString();
                    String[] parts = posStr.split(",");
                    if (parts.length == 2) {
                        float x = Float.parseFloat(parts[0]);
                        float y = Float.parseFloat(parts[1]);
                        setting.setValue(new Vector2f(x, y));
                    }
                } catch (Exception exception) {
                    LOGGER.error("Error parsing position for: {} : {}", feature.getName(), setting.getName());
                }
            }
            case "Enum" -> {
                try {
                    EnumConverter converter = new EnumConverter(setting.getValue().getClass());
                    Enum value = converter.doBackward(element);
                    setting.setValue(value);
                } catch (Exception exception) {
                    LOGGER.error("Error parsing enum for {}.{}: {}", feature.getName(), setting.getName(), exception);
                }
            }
            default -> LOGGER.error("Unknown Setting type for: {} : {}", feature.getName(), setting.getName());
        }
    }
}
