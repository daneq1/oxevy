package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.client.HudModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.manager.TargetManager;
import me.alpha432.oxevy.util.render.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.List;
import java.util.Locale;

public class TargetInfoHudModule extends HudModule {
    public final Setting<Boolean> showArmorDurability = bool("ArmorDurability", true);
    public final Setting<Boolean> showEffects = bool("PotionEffects", true);
    public final Setting<Boolean> showHeldItem = bool("HeldItem", true);
    public final Setting<Boolean> showPing = bool("Ping", true);
    public final Setting<Boolean> showCombatPrediction = bool("CombatPrediction", true);
    public final Setting<Boolean> showCombo = bool("ComboCounter", true);
    public final Setting<Boolean> glowEffect = bool("GlowEffect", true);
    public final Setting<Boolean> rangeCircle = bool("RangeCircle", true);
    public final Setting<Boolean> lineOfSight = bool("LineOfSight", true);
    public final Setting<Boolean> background = bool("Background", true);
    public final Setting<Integer> backgroundOpacity = num("BackgroundOpacity", 120, 0, 255);

    public TargetInfoHudModule() {
        super("TargetInfo", "Current target tracking and combat prediction", 160, 70);
        register(backgroundOpacity);
        backgroundOpacity.setVisibility((Integer i) -> background.getValue());
    }

    @Override
    protected void render(Render2DEvent e) {
        super.render(e);
        if (nullCheck()) return;

        TargetManager tm = Oxevy.targetManager;
        LivingEntity target = tm.getTarget();
        if (target == null) {
            setWidth(80);
            setHeight(mc.font.lineHeight + 4);
            return;
        }

        float x = getX();
        float y = getY();
        GuiGraphics ctx = e.getContext();
        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        int maxWidth = 0;
        
        Color primaryColor = Oxevy.colorManager.getColor();
        int bgAlpha = backgroundOpacity.getValue();

        // Target name with icon
        String name = target.getName().getString();
        double dist = mc.player.distanceTo(target);
        String nameLine = "§7✦ §f" + name + " §8(§7" + String.format(Locale.US, "%.1f", dist) + "§8)";
        ctx.drawString(mc.font, nameLine, (int) x, (int) drawY, 0xFFFFFFFF);
        maxWidth = Math.max(maxWidth, mc.font.width(nameLine));
        drawY += lineHeight;

        // Health bar
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float absorption = target.getAbsorptionAmount();
        float barW = 140;
        float barH = 10;
        
        RenderUtil.rect(ctx, x, drawY, x + barW, drawY + barH, (bgAlpha << 24) | 0x1a1a1a);
        RenderUtil.rect(ctx, x + 1, drawY + 1, x + barW - 1, drawY + barH - 1, (bgAlpha << 24) | 0x2a2a2a);
        
        float healthPct = Math.min(1f, health / maxHealth);
        float healthEnd = (barW - 2) * healthPct;
        
        Color healthColor = healthColor(healthPct);
        RenderUtil.rect(ctx, x + 1, drawY + 1, x + 1 + healthEnd, drawY + barH - 1, healthColor.getRGB());
        
        if (absorption > 0) {
            float absPct = Math.min(1f - healthPct, absorption / (maxHealth * 2f));
            float absEnd = (barW - 2) * absPct;
            RenderUtil.rect(ctx, x + 1 + healthEnd, drawY + 1, x + 1 + healthEnd + absEnd, drawY + barH - 1, 0xFFFFAA00);
        }
        
        String hpStr = String.format(Locale.US, "%.1f", health) + (absorption > 0 ? " §e+" + String.format(Locale.US, "%.1f", absorption) : "") + "§7/§f" + String.format(Locale.US, "%.1f", maxHealth);
        float hpWidth = mc.font.width(hpStr);
        ctx.drawString(mc.font, hpStr, (int) (x + barW / 2 - hpWidth / 2), (int) (drawY + 1), 0xFFFFFFFF);
        
        maxWidth = Math.max(maxWidth, (int) barW);
        drawY += barH + 2;

        // Armor
        if (showArmorDurability.getValue()) {
            List<ItemStack> armor = List.of(
                target.getItemBySlot(EquipmentSlot.HEAD),
                target.getItemBySlot(EquipmentSlot.CHEST),
                target.getItemBySlot(EquipmentSlot.LEGS),
                target.getItemBySlot(EquipmentSlot.FEET)
            );
            StringBuilder armorStr = new StringBuilder("§7⚙ ");
            for (ItemStack stack : armor) {
                if (stack.isEmpty()) continue;
                int max = stack.getMaxDamage();
                int cur = max - stack.getDamageValue();
                int pct = max > 0 ? (cur * 100 / max) : 100;
                String color = pct > 50 ? "§a" : pct > 25 ? "§e" : "§c";
                armorStr.append(color).append(pct).append(" ");
            }
            if (armorStr.length() > 4) {
                ctx.drawString(mc.font, armorStr.toString(), (int) x, (int) drawY, 0xFFAAAAAA);
                maxWidth = Math.max(maxWidth, mc.font.width(armorStr.toString()));
                drawY += lineHeight;
            }
        }

        // Held item
        if (showHeldItem.getValue()) {
            ItemStack main = target.getMainHandItem();
            if (!main.isEmpty()) {
                String itemStr = "§7✚ " + main.getHoverName().getString();
                ctx.drawString(mc.font, itemStr, (int) x, (int) drawY, 0xFFAAAAAA);
                maxWidth = Math.max(maxWidth, mc.font.width(itemStr));
                drawY += lineHeight;
            }
        }

        // Combat prediction
        if (showCombatPrediction.getValue()) {
            float damagePerHit = getEstimatedDamagePerHit(target);
            boolean canCrit = mc.player.fallDistance > 0 && !mc.player.onGround() && !mc.player.isInWater();
            
            if (damagePerHit > 0) {
                float effectiveHealth = health + absorption;
                int hits = (int) Math.ceil(effectiveHealth / damagePerHit);
                String predStr = "§7⚔ " + hits + " hits §8(§f" + String.format(Locale.US, "%.1f", damagePerHit) + "§8)";
                ctx.drawString(mc.font, predStr, (int) x, (int) drawY, 0xFFAAAAAA);
                maxWidth = Math.max(maxWidth, mc.font.width(predStr));
                drawY += lineHeight;
            }
            if (canCrit) {
                ctx.drawString(mc.font, "§7⚡ Crit", (int) x, (int) drawY, 0xFFFFFF55);
                maxWidth = Math.max(maxWidth, mc.font.width("⚡ Crit"));
                drawY += lineHeight;
            }
        }

        // Ping
        if (showPing.getValue() && target instanceof Player player) {
            int ping = getPing(player);
            if (ping >= 0) {
                String pingStr = "§7✉ " + pingColorStr(ping) + ping + "ms";
                ctx.drawString(mc.font, pingStr, (int) x, (int) drawY, pingColor(ping));
                maxWidth = Math.max(maxWidth, mc.font.width(pingStr));
                drawY += lineHeight;
            }
        }

        // Combo
        if (showCombo.getValue()) {
            int combo = tm.getComboCount();
            if (combo > 0) {
                String comboStr = "§6✦ " + combo;
                ctx.drawString(mc.font, comboStr, (int) x, (int) drawY, 0xFFFFFF00);
                maxWidth = Math.max(maxWidth, mc.font.width(comboStr));
                drawY += lineHeight;
            }
        }

        // LOS
        if (lineOfSight.getValue()) {
            boolean los = hasLineOfSight(target);
            ctx.drawString(mc.font, "§7◎ " + (los ? "§aVisible" : "§cBlocked"), (int) x, (int) drawY, los ? 0xFF55FF55 : 0xFFFF5555);
            maxWidth = Math.max(maxWidth, mc.font.width("◎ Blocked"));
            drawY += lineHeight;
        }

        setWidth(Math.max(getWidth(), maxWidth + 4));
        setHeight(drawY - y);

        if (background.getValue()) {
            int alpha = backgroundOpacity.getValue();
            RenderUtil.rect(ctx, x - 2, y - 2, x + getWidth() + 2, y + getHeight() + 2, (alpha << 24) | 0x11_11_11);
        }
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (nullCheck() || !glowEffect.getValue() && !rangeCircle.getValue() && !lineOfSight.getValue()) return;
        LivingEntity target = Oxevy.targetManager.getTarget();
        if (target == null) return;

        Vec3 pos = target.position();
        AABB box = target.getBoundingBox();
        PoseStack stack = event.getMatrix();

        if (glowEffect.getValue()) {
            Color glow = Oxevy.colorManager.getColor();
            RenderUtil.drawBox(stack, box, glow, 2f, true);
        }

        if (rangeCircle.getValue()) {
            Vec3 center = new Vec3(pos.x, box.minY + 0.02, pos.z);
            RenderUtil.drawCircle(stack, center, 0.5f, 32, new Color(255, 200, 0, 180), 1.5f, true);
        }

        if (lineOfSight.getValue()) {
            Vec3 from = mc.player.getEyePosition(1f);
            Vec3 to = target.getEyePosition(1f);
            boolean los = hasLineOfSight(target);
            RenderUtil.drawLine(stack, from, to, los ? new Color(0, 255, 0, 200) : new Color(255, 0, 0, 200), 1.2f, true);
        }
    }

