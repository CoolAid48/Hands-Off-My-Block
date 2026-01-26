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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
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
            if (world.isClientSide()) return InteractionResult.PASS;

            var config = HandsOffMyConfigManager.get();

            // Config marker item
            ItemStack held = player.getItemInHand(hand);
            Item markerItem = BuiltInRegistries.ITEM.getOptional(config.markerItem).orElse(Items.STICK);
            if (held.getItem() != markerItem) return InteractionResult.PASS;

            BlockPos pos = hit.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            boolean isBed = block instanceof BedBlock;

            // CONFIG TOGGLES
            // (pathfindingTweaks is handled in VillagerMixin and actionBarMessages is handled in sendActionBarToPlayer() )
            if (!isBed && (!BlockSets.WORKSTATIONS.contains(block) || !config.enableWorkstationMarking)) return InteractionResult.PASS; // workstation toggle
            if (config.requireSneaking && !player.isCrouching()) return InteractionResult.PASS; // sneaking toggle
            if (isBed && !config.enableBedMarking) return InteractionResult.PASS; // bed toggle

            if (!(world instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

            BlockPos otherHalf = isBed ? pos.relative(BedBlock.getConnectedDirection(state)) : null;
            boolean alreadyBlocked = BlockAccessManager.isBlocked(serverLevel, pos)
                    || (isBed && BlockAccessManager.isBlocked(serverLevel, otherHalf));

            if (alreadyBlocked) {
                unmarkBlockAndInvalidate(serverLevel, pos, isBed, otherHalf, state);
                sendActionBarToPlayer(player,
                        Component.literal("Unmarked ").append(block.getName()).withStyle(ChatFormatting.GREEN)
                );
            } else {
                spawnAngryVillagerParticles(serverLevel, pos, otherHalf);
                markBlockAndInvalidate(serverLevel, pos, isBed, otherHalf);
                sendActionBarToPlayer(player,
                        Component.literal("Marked ").append(block.getName()).withStyle(ChatFormatting.RED)
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

            // Check if this block is marked, then remove POI
            if (BlockAccessManager.isBlocked(serverLevel, pos)) {
                unmarkBroken(serverLevel, pos, state);
                unmarkBlockAndInvalidate(serverLevel, pos, isBed,
                        isBed ? pos.relative(BedBlock.getConnectedDirection(state)) : null, state);
            }

            return true;
        });

        ExternalBlockListener.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (HandsOffMyBlockFabricClient.openConfig != null && HandsOffMyBlockFabricClient.openConfig.consumeClick()) {
                Minecraft.getInstance().setScreen(new ConfigScreen(Minecraft.getInstance().screen));
            }
        });
    }

    private static void unmarkBroken(ServerLevel level, BlockPos pos, BlockState state) {
        var players = level.getPlayers(p -> p.blockPosition().closerThan(pos, 64));

        Component msg = Component.literal("Unmarked ")
                .append(state.getBlock().getName())
                .withStyle(ChatFormatting.GREEN)
                .append(" §e(Destroyed)");

        for (var player : players) {
            sendActionBarToPlayer(player, msg);
        }
    }

    public static void sendActionBarToPlayer(net.minecraft.world.entity.player.Player player, Component message) {
        if (HandsOffMyConfigManager.get().actionBarMessages) {
            player.displayClientMessage(message, true);
        }
    }

    public static void reloadMarkerItemFromConfig() {
        MARKER_ITEM = BuiltInRegistries.ITEM.getOptional(HandsOffMyConfigManager.get().markerItem).orElse(Items.STICK);
    }

    private static void spawnAngryVillagerParticles(ServerLevel level, BlockPos pos, BlockPos otherHalf) {
        AABB searchArea = new AABB(pos).inflate(48.0);
        if (otherHalf != null) searchArea = searchArea.minmax(new AABB(otherHalf).inflate(48.0));

        for (Villager villager : level.getEntitiesOfClass(Villager.class, searchArea)) {
            if (villagerHasMemoryForBlock(villager, pos, level) || (otherHalf != null && villagerHasMemoryForBlock(villager, otherHalf, level))) {
                spawnAngryParticlesAboveHead(level, villager);
            }
        }
    }

    // Checks if villager has a job (workstation), a potential job (pathfinding), or a home (bed)
    private static boolean villagerHasMemoryForBlock(Villager villager, BlockPos pos, ServerLevel level) {
        return memoryMatches(villager, level, pos, MemoryModuleType.JOB_SITE)
                || memoryMatches(villager, level, pos, MemoryModuleType.POTENTIAL_JOB_SITE)
                || memoryMatches(villager, level, pos, MemoryModuleType.HOME);
    }

    private static boolean memoryMatches(Villager villager, ServerLevel level, BlockPos pos, MemoryModuleType<GlobalPos> type) {
        return villager.getBrain().getMemory(type).map(mem -> mem.dimension().equals(level.dimension()) && mem.pos().equals(pos)).orElse(false);
    }

    private static void spawnAngryParticlesAboveHead(ServerLevel level, Villager villager) {
        double x = villager.getX();
        double y = villager.getY() + villager.getEyeHeight();
        double z = villager.getZ();

        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x, y, z, 6, 0.3, 0.1, 0.3, 0.0);
    }

    private static void markBlockAndInvalidate(ServerLevel level, BlockPos pos, boolean isBed, BlockPos otherHalf) {
        BlockAccessManager.markBlock(level, pos);
        VillagerMemoryHelper.invalidateNearbyVillagers(level, pos, isBed);
        if (isBed && otherHalf != null) {
            BlockAccessManager.markBlock(level, otherHalf);
            VillagerMemoryHelper.invalidateNearbyVillagers(level, otherHalf, isBed);
        }
    }

    private static void unmarkBlockAndInvalidate(ServerLevel level, BlockPos pos, boolean isBed, BlockPos otherHalf, BlockState state) {
        BlockAccessManager.unmarkBlock(level, pos);
        VillagerMemoryHelper.invalidateNearbyVillagers(level, pos, isBed);

        if (isBed && otherHalf != null) {
            BlockAccessManager.unmarkBlock(level, otherHalf);
            VillagerMemoryHelper.invalidateNearbyVillagers(level, otherHalf, isBed);

            // Bed POI release
            var poiManager = level.getPoiManager();
            BlockPos headPos = state.getValue(BedBlock.PART) == BedPart.HEAD ? pos : otherHalf;
            if (poiManager.getType(headPos).isPresent()) poiManager.release(headPos);
        } else if (!isBed) {
            var poiManager = level.getPoiManager();
            if (poiManager.getType(pos).isPresent()) poiManager.release(pos);
        }
    }
}