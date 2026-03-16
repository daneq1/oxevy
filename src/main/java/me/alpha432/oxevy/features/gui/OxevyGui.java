package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.Feature;
import me.alpha432.oxevy.features.gui.items.Item;
import me.alpha432.oxevy.features.gui.items.buttons.ModuleButton;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.util.render.AnimationUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import me.alpha432.oxevy.util.ColorUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class OxevyGui extends Screen {
    private static OxevyGui INSTANCE;
    private static Color colorClipboard = null;

    static {
        INSTANCE = new OxevyGui();
    }

    private final ArrayList<Widget> widgets = new ArrayList<>();
    
    // Animation values for GUI open/close
    private float guiOpenAnimation = 0.0f;
    private boolean wasOpen = false;
    
    // Search bar
    private boolean searchOpen = false;
    private float searchAnimation = 0.0f;

    public OxevyGui() {
        super(Component.literal("Oxevy"));
        setInstance();
        load();
    }

    public static OxevyGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OxevyGui();
        }
        return INSTANCE;
    }

    public static OxevyGui getClickGui() {
        return OxevyGui.getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    private void load() {
        int x = -84;
        for (Module.Category category : Oxevy.moduleManager.getCategories()) {
            if (category == Module.Category.HUD) continue;
            Widget panel = new Widget(category.getName(), x += 90, 4, true);
            Oxevy.moduleManager.stream()
                    .filter(m -> m.getCategory() == category && !m.hidden)
                    .map(ModuleButton::new)
                    .forEach(panel::addButton);
            this.widgets.add(panel);
        }
        this.widgets.forEach(components -> components.getItems().sort(Comparator.comparing(Feature::getName)));
    }

    private void applySearchFilter() {
        String searchQuery = ClickGuiModule.getInstance().searchBar.getValue().toLowerCase();
        boolean hasSearch = !searchQuery.isEmpty();
        
        for (Widget widget : this.widgets) {
            boolean hasVisibleItems = false;
            for (Item item : widget.getItems()) {
                if (item instanceof ModuleButton button) {
                    boolean matches = !hasSearch || button.getModule().getName().toLowerCase().contains(searchQuery);
                    item.setHidden(!matches);
                    if (matches) hasVisibleItems = true;
                }
            }
            widget.setHidden(hasSearch && !hasVisibleItems);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Item.context = context;
        
        // Update GUI open animation
        float animSpeed = ClickGuiModule.getInstance().animationsEnabled.getValue() ? 
            ClickGuiModule.getInstance().animationSpeed.getValue() : 1.0f;
        guiOpenAnimation = AnimationUtil.animate(guiOpenAnimation, 1.0f, animSpeed, AnimationUtil.Easing.EASE_OUT_BACK);
        
        // Apply fade animation
        float alpha = ClickGuiModule.getInstance().animationsEnabled.getValue() ? guiOpenAnimation : 1.0f;
        
        // Draw semi-transparent background with fade animation
        int bgColor = new Color(0, 0, 0, (int)(120 * alpha)).hashCode();
        context.fill(0, 0, context.guiWidth(), context.guiHeight(), bgColor);
        
        // Draw search bar (vanilla style - appears when Ctrl+F is pressed)
        if (searchOpen) {
            String searchQuery = ClickGuiModule.getInstance().searchBar.getValue();
            ClickGuiModule search = ClickGuiModule.getInstance();
            
            int sWidth = search.searchWidth.getValue();
            int sXOffset = search.searchXOffset.getValue();
            int sYOffset = search.searchYOffset.getValue();
            int searchX = context.guiWidth() - sWidth - sXOffset;
            int searchY = sYOffset;
            
            // Get colors from settings
            Color bgColorSetting = search.searchBgColor.getValue();
            Color textColorSetting = search.searchTextColor.getValue();
            Color placeholderColorSetting = search.searchPlaceholderColor.getValue();
            
            int searchBgColor = (bgColorSetting.getAlpha() << 24) | (bgColorSetting.getRed() << 16) | (bgColorSetting.getGreen() << 8) | bgColorSetting.getBlue();
            int searchInnerBgColor = ((bgColorSetting.getAlpha() - 40) << 24) | ((bgColorSetting.getRed() - 20) << 16) | ((bgColorSetting.getGreen() - 20) << 8) | (bgColorSetting.getBlue() - 20);
            int searchTextColor = (textColorSetting.getAlpha() << 24) | (textColorSetting.getRed() << 16) | (textColorSetting.getGreen() << 8) | textColorSetting.getBlue();
            int searchPlaceholderColor = (placeholderColorSetting.getAlpha() << 24) | (placeholderColorSetting.getRed() << 16) | (placeholderColorSetting.getGreen() << 8) | placeholderColorSetting.getBlue();
            
            // Search background
            context.fill(searchX - 2, searchY - 2, searchX + sWidth + 2, searchY + 18, searchBgColor);
            context.fill(searchX, searchY, searchX + sWidth, searchY + 14, searchInnerBgColor);
            
            // Search icon
            if (search.searchShowIcon.getValue()) {
                context.drawString(minecraft.font, "🔍", searchX + 4, searchY + 2, searchTextColor);
            }
            
            // Search text
            String displayText = searchQuery.isEmpty() ? "Filter" : searchQuery;
            int txtColor = searchQuery.isEmpty() ? searchPlaceholderColor : searchTextColor;
            int textOffset = search.searchShowIcon.getValue() ? 16 : 4;
            context.drawString(minecraft.font, displayText, searchX + textOffset, searchY + 2, txtColor);
            
            applySearchFilter();
        } else {
            applySearchFilter();
        }
        
        // Draw widgets with animation
        this.widgets.forEach(components -> {
            components.drawScreen(context, mouseX, mouseY, delta);
        });
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        // Handle toggle button click (always active)
        if (click.button() == 0) {
            int searchWidth = 200;
            int searchX = minecraft.getWindow().getGuiScaledWidth() / 2 - searchWidth / 2;
            int searchY = 15;
            int btnWidth = 20;
            int btnHeight = 14;
            int btnX = searchX + searchWidth + 4;
            
            if (click.x() >= btnX && click.x() <= btnX + btnWidth && 
                click.y() >= searchY && click.y() <= searchY + btnHeight) {
                ClickGuiModule.getInstance().searchBarEnabled.setValue(
                    !ClickGuiModule.getInstance().searchBarEnabled.getValue());
                return true;
            }
        }
        
        this.widgets.forEach(components -> components.mouseClicked((int) click.x(), (int) click.y(), click.button()));
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        this.widgets.forEach(components -> components.mouseReleased((int) click.x(), (int) click.y(), click.button()));
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) {
            this.widgets.forEach(component -> component.setY(component.getY() - 10));
        } else if (verticalAmount > 0) {
            this.widgets.forEach(component -> component.setY(component.getY() + 10));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        // Handle Ctrl+F to toggle search
        boolean ctrlPressed = GLFW.glfwGetKey(minecraft.getWindow().handle(), GLFW.GLFW_KEY_LEFT_CONTROL) == 1 || 
                              GLFW.glfwGetKey(minecraft.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == 1;
        
        if (ctrlPressed && input.key() == GLFW.GLFW_KEY_F) {
            searchOpen = !searchOpen;
            if (!searchOpen) {
                ClickGuiModule.getInstance().searchBar.setValue("");
            }
            return true;
        }
        
        // If search is not open, don't process search-related keys
        if (!searchOpen) {
            this.widgets.forEach(component -> component.onKeyPressed(input.input()));
            return super.keyPressed(input);
        }
        
        String searchQuery = ClickGuiModule.getInstance().searchBar.getValue();
        
        // Handle backspace (key code 259 is backspace)
        if (input.key() == 259 && !searchQuery.isEmpty()) {
            ClickGuiModule.getInstance().searchBar.setValue(searchQuery.substring(0, searchQuery.length() - 1));
            return true;
        }
        
        // Handle escape to close search
        if (input.key() == 256) {
            searchOpen = false;
            ClickGuiModule.getInstance().searchBar.setValue("");
            return true;
        }
        
        this.widgets.forEach(component -> component.onKeyPressed(input.input()));
        return super.keyPressed(input);
    }
    
    @Override
    public boolean charTyped(CharacterEvent input) {
        // Only allow typing if search is open
        if (!searchOpen) {
            this.widgets.forEach(component -> component.onKeyTyped(input.codepointAsString(), input.modifiers()));
            return super.charTyped(input);
        }
        
        String searchQuery = ClickGuiModule.getInstance().searchBar.getValue();
        String character = input.codepointAsString();
        
        if (!character.isEmpty() && searchQuery.length() < 50) {
            ClickGuiModule.getInstance().searchBar.setValue(searchQuery + character);
            return true;
        }
        
        this.widgets.forEach(component -> component.onKeyTyped(input.codepointAsString(), input.modifiers()));
        return super.charTyped(input);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }//ignore 1.21.8 blur thing

    public final ArrayList<Widget> getComponents() {
        return this.widgets;
    }

    public int getTextOffset() {
        return -6;
    }

    public static Color getColorClipboard() {
        return colorClipboard;
    }

    public static void setColorClipboard(Color color) {
        colorClipboard = color;
    }
}
