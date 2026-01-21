package coolaid.handsoffmyblock.mixin;

import coolaid.handsoffmyblock.BlockAccessManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PoiManager.class)
public class PoiStorageMixin {

    @Inject(
            method = "getType",
            at = @At("HEAD"),
            cancellable = true
    )
    private void handsOffMyBlock_blockPOI(BlockPos pos, CallbackInfoReturnable<Optional<PoiType>> cir) {
        if (BlockAccessManager.isBlocked(pos)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}

