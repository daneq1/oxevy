package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.alpha432.oxevy.util.render.Layers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import java.awt.Color;

public class TracerModule extends Module {
    public final Setting<Float> tracerRange = num("Range", 100f, 10f, 500f);
    public final Setting<Boolean> throughWalls = bool("ThroughWalls", true);
    public final Setting<Boolean> fromFeet = bool("FromFeet", false);
    public final Setting<Boolean> toFeet = bool("ToFeet", true);
    public final Setting<Float> lineWidth = num("LineWidth", 1.5f, 0.5f, 5f);
    public final Setting<Float> opacity = num("Opacity", 1.0f, 0.0f, 1.0f);
    public final Setting<Boolean> showPlayers = bool("Players", true);
    public final Setting<Boolean> showFriends = bool("Friends", true);
    public final Setting<Boolean> showAnimals = bool("Animals", false);
    public final Setting<Boolean> showMonsters = bool("Monsters", false);
    public final Setting<Boolean> showInvisibles = bool("Invisibles", false);
    public final Setting<String> colorMode = str("ColorMode", "Distance");
    public final Setting<Color> staticColor = color("StaticColor", 255, 255, 255, 255);
    public final Setting<Color> friendColor = color("FriendColor", 0, 255, 0, 255);
    public final Setting<Color> enemyColor = color("EnemyColor", 255, 0, 0, 255);
    public final Setting<Color> neutralColor = color("NeutralColor", 255, 255, 0, 255);

    private int renderCount = 0;

    public TracerModule() {
        super("Tracers", "Draws tracer lines to entities", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        renderCount = 0;
        Vec3 startPos = getStartPoint();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!shouldRenderEntity(entity)) continue;

            Color color = getColor(entity);
            Vec3 endPos = getEndPoint(entity);

            drawLine(event.getMatrix(), startPos, endPos, color, lineWidth.getValue(), throughWalls.getValue());
            renderCount++;
        }
    }

    private Vec3 getStartPoint() {
        if (fromFeet.getValue()) {
            return new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }
        return new Vec3(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
    }

    private Vec3 getEndPoint(Entity entity) {
        var bb = entity.getBoundingBox();
        double minY = bb.minY;
        double maxY = bb.maxY;
        double centerX = bb.minX + (bb.maxX - bb.minX) * 0.5;
        double centerZ = bb.minZ + (bb.maxZ - bb.minZ) * 0.5;

        if (toFeet.getValue()) {
            return new Vec3(centerX, minY + 0.1, centerZ);
        }
        return new Vec3(centerX, maxY, centerZ);
    }

    private boolean shouldRenderEntity(Entity entity) {
        if (entity == mc.player) return false;
        if (entity == null || !entity.isAlive()) return false;

        double dist = mc.player.distanceTo(entity);
        if (dist > tracerRange.getValue()) return false;

        if (!throughWalls.getValue() && !mc.player.hasLineOfSight(entity)) {
            return false;
        }

        if (entity.isInvisible() && !showInvisibles.getValue()) {
            return false;
        }

        if (entity instanceof Player) {
            if (!showPlayers.getValue()) return false;
            boolean isFriend = Oxevy.friendManager.isFriend((Player) entity);
            if (isFriend && !showFriends.getValue()) return false;
        } else if (entity instanceof Animal) {
            if (!showAnimals.getValue()) return false;
        } else if (entity instanceof Monster) {
            if (!showMonsters.getValue()) return false;
        }

        return true;
    }

    private Color getColor(Entity entity) {
        Color color;
        float alpha = opacity.getValue();
        String mode = colorMode.getValue().toLowerCase();

        switch (mode) {
            case "distance":
                double dist = mc.player.distanceTo(entity);
                float ratio = (float) (dist / tracerRange.getValue());
                int r = (int) (255 * ratio);
                int g = (int) (255 * (1 - ratio));
                color = new Color(r, g, 0, (int) (255 * alpha));
                break;

            case "health":
                if (entity instanceof LivingEntity living) {
                    float health = living.getHealth();
                    float maxHealth = living.getMaxHealth();
                    float healthPercent = maxHealth > 0 ? health / maxHealth : 1f;
                    int red = (int) (255 * (1 - healthPercent));
                    int green = (int) (255 * healthPercent);
                    color = new Color(red, green, 0, (int) (255 * alpha));
                } else {
                    color = staticColor.getValue();
                }
                break;

            case "relationship":
                if (entity instanceof Player player) {
                    if (Oxevy.friendManager.isFriend(player)) {
                        color = friendColor.getValue();
                    } else {
                        color = neutralColor.getValue();
                    }
                } else if (entity instanceof Monster) {
                    color = enemyColor.getValue();
                } else if (entity instanceof Animal) {
                    color = new Color(100, 255, 100, (int) (255 * alpha));
                } else {
                    color = staticColor.getValue();
                }
                break;

            case "rainbow":
                long time = System.currentTimeMillis();
                float hue = (time % 2000) / 2000f;
                int rgb = Color.HSBtoRGB(hue, 1f, 1f);
                color = new Color(rgb);
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * alpha));
                break;

            default:
                color = staticColor.getValue();
                break;
        }

        return color;
    }

    private void drawLine(PoseStack stack, Vec3 from, Vec3 to, Color c, float lineWidth, boolean throughWalls) {
        Vec3 camera = mc.gameRenderer.getMainCamera().position();

        float x1 = (float) (from.x - camera.x);
        float y1 = (float) (from.y - camera.y);
        float z1 = (float) (from.z - camera.z);
        float x2 = (float) (to.x - camera.x);
        float y2 = (float) (to.y - camera.y);
        float z2 = (float) (to.z - camera.z);

        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH);
        PoseStack.Pose pose = stack.last();
        bufferBuilder.addVertex(pose, x1, y1, z1).setColor(c.getRGB()).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, x2, y2, z2).setColor(c.getRGB()).setLineWidth(lineWidth);

        (throughWalls ? Layers.lines() : Layers.linesDepth()).draw(bufferBuilder.buildOrThrow());
    }

    @Override
    public String getInfo() {
        return String.valueOf(renderCount);
    }
}
