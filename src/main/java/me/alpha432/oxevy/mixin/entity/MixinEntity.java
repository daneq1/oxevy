package me.alpha432.oxevy.mixin.entity;

import me.alpha432.oxevy.Oxevy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.alpha432.oxevy.util.traits.Util.mc;

@Mixin(Entity.class)
public class MixinEntity {
    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    private void onGetBoundingBox(CallbackInfoReturnable<AABB> cir) {
        if (!Oxevy.hitboxesEnabled) return;
        
        Entity entity = (Entity) (Object) this;
        
        if (!(entity instanceof LivingEntity)) return;
        
        if (entity instanceof Player && !Oxevy.hitboxesPlayers) return;
        if (entity instanceof Mob && !(entity instanceof Player) && !Oxevy.hitboxesMobs) return;
        
        if (entity == mc.player) return;
        
        AABB original = cir.getReturnValue();
        if (original == null) return;
        
        double expand = Oxevy.hitboxesExpand - 1.0;
        
        cir.setReturnValue(original.inflate(expand, expand * 0.5, expand));
    }
}
