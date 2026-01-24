package coolaid.handsoffmyblock.util;

import coolaid.handsoffmyblock.data.MarkedBlocksData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

public final class BlockAccessManager {
    public static void markBlock(ServerLevel level, BlockPos pos) {
        MarkedBlocksData.get(level).mark(pos);
    }

    public static void unmarkBlock(ServerLevel level, BlockPos pos) {
        MarkedBlocksData.get(level).unmark(pos);
    }

    public static boolean isBlocked(ServerLevel level, BlockPos pos) {
        return MarkedBlocksData.get(level).isMarked(pos);
    }

    // Optionally get all blocked POI locations (not currently used but maybe for the future)
    public static Set<BlockPos> getBlocked(ServerLevel level) {
        return MarkedBlocksData.get(level).getAllMarked();
    }
}