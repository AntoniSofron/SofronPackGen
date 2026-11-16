package pl.sofron.terralab.soil.mixin;

import pl.sofron.terralab.soil.*;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class CropOutlineMixin {
    @Inject(method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"), cancellable = true)
    private void lowerOutline(BlockGetter level, BlockPos pos, CollisionContext ctx, CallbackInfoReturnable<VoxelShape> ci) {
        if (SoilGeometry.lowerCrop((BlockState)(Object)this, level, pos))
            ci.setReturnValue(ci.getReturnValue().move(0, -.5, 0));
    }
}
