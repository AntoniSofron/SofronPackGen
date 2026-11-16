package pl.sofron.terralab.soil.mixin;

import pl.sofron.terralab.soil.*;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(SpreadingSnowyBlock.class)
abstract class SpreadingSoilMixin {
    @Redirect(method = "randomTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private boolean acceptsDirtShape(BlockState target, Object base) {
        return target.is((Block)base) || target.getBlock() instanceof SoilShape s && s.soilBase() == base;
    }

    @Redirect(method = "randomTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean keepShape(ServerLevel level, BlockPos pos, BlockState next) {
        BlockState previous = level.getBlockState(pos);
        if (previous.getBlock() instanceof SoilShape)
            next = SoilBlocks.surface(SoilBlocks.convert(previous, next.getBlock()), level, pos);
        return level.setBlockAndUpdate(pos, next);
    }
}
