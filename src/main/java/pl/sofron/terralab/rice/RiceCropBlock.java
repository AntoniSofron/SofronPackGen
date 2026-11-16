package pl.sofron.terralab.rice;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import pl.sofron.terralab.soil.SoilCropRules;
import pl.sofron.terralab.soil.SoilShape;

/**
 * Eight growth stages, like wheat. Growth probability follows wheat's
 * farmland-fertility formula (including neighbours and crop crowding).
 *
 * Ordinary farmland: half wheat's growth chance, regardless of moisture.
 * Waterlogged farmland slabs/stairs: wheat's normal growth chance.
 * Non-waterlogged shaped farmland: planting is forbidden.
 */
public final class RiceCropBlock extends CropBlock {
    public RiceCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return RiceContent.RICE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState soil, BlockGetter level, BlockPos pos) {
        if (soil.getBlock() instanceof SoilShape shape && shape.soilBase() == Blocks.FARMLAND)
            return SoilCropRules.hasInternalWater(soil);
        return super.mayPlaceOn(soil, level, pos);
    }

    private static float wheatEquivalentFertility(BlockGetter level, BlockPos cropPos, BlockState crop) {
        BlockPos basePos = cropPos.below();
        float fertility = 1.0F;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockState field = level.getBlockState(basePos.offset(dx, 0, dz));
                float bonus = 0.0F;
                boolean farmland = field.is(Blocks.FARMLAND);
                boolean shaped = field.getBlock() instanceof SoilShape soil
                        && soil.soilBase() == Blocks.FARMLAND;
                if (farmland || shaped) {
                    boolean irrigated = SoilCropRules.hasInternalWater(field)
                            || (field.hasProperty(FarmlandBlock.MOISTURE)
                            && field.getValue(FarmlandBlock.MOISTURE) > 0);
                    bonus = irrigated ? 3.0F : 1.0F;
                }
                if (dx != 0 || dz != 0) bonus *= 0.25F;
                fertility += bonus;
            }
        }
        boolean eastWest = level.getBlockState(cropPos.east()).is(crop.getBlock())
                || level.getBlockState(cropPos.west()).is(crop.getBlock());
        boolean northSouth = level.getBlockState(cropPos.north()).is(crop.getBlock())
                || level.getBlockState(cropPos.south()).is(crop.getBlock());
        boolean diagonal = level.getBlockState(cropPos.north().east()).is(crop.getBlock())
                || level.getBlockState(cropPos.north().west()).is(crop.getBlock())
                || level.getBlockState(cropPos.south().east()).is(crop.getBlock())
                || level.getBlockState(cropPos.south().west()).is(crop.getBlock());
        if (diagonal || (eastWest && northSouth)) fertility *= 0.5F;
        return fertility;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isMaxAge(state) || !hasSufficientLight(level, pos)) return;

        BlockState soil = level.getBlockState(pos.below());
        boolean waterField = soil.getBlock() instanceof SoilShape shape
                && shape.soilBase() == Blocks.FARMLAND
                && SoilCropRules.hasInternalWater(soil);
        if (!waterField && !soil.is(Blocks.FARMLAND)) return;

        // Normal farmland needs twice as many random growth attempts.
        if (!waterField && random.nextBoolean()) return;
        float fertility = wheatEquivalentFertility(level, pos, state);
        if (random.nextInt((int)(25.0F / fertility) + 1) == 0)
            level.setBlockAndUpdate(pos, getStateForAge(getAge(state) + 1));
    }
}
