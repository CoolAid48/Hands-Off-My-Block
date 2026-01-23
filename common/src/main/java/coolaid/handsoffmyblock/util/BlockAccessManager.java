package coolaid.handsoffmyblock.util;

import net.minecraft.core.BlockPos;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class BlockAccessManager {

    private static final Set<BlockPos> BLOCKED = new HashSet<>();

    public static boolean isBlocked(BlockPos pos) {
        return BLOCKED.contains(pos);
    }

    public static void markBlock(BlockPos pos) {
        BLOCKED.add(pos.immutable());
    }

    public static void unmarkBlock(BlockPos pos) {
        BLOCKED.remove(pos);
    }

    public static Set<BlockPos> getBlocked() {
        return Collections.unmodifiableSet(BLOCKED);
    }
}
