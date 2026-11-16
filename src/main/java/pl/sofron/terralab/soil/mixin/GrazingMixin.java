package pl.sofron.terralab.soil.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import pl.sofron.terralab.soil.*;

@Mixin(EatBlockGoal.class)
abstract class GrazingMixin {
    @Shadow @Final private Level level;

    @Redirect(method = {"canUse", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"))
    private BlockPos grassUnderFeet(BlockPos feet) {
        return level.getBlockState(feet).getBlock() instanceof SoilShape s && s.soilBase() == Blocks.GRASS_BLOCK
                ? feet : feet.below();
    }

    @Redirect(method = {"canUse", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private boolean edibleGrass(BlockState state, Object block) {
        return state.is((net.minecraft.world.level.block.Block)block)
                || state.getBlock() instanceof SoilShape s && s.soilBase() == block;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean graze(Level level, BlockPos pos, BlockState dirt, int flags) {
        var previous = level.getBlockState(pos);
        return level.setBlock(pos, previous.getBlock() instanceof SoilShape
                ? SoilBlocks.convert(previous, Blocks.DIRT) : dirt, flags);
    }
}
