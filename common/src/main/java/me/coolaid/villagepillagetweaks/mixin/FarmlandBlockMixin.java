package me.coolaid.villagepillagetweaks.mixin;

import me.coolaid.villagepillagetweaks.config.ConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin extends Block {
    private static final double VANILLA_TRAMPLE_THRESHOLD = 0.5D;
    private static final float MIN_TRAMPLE_VOLUME = 0.512F;

    public FarmlandBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void villagePillageTweaks$mobCropTrampleThreshold(
            Level level,
            BlockState state,
            BlockPos pos,
            Entity entity,
            double fallDistance,
            CallbackInfo ci
    ) {
        if (entity instanceof Player || !(entity instanceof LivingEntity)) {
            return;
        }

        if (level instanceof ServerLevel serverLevel
                && level.getRandom().nextFloat() < fallDistance - cropTrampleThreshold()
                && (Boolean) serverLevel.getGameRules().get(GameRules.MOB_GRIEFING)
                && entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > MIN_TRAMPLE_VOLUME) {
            FarmlandBlock.turnToDirt(entity, state, level, pos);
        }

        super.fallOn(level, state, pos, entity, fallDistance);
        ci.cancel();
    }

    private static double cropTrampleThreshold() {
        double configuredThreshold = ConfigManager.get().cropTrampleThreshold;
        return Double.isFinite(configuredThreshold) && configuredThreshold >= 0.0D ? configuredThreshold : VANILLA_TRAMPLE_THRESHOLD;
    }
}
