package pl.sofron.terralab.soil;

import java.util.Map;
import pl.sofron.terralab.rice.RiceCropBlock;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.*;

public final class SoilGeometry {
    private static final Map<VoxelShape,VoxelShape> FIELD = new ConcurrentHashMap<>();
    private static final Map<VoxelShape,VoxelShape> MUD = new ConcurrentHashMap<>();

    static VoxelShape lower(VoxelShape shape, int pixels) {
        return (pixels == 1 ? FIELD : MUD).computeIfAbsent(shape, s -> {
            var boxes = s.toAabbs();
            VoxelShape result = Shapes.empty();
            for (int x = 0; x < 2; x++) for (int z = 0; z < 2; z++) {
                double lo = 1, hi = 0;
                for (var b : boxes) {
                    double cx = x * .5 + .25, cz = z * .5 + .25;
                    if (cx > b.minX && cx < b.maxX && cz > b.minZ && cz < b.maxZ) {
                        lo = Math.min(lo, b.minY);
                        hi = Math.max(hi, b.maxY);
                    }
                }
                if (hi > lo) result = Shapes.or(result, Shapes.box(x*.5, lo, z*.5,
                        (x+1)*.5, hi - pixels/16.0, (z+1)*.5));
            }
            return result.optimize();
        });
    }

    public static boolean lowerCrop(BlockState crop, BlockGetter level, BlockPos pos) {
        // Rice stays in the upper cell: WATER_ROOTS extends the stem down to
        // the true soil surface. Lowering the whole plant would double-shift it.
        if (crop.getBlock() instanceof RiceCropBlock) return false;
        if (!(crop.getBlock() instanceof VegetationBlock)) return false;
        if (crop.getValueOrElse(DoublePlantBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)
                == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) pos = pos.below();
        var below = level.getBlockState(pos.below());
        return below.getBlock() instanceof SoilSlab slab && slab.soilBase() == Blocks.FARMLAND
                && below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM && crop.is(BlockTags.MAINTAINS_FARMLAND);
    }
}
