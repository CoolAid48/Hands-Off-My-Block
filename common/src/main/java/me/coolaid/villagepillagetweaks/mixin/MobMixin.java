package me.coolaid.villagepillagetweaks.mixin;

import me.coolaid.villagepillagetweaks.util.PillagerThreatHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobMixin {
    @Inject(method = "canAttack", at = @At("HEAD"), cancellable = true)
    private void villagePillageTweaks$ignoreUnarmedPillagers(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (villagePillageTweaks$usesMobAttackRules()
                && PillagerThreatHelper.shouldIgnoreUnarmedPillager(target)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void villagePillageTweaks$stopTargetingUnarmedPillagers(CallbackInfo ci) {
        if (!villagePillageTweaks$usesSharedTargetCleanup()) {
            return;
        }

        Mob mob = (Mob) (Object) this;
        LivingEntity target = mob.getTarget();
        if (target != null && PillagerThreatHelper.shouldIgnoreUnarmedPillager(target)) {
            mob.setTarget(null);
        }
    }

    @Unique
    private boolean villagePillageTweaks$usesMobAttackRules() {
        return (Object) this instanceof SnowGolem || (Object) this instanceof TraderLlama;
    }

    @Unique
    private boolean villagePillageTweaks$usesSharedTargetCleanup() {
        return (Object) this instanceof IronGolem
                || (Object) this instanceof SnowGolem
                || (Object) this instanceof TraderLlama;
    }
}
