package pl.sofron.terralab.soil.mixin;

import pl.sofron.terralab.soil.*;

import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ModelBlockRenderer.class)
abstract class CropRenderMixin {
    @ModifyVariable(method = "tesselateBlock", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float lowerCrop(float value, BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level,
                            BlockPos pos, BlockState state, BlockStateModel model, long seed) {
        return SoilGeometry.lowerCrop(state, level, pos) ? value - .5f : value;
    }
}
