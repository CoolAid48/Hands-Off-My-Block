package coolaid.handsoffmyblock.fabric;

import coolaid.handsoffmyblock.config.HandsOffMyConfigManager;
import coolaid.handsoffmyblock.fabric.client.ConfigScreen;
import coolaid.handsoffmyblock.fabric.client.HandsOffMyBlockFabricClient;
import coolaid.handsoffmyblock.util.BlockAccessManager;
import coolaid.handsoffmyblock.util.BlockSets;
import coolaid.handsoffmyblock.util.VillagerMemoryHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;

public final class HandsOffMyBlockFabric implements ModInitializer {

    public static Item MARKER_ITEM = Items.STICK;

    @Override
    public void onInitialize() {

        reloadMarkerItemFromConfig();

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {

            if (world.isClientSide())
                return InteractionResult.PASS;

            var config = HandsOffMyConfigManager.get();

            // Config sneaking toggle
            if (config.requireSneaking && !player.isCrouching())
                return InteractionResult.PASS;

            // Config marker item
            Identifier markerId = config.markerItem;
            Item markerItem = BuiltInRegistries.ITEM
                    .getOptional(markerId)
                    .orElse(Items.STICK);

            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() != markerItem)
                return InteractionResult.PASS;

            BlockPos pos = hit.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            boolean isBed = block instanceof BedBlock;

            // Config bed toggle
            if (isBed && !config.enableBedMarking)
                return InteractionResult.PASS;

            // Only beds and workstation blocks are marked/unmarked
            if (!isBed && !BlockSets.WORKSTATIONS.contains(block))
                return InteractionResult.PASS;

            if (!(world instanceof ServerLevel serverLevel))
                return InteractionResult.PASS;

            BlockPos otherHalf = isBed
                    ? pos.relative(BedBlock.getConnectedDirection(state)) : null;

            boolean alreadyBlocked =
                    BlockAccessManager.isBlocked(serverLevel, pos) ||
                            (isBed && BlockAccessManager.isBlocked(serverLevel, otherHalf));

            if (alreadyBlocked) {
                BlockAccessManager.unmarkBlock(serverLevel, pos);
                if (isBed) BlockAccessManager.unmarkBlock(serverLevel, otherHalf);

                PoiManager poiManager = serverLevel.getPoiManager();

                if (isBed) {
                    BlockPos headPos = state.getValue(BedBlock.PART) == BedPart.HEAD ? pos : otherHalf;
                    if (poiManager.getType(headPos).isPresent()) {
                        poiManager.release(headPos);
                    }
                } else {
                    if (poiManager.getType(pos).isPresent()) {
                        poiManager.release(pos);
                    }
                }

                VillagerMemoryHelper.invalidateNearbyVillagers(serverLevel, pos, isBed);
                if (isBed) VillagerMemoryHelper.invalidateNearbyVillagers(serverLevel, otherHalf, isBed);

                player.displayClientMessage(
                        Component.literal("Unmarked ").append(block.getName()).withStyle(ChatFormatting.GREEN),
                        true
                );
            } else {
                spawnAngryVillagerParticles(serverLevel, pos, isBed ? otherHalf : null);

                // Invalidate nearby villagers so they can reclaim unmarked workstation
                BlockAccessManager.markBlock(serverLevel, pos);
                if (isBed) BlockAccessManager.markBlock(serverLevel, otherHalf);

                VillagerMemoryHelper.invalidateNearbyVillagers(serverLevel, pos, isBed);
                if (isBed) VillagerMemoryHelper.invalidateNearbyVillagers(serverLevel, otherHalf, isBed);

                player.displayClientMessage(
                        Component.literal("Marked ").append(block.getName()).withStyle(ChatFormatting.RED),
                        true
                );
            }
            return InteractionResult.SUCCESS;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide()) return true;
            if (!(world instanceof ServerLevel serverLevel)) return true;

            Block block = state.getBlock();
            boolean isBed = block instanceof BedBlock;

