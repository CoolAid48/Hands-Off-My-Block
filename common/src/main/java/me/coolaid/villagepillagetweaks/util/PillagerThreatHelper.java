package me.coolaid.villagepillagetweaks.util;

import me.coolaid.villagepillagetweaks.config.ConfigManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.item.Items;

public final class PillagerThreatHelper {
    private PillagerThreatHelper() {
    }

    public static boolean shouldIgnoreUnarmedPillager(Entity entity) {
        return ConfigManager.get().ignoreUnarmedPillagers
                && entity instanceof Pillager pillager
                && !pillager.isHolding(Items.CROSSBOW);
    }
}
