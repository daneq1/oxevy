package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.client.DeltaTracker;

import java.lang.reflect.Field;

public class TimerModule extends Module {
    private final Setting<Float> speed = num("Speed", 1.5f, 0.1f, 4.0f);
    private float originalTps = 20.0f;

    public TimerModule() {
        super("Timer", "Speeds up the game tick", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        DeltaTracker deltaTracker = mc.getDeltaTracker();
        if (deltaTracker == null) return;
        
        try {
            Field timerField = DeltaTracker.class.getDeclaredField("timer");
            timerField.setAccessible(true);
            Object timer = timerField.get(deltaTracker);
            
            if (timer != null) {
                Field tpsField = timer.getClass().getDeclaredField("tps");
                tpsField.setAccessible(true);
                tpsField.set(timer, speed.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        DeltaTracker deltaTracker = mc.getDeltaTracker();
        if (deltaTracker == null) return;
        
        try {
            Field timerField = DeltaTracker.class.getDeclaredField("timer");
            timerField.setAccessible(true);
            Object timer = timerField.get(deltaTracker);
            
            if (timer != null) {
                Field tpsField = timer.getClass().getDeclaredField("tps");
                tpsField.setAccessible(true);
                tpsField.set(timer, originalTps);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
