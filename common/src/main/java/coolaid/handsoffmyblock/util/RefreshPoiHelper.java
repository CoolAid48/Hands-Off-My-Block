package coolaid.handsoffmyblock.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class RefreshPoiHelper {

    public static void refresh(ServerLevel level, BlockPos pos, BlockState state) {
        level.updatePOIOnBlockStateChange(pos, state, state);
    }
}
