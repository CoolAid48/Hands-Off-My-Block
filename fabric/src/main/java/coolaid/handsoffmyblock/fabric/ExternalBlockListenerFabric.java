package coolaid.handsoffmyblock.fabric;

import coolaid.handsoffmyblock.util.HandsOffMyBlockAccessManager;
import coolaid.handsoffmyblock.util.HandsOffMyBlockAccessManager.DestroyReason;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class ExternalBlockListenerFabric {

    public static void register() {

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!(world instanceof ServerLevel level)) return;

            for (BlockPos pos : HandsOffMyBlockAccessManager.getMarked(level).toArray(new BlockPos[0])) {
                BlockState last = HandsOffMyBlockAccessManager.getLastState(level, pos);

                if (last == null) continue;
                BlockState current = level.getBlockState(pos);

                if (!current.equals(last)) {
                    DestroyReason reason = HandsOffMyBlockAccessManager.consumeReason(level, pos);

                    HandsOffMyBlockAccessManager.unmarkExternallyDestroyed(level, pos, reason
                    );
                }
            }
        });
    }
}
