package pl.sofron.terralab.rice;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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
    // The rice above waterlogged shaped farmland carries additional visual
    // geometry extending BELOW the crop block into the half-height water gap.
    // This is a render-only blockstate property, not a separate collision block.
    public static final BooleanProperty WATER_ROOTS = BooleanProperty.create("water_roots");

    public RiceCropBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATER_ROOTS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATER_ROOTS);
    }

    private static boolean needsWaterRoots(BlockState soil) {
        return soil.getBlock() instanceof SoilShape shaped
                && shaped.soilBase() == Blocks.FARMLAND
                && SoilCropRules.hasInternalWater(soil);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(WATER_ROOTS,
                needsWaterRoots(context.getLevel().getBlockState(context.getClickedPos().below())));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction side, BlockPos neighbor,
                                     BlockState adjacent, RandomSource random) {
        BlockState next = super.updateShape(state, level, ticks, pos, side, neighbor, adjacent, random);
        if (side == Direction.DOWN && next.is(this))
            return next.setValue(WATER_ROOTS, needsWaterRoots(adjacent));
        return next;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos,
                           BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        // Also covers farmer-villager planting, which can bypass BlockItem.
        if (!level.isClientSide() && !oldState.is(this)) {
            boolean expected = needsWaterRoots(level.getBlockState(pos.below()));
            if (state.getValue(WATER_ROOTS) != expected)
                level.setBlockAndUpdate(pos, state.setValue(WATER_ROOTS, expected));
        }
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random,
                                BlockPos pos, BlockState state, BonemealSource source) {
        super.performBonemeal(level, random, pos, state, source);
        // Vanilla bonemeal creates a fresh age state; keep the visual roots.
        BlockState grown = level.getBlockState(pos);
        if (grown.is(this) && grown.getValue(WATER_ROOTS) != state.getValue(WATER_ROOTS))
            level.setBlockAndUpdate(pos, grown.setValue(WATER_ROOTS, state.getValue(WATER_ROOTS)));
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
            level.setBlockAndUpdate(pos, getStateForAge(getAge(state) + 1)
                    .setValue(WATER_ROOTS, state.getValue(WATER_ROOTS)));
    }
}
