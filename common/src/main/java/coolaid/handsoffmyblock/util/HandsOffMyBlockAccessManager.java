package coolaid.handsoffmyblock.util;

import coolaid.handsoffmyblock.data.HandsOffMyMarkedBlocksData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public final class HandsOffMyBlockAccessManager {

    public enum DestroyReason {
        PLAYER, EXPLOSION, FIRE, PISTON, REPLACED, UNKNOWN
    }

    private static final Map<ServerLevel, Map<BlockPos, BlockState>> LAST_STATES = new WeakHashMap<>();
    private static final Map<ServerLevel, Map<BlockPos, DestroyReason>> LAST_REASONS = new WeakHashMap<>();

    public static void unmarkExternallyDestroyed(
            ServerLevel level, BlockPos pos, DestroyReason reason
    ) {

        // Get original state before removal, then unmark
        BlockState old = getLastState(level, pos);
        unmarkBlock(level, pos);
        if (old == null) return;

        Component msg = Component.literal("Unmarked ")
                .append(old.getBlock().getName()).withStyle(ChatFormatting.GREEN)
                .append(" ").append(Component.literal("(" + reasonText(reason) + ")").withStyle(ChatFormatting.YELLOW));

        for (ServerPlayer player :
                level.getPlayers(p ->
                        p.blockPosition().closerThan(pos, 64))) {

            player.displayClientMessage(msg, true);
        }
    }

    private static String reasonText(DestroyReason reason) {

        return switch (reason) {

            case PLAYER -> "Broken";
            case EXPLOSION -> "Exploded";
            case FIRE -> "Burned";
            case PISTON -> "Pushed";
            case REPLACED -> "Replaced";
            default -> "Destroyed";
        };
    }

    // MARKING METHODS
    public static void markBlock(ServerLevel level, BlockPos pos) {
        HandsOffMyMarkedBlocksData.get(level).mark(pos);
        track(level, pos);
    }

    public static void unmarkBlock(ServerLevel level, BlockPos pos) {
        HandsOffMyMarkedBlocksData.get(level).unmark(pos);
        clear(level, pos);
    }

    public static boolean isBlocked(ServerLevel level, BlockPos pos) {
        return HandsOffMyMarkedBlocksData.get(level).isMarked(pos);
    }

    public static Set<BlockPos> getMarked(ServerLevel level) {
        return HandsOffMyMarkedBlocksData.get(level).getAllMarked();
    }

    // TRACKING METHODS
    public static void track(ServerLevel level, BlockPos pos) {

        LAST_STATES
                .computeIfAbsent(level, l -> new HashMap<>())
                .put(pos, level.getBlockState(pos));
    }

    public static BlockState getLastState(ServerLevel level, BlockPos pos) {
        return LAST_STATES.getOrDefault(level, Map.of()).get(pos);
    }

    public static DestroyReason consumeReason(
            ServerLevel level, BlockPos pos
    ) {
        Map<BlockPos, DestroyReason> map = LAST_REASONS.get(level);

        if (map == null) return DestroyReason.UNKNOWN;
        DestroyReason reason = map.remove(pos);
        return reason != null ? reason : DestroyReason.UNKNOWN;
    }

    private static void clear(ServerLevel level, BlockPos pos) {

        Map<BlockPos, BlockState> states = LAST_STATES.get(level);
        if (states != null) states.remove(pos);

        Map<BlockPos, DestroyReason> reasons = LAST_REASONS.get(level);
        if (reasons != null) reasons.remove(pos);
    }
}
