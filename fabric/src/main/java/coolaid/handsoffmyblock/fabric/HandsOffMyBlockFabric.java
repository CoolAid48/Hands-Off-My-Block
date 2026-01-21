package coolaid.handsoffmyblock.fabric;

import coolaid.handsoffmyblock.BlockAccessManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;

public final class HandsOffMyBlockFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!world.isClientSide() && player.isCrouching()) {
                BlockPos pos = hit.getBlockPos();
                if (world instanceof ServerLevel serverLevel) {
                    BlockAccessManager.markBlock(pos);
                    player.displayClientMessage(
                            Component.literal("Marked block at " + pos),
                            false // displays marked block in chat
                    );
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

    }
}