    private Color healthColor(float pct) {
        if (pct > 0.6f) return new Color(46, 204, 113);
        if (pct > 0.3f) return new Color(241, 196, 15);
        return new Color(231, 76, 60);
    }

    private String pingColorStr(int ms) {
        if (ms < 50) return "§a";
        if (ms < 100) return "§e";
        return "§c";
    }

    private int getPing(Player player) {
        if (mc.getConnection() == null) return -1;
        try {
            var info = mc.getConnection().getPlayerInfo(player.getName().getString());
            return info != null ? info.getLatency() : -1;
        } catch (Throwable t) {
            return -1;
        }
    }

    private int pingColor(int ms) {
        if (ms < 50) return 0xFF44FF44;
        if (ms < 100) return 0xFFFFCC44;
        return 0xFFFF4444;
    }

    private float getEstimatedDamagePerHit(LivingEntity target) {
        try {
            double base = mc.player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            if (base <= 0) return 4f;
            double armor = target.getAttributeValue(Attributes.ARMOR);
            double toughness = target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            double armorReduction = Math.min(20, Math.max(armor / 5, armor - base / (2 + toughness / 4)));
            double reduction = 1 - armorReduction / 25;
            double damage = base * Math.max(0.2, reduction);
            if (mc.player.fallDistance > 0 && !mc.player.onGround()) damage *= 1.5;
            return (float) Math.max(0.5, damage);
        } catch (Throwable t) {
            return 4f;
        }
    }

    private boolean hasLineOfSight(LivingEntity target) {
        Vec3 from = mc.player.getEyePosition(1f);
        Vec3 to = target.getEyePosition(1f);
        return mc.level.clip(new net.minecraft.world.level.ClipContext(from, to, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player)).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }
}
