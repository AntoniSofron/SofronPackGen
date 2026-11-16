package pl.sofron.terralab.soil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public interface SoilShape extends BonemealableBlock {
    Block soilBase();

    static boolean isFarmland(BlockState state) {
        return state.getBlock() instanceof SoilShape soil && soil.soilBase() == Blocks.FARMLAND;
    }

    static boolean hasPlantingWater(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED);
    }

    default boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return soilBase() instanceof BonemealableBlock b && b.isValidBonemealTarget(level, pos, soilBase().defaultBlockState(), source);
    }
    default boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return soilBase() instanceof BonemealableBlock b && b.isBonemealSuccess(level, random, pos, soilBase().defaultBlockState(), source);
    }
    default void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        if (soilBase() instanceof BonemealableBlock b)
            b.performBonemeal(level, random, pos, soilBase().defaultBlockState(), source);
    }
}
