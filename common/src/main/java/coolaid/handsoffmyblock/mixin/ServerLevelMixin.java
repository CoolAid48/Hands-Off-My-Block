package coolaid.handsoffmyblock.mixin;

import coolaid.handsoffmyblock.util.ServerLevelTracker;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onServerLevelCreated(CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        ServerLevelTracker.registerLevel(self);
    }
}