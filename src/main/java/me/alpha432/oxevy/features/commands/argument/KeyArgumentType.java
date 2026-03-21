package me.alpha432.oxevy.features.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.alpha432.oxevy.features.commands.CommandExceptions;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class KeyArgumentType implements ArgumentType<Integer> {
    private static final Map<String, Integer> KEY_MAP = new HashMap<>();
    
    static {
        for (int i = GLFW.GLFW_KEY_A; i <= GLFW.GLFW_KEY_Z; i++) {
            KEY_MAP.put(String.valueOf((char) ('a' + i - GLFW.GLFW_KEY_A)), i);
        }
        for (int i = GLFW.GLFW_KEY_0; i <= GLFW.GLFW_KEY_9; i++) {
            KEY_MAP.put(String.valueOf((char) ('0' + i - GLFW.GLFW_KEY_0)), i);
        }
        KEY_MAP.put("SPACE", GLFW.GLFW_KEY_SPACE);
        KEY_MAP.put("LEFT SHIFT", GLFW.GLFW_KEY_LEFT_SHIFT);
        KEY_MAP.put("RIGHT SHIFT", GLFW.GLFW_KEY_RIGHT_SHIFT);
        KEY_MAP.put("LEFT CONTROL", GLFW.GLFW_KEY_LEFT_CONTROL);
        KEY_MAP.put("RIGHT CONTROL", GLFW.GLFW_KEY_RIGHT_CONTROL);
        KEY_MAP.put("LEFT ALT", GLFW.GLFW_KEY_LEFT_ALT);
        KEY_MAP.put("RIGHT ALT", GLFW.GLFW_KEY_RIGHT_ALT);
        KEY_MAP.put("TAB", GLFW.GLFW_KEY_TAB);
        KEY_MAP.put("CAPS LOCK", GLFW.GLFW_KEY_CAPS_LOCK);
        KEY_MAP.put("ENTER", GLFW.GLFW_KEY_ENTER);
        KEY_MAP.put("ESCAPE", GLFW.GLFW_KEY_ESCAPE);
        KEY_MAP.put("ESC", GLFW.GLFW_KEY_ESCAPE);
        KEY_MAP.put("BACKSPACE", GLFW.GLFW_KEY_BACKSPACE);
        KEY_MAP.put("INSERT", GLFW.GLFW_KEY_INSERT);
        KEY_MAP.put("DELETE", GLFW.GLFW_KEY_DELETE);
        KEY_MAP.put("HOME", GLFW.GLFW_KEY_HOME);
        KEY_MAP.put("END", GLFW.GLFW_KEY_END);
        KEY_MAP.put("PAGE UP", GLFW.GLFW_KEY_PAGE_UP);
        KEY_MAP.put("PAGE DOWN", GLFW.GLFW_KEY_PAGE_DOWN);
        KEY_MAP.put("UP", GLFW.GLFW_KEY_UP);
        KEY_MAP.put("DOWN", GLFW.GLFW_KEY_DOWN);
        KEY_MAP.put("LEFT", GLFW.GLFW_KEY_LEFT);
        KEY_MAP.put("RIGHT", GLFW.GLFW_KEY_RIGHT);
        KEY_MAP.put("F1", GLFW.GLFW_KEY_F1);
        KEY_MAP.put("F2", GLFW.GLFW_KEY_F2);
        KEY_MAP.put("F3", GLFW.GLFW_KEY_F3);
        KEY_MAP.put("F4", GLFW.GLFW_KEY_F4);
        KEY_MAP.put("F5", GLFW.GLFW_KEY_F5);
        KEY_MAP.put("F6", GLFW.GLFW_KEY_F6);
        KEY_MAP.put("F7", GLFW.GLFW_KEY_F7);
        KEY_MAP.put("F8", GLFW.GLFW_KEY_F8);
        KEY_MAP.put("F9", GLFW.GLFW_KEY_F9);
        KEY_MAP.put("F10", GLFW.GLFW_KEY_F10);
        KEY_MAP.put("F11", GLFW.GLFW_KEY_F11);
        KEY_MAP.put("F12", GLFW.GLFW_KEY_F12);
        KEY_MAP.put("MINUS", GLFW.GLFW_KEY_MINUS);
        KEY_MAP.put("EQUAL", GLFW.GLFW_KEY_EQUAL);
        KEY_MAP.put("SLASH", GLFW.GLFW_KEY_SLASH);
        KEY_MAP.put("BACKSLASH", GLFW.GLFW_KEY_BACKSLASH);
        KEY_MAP.put("SEMICOLON", GLFW.GLFW_KEY_SEMICOLON);
        KEY_MAP.put("APOSTROPHE", GLFW.GLFW_KEY_APOSTROPHE);
        KEY_MAP.put("GRAVE", GLFW.GLFW_KEY_GRAVE_ACCENT);
        KEY_MAP.put("COMMA", GLFW.GLFW_KEY_COMMA);
        KEY_MAP.put("PERIOD", GLFW.GLFW_KEY_PERIOD);
        KEY_MAP.put("BRACKET LEFT", GLFW.GLFW_KEY_LEFT_BRACKET);
        KEY_MAP.put("BRACKET RIGHT", GLFW.GLFW_KEY_RIGHT_BRACKET);
        KEY_MAP.put("NONE", GLFW.GLFW_KEY_UNKNOWN);
        KEY_MAP.put("NONE", 0);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        String keyName = reader.readString().toUpperCase().replace(" ", "_");
        
        if (KEY_MAP.containsKey(keyName)) {
            return KEY_MAP.get(keyName);
        }
        
        if (keyName.length() == 1) {
            char c = keyName.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                return GLFW.GLFW_KEY_A + (c - 'A');
            }
            if (c >= '0' && c <= '9') {
                return GLFW.GLFW_KEY_0 + (c - '0');
            }
        }
        
        throw CommandExceptions.invalidArgument("Unknown key: " + keyName).create();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase().replace("_", " ");
        
        for (Map.Entry<String, Integer> entry : KEY_MAP.entrySet()) {
            String key = entry.getKey().replace("_", " ");
            if (key.contains(input)) {
                builder.suggest(key);
            }
        }
        
        return builder.buildFuture();
    }

    public static KeyArgumentType key() {
        return new KeyArgumentType();
    }

    public static int getKey(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, Integer.class);
    }
}
