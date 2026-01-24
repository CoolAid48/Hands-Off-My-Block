package coolaid.handsoffmyblock.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import coolaid.handsoffmyblock.util.BlockSets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* NOTES ON DATA REGISTRY BELOW
 * - This is my first attempt at persistent world data that gets automatically saved and loaded, using codecs
 * for serialization (SavedDataType Registration).
 * - Each individual world and dimension has separate data; there are 3 separate handsoffmyblock_marked.dat
 * files for world folder + both DIM folders.
 * - I also implemented a system that cleans up the data when POIs are destroyed while marked (automatically
 * unmarking them and clearing them from the .dat file).
 */

public class MarkedBlocksData extends SavedData {
    private final Set<BlockPos> marked = new HashSet<>();

    // Define a Codec for serialization/deserialization
    private static final Codec<MarkedBlocksData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.LONG.listOf().fieldOf("positions").xmap(
                    longs -> {
                        Set<BlockPos> set = new HashSet<>();
                        for (long l : longs) {
                            set.add(BlockPos.of(l));
                        }
                        return set;
                    },
                    set -> {
                        Set<Long> longs = new HashSet<>();
                        for (BlockPos pos : set) {
                            longs.add(pos.asLong());
                        }
                        return new ArrayList<>(longs);
                    }
            ).forGetter(data -> data.marked)).apply(instance, set -> {
                MarkedBlocksData data = new MarkedBlocksData();
                data.marked.addAll(set);
                return data;
            })
    );

    public static final SavedDataType<MarkedBlocksData> TYPE = new SavedDataType<>(
            "handsoffmyblock_marked", MarkedBlocksData::new, CODEC, null
    );

    public MarkedBlocksData() {
    }

    public static MarkedBlocksData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isMarked(BlockPos pos) {
        return marked.contains(pos);
    }

    public void mark(BlockPos pos) {
        if (marked.add(pos)) {
            setDirty();
        }
    }

    public void unmark(BlockPos pos) {
        if (marked.remove(pos)) {
            setDirty();
        }
    }

    // Optionally get all marked positions
    public Set<BlockPos> getAllMarked() {
        return new HashSet<>(marked);
    }

    public void cleanupInvalidPositions(ServerLevel level) {
        Set<BlockPos> toRemove = new HashSet<>();
        PoiManager poiManager = level.getPoiManager();

        for (BlockPos pos : marked) {
            if (!level.isLoaded(pos)) continue;

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            if (state.isAir() || (!(block instanceof BedBlock) && !BlockSets.WORKSTATIONS.contains(block))) {

                // Remove from Minecraft's POI system if it exists
                if (poiManager.getType(pos).isPresent()) {
                    poiManager.remove(pos);
                }

                // Mark for removal from data
                toRemove.add(pos);
            }
        }

        if (!toRemove.isEmpty()) {
            marked.removeAll(toRemove);
            setDirty();
        }
    }
}