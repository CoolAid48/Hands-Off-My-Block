package me.coolaid.villagepillagetweaks.mixin;

import me.coolaid.villagepillagetweaks.util.PillagerThreatHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(AvoidEntityGoal.class)
public class AvoidEntityGoalMixin {
    @ModifyArg(
            method = "canUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
            ),
            index = 2
    )
    private Predicate<LivingEntity> villagePillageTweaks$filterUnarmedPillagers(Predicate<LivingEntity> original) {
        return entity -> original.test(entity) && !PillagerThreatHelper.shouldIgnoreUnarmedPillager(entity);
    }
}
