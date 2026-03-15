package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.MathUtil;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NametagsModule extends Module {
    public final Setting<Float> textSize = num("TextSize", 1.0f, 0.5f, 2.5f);
    public final Setting<Boolean> background = bool("Background", true);
    public final Setting<Boolean> backgroundBlur = bool("BackgroundBlur", false);
    public final Setting<Boolean> distanceScale = bool("DistanceScale", true);
    public final Setting<Float> maxDistance = num("MaxDistance", 64f, 16f, 128f);
    public final Setting<HealthMode> healthMode = mode("HealthMode", HealthMode.BAR);
    public final Setting<Float> fadeTime = num("FadeTime", 0.2f, 0.05f, 1f);
    public final Setting<Boolean> showDistance = bool("ShowDistance", true);
    public final Setting<Boolean> showEquipment = bool("ShowEquipment", true);
    public final Setting<Boolean> showPing = bool("ShowPing", true);
    public final Setting<Boolean> showGameMode = bool("ShowGameMode", true);

    private final List<NametagEntry> entriesThisFrame = new ArrayList<>();
    private final Map<Integer, Float> entityAlpha = new HashMap<>();

    public NametagsModule() {
        super("Nametags", "Floating nametags with health, distance, equipment, ping, game mode", Category.RENDER);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (nullCheck()) return;
        entriesThisFrame.clear();
        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        Set<Integer> seen = new HashSet<>();
        float fadeSpeed = 1f / Math.max(0.05f, fadeTime.getValue());

        for (Player player : mc.level.players()) {
            if (player == mc.player || player.isInvisible() && !mc.player.isSpectator()) continue;
            double dist = camera.distanceTo(player.position());
            if (dist > maxDistance.getValue()) continue;

            float offset = me.alpha432.oxevy.features.modules.client.ClickGuiModule.getInstance().nameTagOffset.getValue();
            float[] screen = event.worldToScreen(player.position().add(0, player.getEyeHeight(player.getPose()) + offset, 0), camera);
            if (screen == null) continue;

            int id = player.getId();
            seen.add(id);
            float targetAlpha = 1f;
            float currentAlpha = MathUtil.lerp(entityAlpha.getOrDefault(id, 0f), targetAlpha, event.getDelta() * fadeSpeed);
            entityAlpha.put(id, currentAlpha);

            float scale = textSize.getValue();
            if (distanceScale.getValue()) {
                scale *= (float) Math.max(0.3, 1.0 - (dist / maxDistance.getValue()) * 0.7);
            }

            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            float absorption = player.getAbsorptionAmount();
            int armor = player.getArmorValue();

            int ping = -1;
            GameType gameType = null;
            if (mc.getConnection() != null) {
                PlayerInfo info = mc.getConnection().getPlayerInfo(player.getName().getString());
                if (info != null) {
                    ping = info.getLatency();
                    gameType = info.getGameMode();
                }
            }

            List<ItemStack> armorStacks = List.of(
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET)
            );

            entriesThisFrame.add(new NametagEntry(
                screen[0], screen[1], scale, currentAlpha,
                player.getName().getString(),
                health, maxHealth, absorption, armor,
                (int) dist, ping, gameType,
                player.getMainHandItem(),
                player.getOffhandItem(),
                armorStacks
            ));
        }

        entityAlpha.entrySet().removeIf(e -> {
            if (seen.contains(e.getKey())) return false;
            float next = MathUtil.lerp(e.getValue(), 0f, event.getDelta() * fadeSpeed);
            if (next < 0.01f) return true;
            e.setValue(next);
            return false;
        });
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (nullCheck()) return;
        GuiGraphics context = event.getContext();
        for (NametagEntry e : entriesThisFrame) {
            renderNametag(context, e);
        }
    }

    private void renderNametag(GuiGraphics context, NametagEntry e) {
        String name = e.name;
        String healthStr = healthMode.getValue() == HealthMode.NUMBER
            ? String.format("%.1f", e.health) + (e.absorption > 0 ? "+" + String.format("%.1f", e.absorption) : "")
            : null;

        int nameWidth = mc.font.width(name);
        int nameHeight = mc.font.lineHeight;
        int totalWidth = nameWidth;
        int totalHeight = nameHeight;

        if (healthStr != null) {
            int hw = mc.font.width(healthStr);
            totalWidth = Math.max(totalWidth, hw);
            totalHeight += nameHeight + 2;
        } else {
            totalHeight += 8;
        }

        if (showDistance.getValue()) {
            String distStr = e.distance + "m";
            totalWidth = Math.max(totalWidth, mc.font.width(distStr));
            totalHeight += nameHeight + 1;
        }

        if (showPing.getValue() && e.ping >= 0) {
            totalWidth = Math.max(totalWidth, mc.font.width(e.ping + "ms"));
            totalHeight += nameHeight + 1;
        }

        if (showGameMode.getValue() && e.gameMode != null) {
            totalWidth = Math.max(totalWidth, mc.font.width(gameModeStr(e.gameMode)));
            totalHeight += nameHeight + 1;
        }

        int equipmentRows = 0;
        if (showEquipment.getValue()) {
            int items = 0;
            if (!e.mainHand.isEmpty()) items++;
            if (!e.offHand.isEmpty()) items++;
            for (ItemStack stack : e.armorStacks) if (!stack.isEmpty()) items++;
            if (items > 0) {
                equipmentRows = 1;
                totalWidth = Math.max(totalWidth, Math.min(items, 4) * 18);
                totalHeight += 20;
            }
        }

        int pad = 2;
        float w = (totalWidth + pad * 2) * e.scale;
        float h = (totalHeight + pad * 2) * e.scale;
        float left = e.sx - w / 2f;
        float top = e.sy;

        int alpha = (int) (255 * e.alpha);
        if (alpha < 5) return;

        int bgAlpha = backgroundBlur.getValue() ? (int) (0.65 * alpha) : (int) (0.5 * alpha);
        int bgColor = new Color(0, 0, 0, bgAlpha).getRGB();
        int textColor = new Color(255, 255, 255, alpha).getRGB();

        if (background.getValue()) {
            if (backgroundBlur.getValue()) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue;
                        RenderUtil.rect(context, left + i, top + j, left + w + i, top + h + j, new Color(0, 0, 0, (int) (0.2 * alpha)).getRGB());
                    }
                }
            }
            RenderUtil.rect(context, left, top, left + w, top + h, bgColor);
        }

        float y = top + pad;
        context.drawString(mc.font, name, (int) (e.sx - nameWidth * e.scale / 2f), (int) y, textColor);
        y += nameHeight + 2;

        if (healthMode.getValue() == HealthMode.NUMBER && healthStr != null) {
            int hw = mc.font.width(healthStr);
            context.drawString(mc.font, healthStr, (int) (e.sx - hw * e.scale / 2f), (int) y, textColor);
            y += nameHeight + 2;
        } else if (healthMode.getValue() == HealthMode.BAR) {
            float barW = nameWidth * e.scale;
            float barH = 3;
            float healthPct = Math.min(1f, e.health / e.maxHealth);
            float absPct = e.absorption / (e.maxHealth * 2f);
            float barY = y;
            float barLeft = e.sx - barW / 2f;
            RenderUtil.rect(context, barLeft, barY, barLeft + barW, barY + barH, new Color(40, 40, 40, alpha).getRGB());
            RenderUtil.rect(context, barLeft, barY, barLeft + barW * healthPct, barY + barH, healthColor(healthPct, alpha));
            if (absPct > 0) {
                RenderUtil.rect(context, barLeft + barW * healthPct, barY, barLeft + barW * (healthPct + absPct), barY + barH,
                    new Color(255, 255, 0, alpha).getRGB());
            }
            y += 8;
        }

        if (showDistance.getValue()) {
            String distStr = e.distance + "m";
            int dw = mc.font.width(distStr);
            context.drawString(mc.font, distStr, (int) (e.sx - dw * e.scale / 2f), (int) y, new Color(180, 180, 180, alpha).getRGB());
            y += nameHeight + 1;
        }

        if (showPing.getValue() && e.ping >= 0) {
            String pingStr = e.ping + "ms";
            int pingColor = pingColor(e.ping, alpha);
            int pw = mc.font.width(pingStr);
            context.drawString(mc.font, pingStr, (int) (e.sx - pw * e.scale / 2f), (int) y, pingColor);
            y += nameHeight + 1;
        }

        if (showGameMode.getValue() && e.gameMode != null) {
            String gmStr = gameModeStr(e.gameMode);
            context.drawString(mc.font, gmStr, (int) (e.sx - mc.font.width(gmStr) * e.scale / 2f), (int) y, new Color(160, 200, 255, alpha).getRGB());
            y += nameHeight + 1;
        }

        if (showEquipment.getValue()) {
            float iconSize = 16 * e.scale;
            float startX = e.sx - (iconSize * 2);
            float iconY = y;
            int col = 0;
            if (!e.mainHand.isEmpty()) {
                drawItem(context, e.mainHand, (int) (startX + col * (iconSize + 2)), (int) iconY);
                col++;
            }
            if (!e.offHand.isEmpty()) {
                drawItem(context, e.offHand, (int) (startX + col * (iconSize + 2)), (int) iconY);
                col++;
            }
            for (int i = 0; i < e.armorStacks.size(); i++) {
                ItemStack stack = e.armorStacks.get(i);
                if (!stack.isEmpty()) {
                    drawItem(context, stack, (int) (startX + col * (iconSize + 2)), (int) iconY);
                    col++;
                }
            }
        }
    }

    private void drawItem(GuiGraphics context, ItemStack stack, int x, int y) {
        context.renderItem(stack, x, y);
        context.renderItemDecorations(mc.font, stack, x, y);
    }

    private static String gameModeStr(GameType mode) {
        return switch (mode) {
            case SURVIVAL -> "S";
            case CREATIVE -> "C";
            case ADVENTURE -> "A";
            case SPECTATOR -> "SP";
        };
    }

    private static int pingColor(int pingMs, int alpha) {
        if (pingMs < 50) return new Color(100, 255, 100, alpha).getRGB();
        if (pingMs < 100) return new Color(200, 255, 100, alpha).getRGB();
        if (pingMs < 150) return new Color(255, 255, 100, alpha).getRGB();
        return new Color(255, 100, 100, alpha).getRGB();
    }

    private static int healthColor(float pct, int alpha) {
        float r = pct > 0.5f ? (1 - pct) * 2 : 1f;
        float g = pct <= 0.5f ? pct * 2 : 1f;
        return new Color((int) (r * 255), (int) (g * 255), 0, alpha).getRGB();
    }

    public enum HealthMode {
        BAR,
        NUMBER
    }

    private record NametagEntry(float sx, float sy, float scale, float alpha,
                                String name, float health, float maxHealth, float absorption, int armorValue,
                                int distance, int ping, GameType gameMode,
                                ItemStack mainHand, ItemStack offHand, List<ItemStack> armorStacks) {}
}
