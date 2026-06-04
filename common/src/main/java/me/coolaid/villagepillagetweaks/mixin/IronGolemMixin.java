package me.coolaid.villagepillagetweaks.mixin;

import me.coolaid.villagepillagetweaks.util.PillagerThreatHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IronGolem.class)
public abstract class IronGolemMixin {
    @Inject(method = "canAttack", at = @At("HEAD"), cancellable = true)
    private void villagePillageTweaks$ignoreUnarmedPillagerTargets(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (PillagerThreatHelper.shouldIgnoreUnarmedPillager(target)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "doPush", at = @At("TAIL"))
    private void villagePillageTweaks$clearUnarmedPillagerPushTarget(Entity entity, CallbackInfo ci) {
        IronGolem golem = (IronGolem) (Object) this;
        if (PillagerThreatHelper.shouldIgnoreUnarmedPillager(entity) && golem.getTarget() == entity) {
            golem.setTarget(null);
        }
    }
}
