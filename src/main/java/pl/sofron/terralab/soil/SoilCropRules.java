package pl.sofron.terralab.soil;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import pl.sofron.terralab.TerraLabMod;

/**
 * Farmland shape planting rules. Moisture (0..7) is intentionally NOT used:
 * only the actual WATERLOGGED property controls planting.
 *
 * Future rice crops may be added to persistent_water_farmland_crops; they
 * will be allowed to stay and grow on waterlogged farmland slabs/stairs.
 */
public final class SoilCropRules {
    private static final int EJECTION_DELAY_TICKS = 80; // 4 seconds, 20 TPS
    public static final TagKey<Block> PERSISTENT_WATER_CROPS = TagKey.create(
            Registries.BLOCK, Identifier.fromNamespaceAndPath(
                    TerraLabMod.MOD_ID, "persistent_water_farmland_crops"));

    private record Pending(Block crop, long dueTime) {}
    // World references are weak; positions are forgotten when the crop disappears.
    private static final Map<ServerLevel, Map<BlockPos, Pending>> PENDING = new WeakHashMap<>();

    private SoilCropRules() {}

    public static boolean hasInternalWater(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED);
    }

    public static boolean isDryShapedField(BlockState state) {
        return state.getBlock() instanceof SoilShape soil
                && soil.soilBase() == Blocks.FARMLAND
                && !hasInternalWater(state);
    }

    private static boolean isTemporaryCrop(BlockState state) {
        return state.is(BlockTags.MAINTAINS_FARMLAND)
                && !state.is(PERSISTENT_WATER_CROPS);
    }

    private static void clear(ServerLevel level, BlockPos pos) {
        Map<BlockPos, Pending> pending = PENDING.get(level);
        if (pending != null) {
            pending.remove(pos);
            if (pending.isEmpty()) PENDING.remove(level);
        }
    }

    /**
     * Called when a crop above a shaped field is planted, changed or removed.
     * A pending clock is preserved across growth stage changes; replanting
     * after the previous crop was removed creates a fresh deadline.
     */
    static void onTopChanged(BlockState field, LevelReader level, ScheduledTickAccess ticks,
                             BlockPos fieldPos, BlockState above) {
        if (!(level instanceof ServerLevel server)) return;

        if (!hasInternalWater(field) || !isTemporaryCrop(above)) {
            clear(server, fieldPos);
            return;
        }

        Map<BlockPos, Pending> pending = PENDING.computeIfAbsent(server, ignored -> new HashMap<>());
        Pending old = pending.get(fieldPos);
        if (old != null && old.crop() == above.getBlock()) return;

        pending.put(fieldPos.immutable(), new Pending(above.getBlock(),
                server.getGameTime() + EJECTION_DELAY_TICKS));
        ticks.scheduleTick(fieldPos, field.getBlock(), EJECTION_DELAY_TICKS);
    }

    /**
     * Uses block scheduled ticks instead of per-tick polling. If an older
     * scheduled tick fires after replanting, reschedule to the NEW deadline.
     * Minecraft saves scheduled block ticks, so after a restart an untracked
     * scheduled crop is still ejected rather than remaining permanently.
     */
    static void onFieldTick(ServerLevel level, BlockPos fieldPos, BlockState field) {
        BlockPos cropPos = fieldPos.above();
        BlockState crop = level.getBlockState(cropPos);
        if (!hasInternalWater(field) || !isTemporaryCrop(crop)) {
            clear(level, fieldPos);
            return;
        }

        Map<BlockPos, Pending> pending = PENDING.get(level);
        Pending entry = pending == null ? null : pending.get(fieldPos);
        if (entry != null && entry.crop() == crop.getBlock()) {
            long left = entry.dueTime() - level.getGameTime();
            if (left > 0) {
                level.scheduleTick(fieldPos, field.getBlock(), (int)Math.min(left, EJECTION_DELAY_TICKS));
                return;
            }
        }

        clear(level, fieldPos);
        // Normal vanilla drops: wheat/beetroot seeds, carrots, potatoes, etc.
        level.destroyBlock(cropPos, true);
    }
}
