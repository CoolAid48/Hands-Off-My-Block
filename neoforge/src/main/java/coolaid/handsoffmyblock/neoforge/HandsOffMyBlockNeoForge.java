package coolaid.handsoffmyblock.neoforge;

import coolaid.handsoffmyblock.HandsOffMyBlock;
import net.neoforged.fml.common.Mod;

@Mod(HandsOffMyBlock.MOD_ID)
public final class HandsOffMyBlockNeoForge {
    public HandsOffMyBlockNeoForge() {
        // Run our common setup.
        HandsOffMyBlock.init();
    }
}
