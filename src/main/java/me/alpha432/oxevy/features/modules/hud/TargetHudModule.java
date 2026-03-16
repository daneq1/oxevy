package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.client.HudModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.AnimationUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.text.DecimalFormat;

public class TargetHudModule extends HudModule {

    private final Setting<Boolean> showHealth = bool("Health", true);
    private final Setting<Boolean> showDistance = bool("Distance", true);
    private final Setting<Boolean> showArmor = bool("Armor", true);
    private final Setting<Boolean> showPing = bool("Ping", true);
    private final Setting<Boolean> showName = bool("Name", true);
    private final Setting<Boolean> showHealthBar = bool("HealthBar", true);
    private final Setting<Boolean> showArmorBar = bool("ArmorBar", true);
    private final Setting<Boolean> showHead = bool("Head", true);
    private final Setting<Boolean> showItems = bool("Items", true);
    private final Setting<Boolean> smoothAnimations = bool("SmoothAnimations", true);
    private final Setting<Color> healthColor = color("HealthColor", 255, 0, 0, 255);
    private final Setting<Color> armorColor = color("ArmorColor", 0, 120, 255, 255);

    private Entity target = null;
    private float targetAlpha = 0.0f;
    private float lastHealth = 0.0f;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    public TargetHudModule() {
        super("TargetHUD", "Shows information about your target", 180, 60);
    }

    @Override
    protected void render(Render2DEvent e) {
        super.render(e);

        GuiGraphics ctx = e.getContext();
        float x = getX();
        float y = getY();

        Entity newTarget = findTarget();
        
        if (newTarget != target) {
            target = newTarget;
            if (smoothAnimations.getValue()) {
                targetAlpha = 0.0f;
            }
        }

        float targetAlphaTarget = target != null ? 1.0f : 0.0f;
        targetAlpha = AnimationUtil.animate(targetAlpha, targetAlphaTarget, 
            smoothAnimations.getValue() ? 0.15f : 1.0f, AnimationUtil.Easing.EASE_OUT);

        if (target == null || targetAlpha < 0.01f) {
            setWidth(180);
            setHeight(50);
            return;
        }

        int maxWidth = 180;
        int height = 60;
        int barWidth = maxWidth - 40;
        int alpha = (int) (targetAlpha * 255);

        int bgColor = (alpha << 24) | 0x222222;
        ctx.fill((int) x, (int) y, (int) x + maxWidth, (int) y + height, bgColor);

        // Draw player head placeholder (colored square)
        if (showHead.getValue() && target instanceof Player player) {
            int headColor = Oxevy.friendManager.isFriend(player) ? 0xFF00FF00 : 0xFFFFFFFF;
            ctx.fill((int) x + 5, (int) y + 5, (int) x + 35, (int) y + 35, (alpha << 24) | headColor);
            ctx.drawString(mc.font, "Head", (int) x + 8, (int) y + 12, (alpha << 24) | 0x000000);
        }

        int textStartX = showHead.getValue() ? (int) x + 40 : (int) x + 5;

        if (showName.getValue()) {
            String name = target.getName().getString();
            ctx.drawString(mc.font, name, textStartX, (int) y + 3, 
                (alpha << 24) | 0xFFFFFF);
        }

        if (showHealthBar.getValue() && target instanceof LivingEntity living) {
            float health = living.getHealth();
            float maxHealth = living.getMaxHealth();
            float healthPercent = Math.max(0, Math.min(1, health / maxHealth));
            
            ctx.fill(textStartX, (int) y + 15, textStartX + barWidth, (int) y + 23, 
                (alpha << 24) | 0x444444);
            
            int hpColor = healthColor.getValue().getRGB();
            int hpAlpha = (alpha << 24) | (hpColor & 0x00FFFFFF);
            int hpBarWidth = (int) (barWidth * healthPercent);
            ctx.fill(textStartX, (int) y + 15, 
                textStartX + hpBarWidth, (int) y + 23, hpAlpha);
        }

        if (showArmorBar.getValue() && target instanceof Player player) {
            float armor = player.getArmorValue();
            float armorPercent = Math.min(1, armor / 20f);
            
            ctx.fill(textStartX, (int) y + 25, textStartX + barWidth, (int) y + 33, 
                (alpha << 24) | 0x444444);
            
            int armorColorValue = armorColor.getValue().getRGB();
            int armorAlpha = (alpha << 24) | (armorColorValue & 0x00FFFFFF);
            int armorBarWidth = (int) (barWidth * armorPercent);
            ctx.fill(textStartX, (int) y + 25, 
                textStartX + armorBarWidth, (int) y + 33, armorAlpha);
        }

        // Draw items/armor
        if (showItems.getValue() && target instanceof Player player) {
            int itemY = (int) y + 38;
            int itemX = textStartX;
            
            // Draw equipment slots (helmet, chestplate, leggings, boots, mainhand, offhand)
            ItemStack[] items = {
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET),
                player.getMainHandItem(),
                player.getOffhandItem()
            };
            
            for (int i = 0; i < items.length; i++) {
                if (!items[i].isEmpty()) {
                    ctx.renderItem(items[i], itemX, itemY);
                    ctx.renderItemDecorations(mc.font, items[i], itemX, itemY);
                    itemX += 16;
                }
            }
        }

        // Draw info text
        int infoY = (int) y + 38;
        if (!showItems.getValue() || !(target instanceof Player)) {
            String infoText = "";

            if (showHealth.getValue() && target instanceof LivingEntity living) {
                infoText += "HP: " + DECIMAL_FORMAT.format(living.getHealth()) + "  ";
            }

            if (showDistance.getValue()) {
                double dist = mc.player.distanceTo(target);
                infoText += "Dist: " + DECIMAL_FORMAT.format(dist) + "m  ";
            }

            if (showArmor.getValue() && target instanceof Player player) {
                infoText += "Armor: " + (int) player.getArmorValue() + "  ";
            }

            if (showPing.getValue() && target instanceof Player player) {
                int ping = 0;
                try {
                    ping = mc.getConnection().getPlayerInfo(player.getUUID()) != null ? 
                        mc.getConnection().getPlayerInfo(player.getUUID()).getLatency() : 0;
                } catch (Exception ignored) {}
                infoText += "Ping: " + ping + "ms";
            }

            ctx.drawString(mc.font, infoText, textStartX, infoY, (alpha << 24) | 0xAAAAAA);
        }

        setWidth((int) maxWidth);
        setHeight((int) height);
    }

    private Entity findTarget() {
        Entity entity = mc.crosshairPickEntity;
        if (entity != null && entity.isAlive()) {
            return entity;
        }

        if (target != null && target.isAlive()) {
            return target;
        }

        return null;
    }
}
