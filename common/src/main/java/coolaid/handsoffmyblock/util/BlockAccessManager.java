package coolaid.handsoffmyblock.util;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class BlockAccessManager {

    private static final Set<BlockPos> blockedPositions = new HashSet<>();

    public static void markBlock(BlockPos pos) {
        blockedPositions.add(pos.immutable());
    }

    public static void unmarkBlock(BlockPos pos) {
        blockedPositions.remove(pos);
    }

    public static boolean isBlocked(BlockPos pos) {
        return blockedPositions.contains(pos);
    }
}