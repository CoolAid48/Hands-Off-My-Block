package coolaid.handsoffmyblock.config;

import net.minecraft.resources.Identifier;

public class HandsOffMyConfig {

    public Identifier markerItem = Identifier.parse("minecraft:stick");
    public boolean requireSneaking = true; // Toggle require sneaking to mark
    public boolean enableBedMarking = false; // Toggle beds to be marked
}
