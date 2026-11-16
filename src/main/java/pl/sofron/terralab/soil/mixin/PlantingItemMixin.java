package pl.sofron.terralab.soil.mixin;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.sofron.terralab.soil.SoilShape;

@Mixin(BlockItem.class)
abstract class PlantingItemMixin {
    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private void requireWaterForPlanting(BlockPlaceContext context, BlockState plant, CallbackInfoReturnable<Boolean> result) {
        if (!(plant.getBlock() instanceof VegetationBlock)) return;
        BlockState soil = context.getLevel().getBlockState(context.getClickedPos().below());
        if (SoilShape.isFarmland(soil) && !SoilShape.hasPlantingWater(soil)) result.setReturnValue(false);
    }
}
