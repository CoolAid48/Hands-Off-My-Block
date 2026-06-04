package me.coolaid.villagepillagetweaks.mixin;

import me.coolaid.villagepillagetweaks.util.PillagerThreatHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerHostilesSensor.class)
public class VillagerHostilesSensorMixin {
    @Inject(method = "isMatchingEntity", at = @At("HEAD"), cancellable = true)
    private void villagePillageTweaks$ignoreUnarmedPillagers(
            ServerLevel level,
            LivingEntity body,
            LivingEntity mob,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (PillagerThreatHelper.shouldIgnoreUnarmedPillager(mob)) {
            cir.setReturnValue(false);
        }
    }
}
