package coolaid.handsoffmyblock.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;

public class HandsOffMyVillagerMemoryHelper {

    private static final double RANGE = 64.0D;

    public static void invalidateNearbyVillagers(ServerLevel level, BlockPos pos, boolean isBed) {
        AABB box = new AABB(pos).inflate(RANGE);

        for (Villager villager : level.getEntitiesOfClass(Villager.class, box)) {

            // Clear job/home memories
            villager.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
            villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
            if (isBed) villager.getBrain().eraseMemory(MemoryModuleType.HOME);

            // Stop pathing immediately
            PathNavigation nav = villager.getNavigation();
            if (nav != null) nav.stop();
        }
    }
}
