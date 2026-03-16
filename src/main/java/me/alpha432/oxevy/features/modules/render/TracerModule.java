package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import java.awt.Color;

public class TracerModule extends Module {
    public final Setting<Float> maxDistance = num("MaxDistance", 100f, 16f, 256f);
    public final Setting<Float> lineWidth = num("LineWidth", 1.0f, 0.5f, 5f);
    public final Setting<Boolean> throughWalls = bool("ThroughWalls", true);
    public final Setting<String> targetPosition = str("Target", "Head");
    public final Setting<String> sourcePosition = str("Source", "Eyes");
    
    // Color settings
    public final Setting<Boolean> customColors = bool("CustomColors", false);
    public final Setting<Color> playerColor = color("PlayerColor", 255, 80, 80, 255);
    public final Setting<Color> friendColor = color("FriendColor", 0, 255, 0, 255);
    public final Setting<Color> mobColor = color("MobColor", 255, 0, 255, 255);
    public final Setting<Color> animalColor = color("AnimalColor", 255, 255, 0, 255);
    public final Setting<Color> otherColor = color("OtherColor", 255, 255, 255, 255);

    public TracerModule() {
        super("Tracer", "Draws tracers to nearby entities", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();
        Vec3 startPos = getSourcePosition();
        
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            if (entity == null || !entity.isAlive()) continue;

            double dist = cameraPos.distanceTo(entity.position());
            if (dist > maxDistance.getValue()) continue;

            Color color = getColor(entity);
            Vec3 to = getTargetPosition(entity);

            RenderUtil.drawLine(event.getMatrix(), startPos, to, color, lineWidth.getValue(), throughWalls.getValue());
        }
    }

    private Vec3 getSourcePosition() {
        String source = sourcePosition.getValue().toLowerCase();
        double playerY = mc.player.getY();
        double eyeY = mc.player.getEyeY();
        
        switch (source) {
            case "body":
            case "feet":
                return new Vec3(mc.player.getX(), playerY + 1.0, mc.player.getZ());
            case "eyes":
            default:
                return new Vec3(mc.player.getX(), eyeY, mc.player.getZ());
        }
    }

    private Vec3 getTargetPosition(Entity entity) {
        var bb = entity.getBoundingBox();
        double minY = bb.minY;
        double maxY = bb.maxY;
        double centerX = bb.minX + (bb.maxX - bb.minX) * 0.5;
        double centerZ = bb.minZ + (bb.maxZ - bb.minZ) * 0.5;
        
        String target = targetPosition.getValue().toLowerCase();
        
        switch (target) {
            case "head":
                return new Vec3(centerX, maxY, centerZ);
            case "body":
            case "torso":
                return new Vec3(centerX, minY + (maxY - minY) * 0.5, centerZ);
            case "feet":
            default:
                return new Vec3(centerX, minY + 0.1, centerZ);
        }
    }

    private Color getColor(Entity entity) {
        if (!customColors.getValue()) {
            // Default colors
            if (entity instanceof Player player) {
                if (Oxevy.friendManager.isFriend(player)) return new Color(0, 255, 0, 255);
                if (mc.player.isAlliedTo(player)) return new Color(100, 255, 180, 255);
                return new Color(255, 80, 80, 255);
            }
            if (entity instanceof LivingEntity) {
                if (entity instanceof Monster) return new Color(255, 0, 255, 255);
                if (entity instanceof Animal) return new Color(255, 255, 0, 255);
            }
            return new Color(255, 255, 255, 255);
        } else {
            // Custom colors from settings
            if (entity instanceof Player player) {
                if (Oxevy.friendManager.isFriend(player)) return friendColor.getValue();
                if (mc.player.isAlliedTo(player)) return friendColor.getValue();
                return playerColor.getValue();
            }
            if (entity instanceof Monster) return mobColor.getValue();
            if (entity instanceof Animal) return animalColor.getValue();
            return otherColor.getValue();
        }
    }
}
