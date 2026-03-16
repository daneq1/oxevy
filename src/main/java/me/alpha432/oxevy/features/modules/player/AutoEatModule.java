package me.alpha432.oxevy.features.modules.player;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoEatModule extends Module {
    public final Setting<Integer> hungerThreshold = num("HungerThreshold", 14, 1, 20);

    public AutoEatModule() {
        super("AutoEat", "Auto eats food", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || !mc.player.isAlive()) return;

        int foodLevel = mc.player.getFoodData().getFoodLevel();
        
        if (foodLevel >= hungerThreshold.getValue()) return;
        if (mc.player.isUsingItem()) return;

        int foodSlot = findFoodSlot();
        
        if (foodSlot != -1) {
            int prevSlot = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(foodSlot);
            
            // Use item in hand
            mc.player.swing(InteractionHand.MAIN_HAND);
            
            // Switch back
            mc.player.getInventory().setSelectedSlot(prevSlot);
        }
    }

    private int findFoodSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && isFood(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    private boolean isFood(net.minecraft.world.item.Item item) {
        return item == Items.BREAD ||
               item == Items.COOKED_BEEF ||
               item == Items.COOKED_CHICKEN ||
               item == Items.COOKED_PORKCHOP ||
               item == Items.GOLDEN_APPLE ||
               item == Items.ENCHANTED_GOLDEN_APPLE ||
               item == Items.APPLE ||
               item == Items.CARROT ||
               item == Items.POTATO;
    }
}
