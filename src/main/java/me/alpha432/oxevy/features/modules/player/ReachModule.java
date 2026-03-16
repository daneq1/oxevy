package me.alpha432.oxevy.features.modules.player;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class ReachModule extends Module {
    public final Setting<Double> combatReach = num("CombatReach", 3.5, 3.0, 6.0);
    public final Setting<Double> blockReach = num("BlockReach", 4.5, 4.5, 6.0);

    public ReachModule() {
        super("Reach", "Increase reach distance", Category.PLAYER);
    }

    @Override
    public void onTick() {
    }

    public double getCombatReach() {
        return isEnabled() ? combatReach.getValue() : 3.0;
    }

    public double getBlockReach() {
        return isEnabled() ? blockReach.getValue() : 4.5;
    }
}
