package coolaid.handsoffmyblock.fabric;

import coolaid.handsoffmyblock.util.BlockAccessManager;
import coolaid.handsoffmyblock.util.VillagerMemoryHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

import java.util.HashSet;
import java.util.Set;

public final class HandsOffMyBlockFabric implements ModInitializer {

    public static Item MARKER_ITEM = Items.STICK;

    private static final Set<Block> WORKSTATIONS = new HashSet<>();
    static {
        WORKSTATIONS.add(Blocks.BARREL);
        WORKSTATIONS.add(Blocks.BLAST_FURNACE);
        WORKSTATIONS.add(Blocks.BREWING_STAND);
        WORKSTATIONS.add(Blocks.CARTOGRAPHY_TABLE);
        WORKSTATIONS.add(Blocks.CAULDRON);
        WORKSTATIONS.add(Blocks.COMPOSTER);
        WORKSTATIONS.add(Blocks.FLETCHING_TABLE);
        WORKSTATIONS.add(Blocks.GRINDSTONE);
        WORKSTATIONS.add(Blocks.LECTERN);
        WORKSTATIONS.add(Blocks.LOOM);
        WORKSTATIONS.add(Blocks.SMITHING_TABLE);
        WORKSTATIONS.add(Blocks.SMOKER);
        WORKSTATIONS.add(Blocks.STONECUTTER);
    }

    @Override
    public void onInitialize() {

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {

            if (world.isClientSide() || !player.isCrouching()) return InteractionResult.PASS;

            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() != MARKER_ITEM) return InteractionResult.PASS;

            BlockPos pos = hit.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            // Marks/Unmarks only beds and workstation blocks
            if (!(block instanceof BedBlock) && !WORKSTATIONS.contains(block)) return InteractionResult.PASS;
            if (!(world instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

            boolean isBed = block instanceof BedBlock;
            BlockPos otherHalf = isBed ? pos.relative(((BedBlock) block).getConnectedDirection(state)) : null;

            boolean alreadyBlocked =
                    BlockAccessManager.isBlocked(pos) ||
                            (isBed && BlockAccessManager.isBlocked(otherHalf));

            if (alreadyBlocked) {
                BlockAccessManager.unmarkBlock(pos);
                if (isBed) BlockAccessManager.unmarkBlock(otherHalf);

                //  Release POI for head half, since that's what the villager tries to claim
                if (isBed) {
                    BlockPos headPos = state.getValue(BedBlock.PART) == BedPart.HEAD ? pos : otherHalf;
                    serverLevel.getPoiManager().release(headPos);
                } else {
                    serverLevel.getPoiManager().release(pos);
                }

                // Invalidate nearby villagers so they can reclaim unmarked workstation
                VillagerMemoryHelper.invalidateNearbyVillagers(serverLevel, pos, isBed);
                if (isBed) VillagerMemoryHelper.invalidateNearbyVillagers(serverLevel, otherHalf, isBed);

                // Action bar message
                player.displayClientMessage(
                        Component.literal("Unmarked ").append(block.getName()).withStyle(ChatFormatting.GREEN),
                        true
                );
            } else {
                BlockAccessManager.markBlock(pos);
                if (isBed) BlockAccessManager.markBlock(otherHalf);

                // Stop villagers from pathfinding to workstation
                VillagerMemoryHelper.invalidateNearbyVillagers(serverLevel, pos, isBed);
                if (isBed) VillagerMemoryHelper.invalidateNearbyVillagers(serverLevel, otherHalf, isBed);

                // Action bar message
                player.displayClientMessage(
                        Component.literal("Marked ").append(block.getName()).withStyle(ChatFormatting.RED),
                        true
                );
            }

            return InteractionResult.SUCCESS;
        });
    }
}