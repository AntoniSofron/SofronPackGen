package pl.sofron.terralab.soil.mixin;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import pl.sofron.terralab.soil.SoilShape;

@Mixin(GrassBlock.class)
abstract class GrassBonemealMixin {
    @Redirect(method = "stopBonemealSpread", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private boolean followsGrass(BlockState state, Object grass) {
        return state.is((Block)grass) || state.getBlock() instanceof SoilShape s && s.soilBase() == grass;
    }
}
