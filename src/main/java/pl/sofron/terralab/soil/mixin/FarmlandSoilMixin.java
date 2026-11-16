package pl.sofron.terralab.soil.mixin;

import pl.sofron.terralab.soil.*;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.level.gameevent.GameEvent;

@Mixin(FarmlandBlock.class)
abstract class FarmlandSoilMixin {
    @Inject(method = "turnToBaseBlock", at = @At("HEAD"), cancellable = true)
    private void keepShape(Entity entity, BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (!(state.getBlock() instanceof SoilShape)) return;
        BlockState dirt = SoilBlocks.convert(state, Blocks.DIRT);
        level.setBlockAndUpdate(pos, Block.pushEntitiesUp(state, dirt, level, pos));
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, dirt));
        ci.cancel();
    }
}