            // Only care about blocks that can be marked
            if (!isBed && !BlockSets.WORKSTATIONS.contains(block)) return true;

            PoiManager poiManager = serverLevel.getPoiManager();

            // Check if this block is marked, then remove POI
            if (BlockAccessManager.isBlocked(serverLevel, pos)) {
                if (poiManager.getType(pos).isPresent()) {
                    poiManager.remove(pos);
                }

                // If it's a bed, also remove the other half
                if (isBed) {
                    BlockPos otherHalf = pos.relative(BedBlock.getConnectedDirection(state));
                    if (poiManager.getType(otherHalf).isPresent()) {
                        poiManager.remove(otherHalf);
                    }
                }

                // Unmark POI
                BlockAccessManager.unmarkBlock(serverLevel, pos);
                if (isBed) {
                    BlockPos otherHalf = pos.relative(BedBlock.getConnectedDirection(state));
                    BlockAccessManager.unmarkBlock(serverLevel, otherHalf);
                }

                // Clean up villager memories
                VillagerMemoryHelper.invalidateNearbyVillagers(serverLevel, pos, isBed);
                if (isBed) {
                    BlockPos otherHalf = pos.relative(BedBlock.getConnectedDirection(state));
                    VillagerMemoryHelper.invalidateNearbyVillagers(serverLevel, otherHalf, isBed);
                }
            }

            return true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (HandsOffMyBlockFabricClient.openConfig != null && HandsOffMyBlockFabricClient.openConfig.consumeClick()) {
                Minecraft.getInstance().setScreen(new ConfigScreen(Minecraft.getInstance().screen));
            }
        });
    }

    public static void reloadMarkerItemFromConfig() {
        Identifier id = HandsOffMyConfigManager.get().markerItem;
        MARKER_ITEM = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.STICK);
    }

    private static void spawnAngryVillagerParticles(ServerLevel level, BlockPos pos, BlockPos otherHalf) {
        AABB searchArea = new AABB(pos).inflate(48.0);
        if (otherHalf != null) {
            searchArea = searchArea.minmax(new AABB(otherHalf).inflate(48.0));
        }

        for (Villager villager : level.getEntitiesOfClass(Villager.class, searchArea)) {
            if (villagerHasMemoryForBlock(villager, pos, level) || (otherHalf != null && villagerHasMemoryForBlock(villager, otherHalf, level))) {
                spawnAngryParticlesAboveHead(level, villager);
            }
        }
    }

    // Checks if villager has a job (workstation), a potential job (pathfinding), or a home (bed)
    private static boolean villagerHasMemoryForBlock(Villager villager, BlockPos pos, ServerLevel level) {
        if (villager.getBrain().hasMemoryValue(MemoryModuleType.JOB_SITE)) {
            GlobalPos jobSite = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).orElse(null);
            if (jobSite != null && jobSite.dimension().equals(level.dimension()) && jobSite.pos().equals(pos)) {
                return true;
            }
        }
        if (villager.getBrain().hasMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE)) {
            GlobalPos potentialJobSite = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).orElse(null);
            if (potentialJobSite != null && potentialJobSite.dimension().equals(level.dimension()) && potentialJobSite.pos().equals(pos)) {
                return true;
            }
        }
        if (villager.getBrain().hasMemoryValue(MemoryModuleType.HOME)) {
            GlobalPos home = villager.getBrain().getMemory(MemoryModuleType.HOME).orElse(null);
            return home != null && home.dimension().equals(level.dimension()) && home.pos().equals(pos);
        }

        return false;
    }

    // Spawns thunder particles above a villager's head (vanilla-like) when a claimed POI becomes unmarked
    private static void spawnAngryParticlesAboveHead(ServerLevel level, Villager villager) {
        double x = villager.getX();
        double y = villager.getY() + villager.getEyeHeight();
        double z = villager.getZ();

        level.sendParticles(
                ParticleTypes.ANGRY_VILLAGER,
                x, y, z,
                6, 0.3, 0.1, 0.3, 0.0
        );
    }
}