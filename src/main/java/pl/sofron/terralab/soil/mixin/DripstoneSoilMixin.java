package pl.sofron.terralab.soil.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import pl.sofron.terralab.soil.*;

@Mixin(PointedDripstoneBlock.class)
abstract class DripstoneSoilMixin {
    @Redirect(method = {"maybeTransferFluid", "lambda$getFluidAboveStalactite$0"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private static boolean recognizesMud(BlockState state, Object block) {
        return state.is((Block)block) || state.getBlock() instanceof SoilShape soil && soil.soilBase() == block;
    }

    @Redirect(method = "maybeTransferFluid", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;pushEntitiesUp(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState raiseEntities(BlockState previous, BlockState clay,
                                            net.minecraft.world.level.LevelAccessor level, BlockPos pos) {
        if (previous.getBlock() instanceof SoilShape) clay = SoilBlocks.convert(previous, Blocks.CLAY);
        return Block.pushEntitiesUp(previous, clay, level, pos);
    }

    @Redirect(method = "maybeTransferFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean dryMud(ServerLevel level, BlockPos pos, BlockState clay) {
        var previous = level.getBlockState(pos);
        return level.setBlockAndUpdate(pos, previous.getBlock() instanceof SoilShape
                ? SoilBlocks.convert(previous, Blocks.CLAY) : clay);
    }
}
