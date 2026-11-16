package pl.sofron.terralab.soil.mixin;

import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import pl.sofron.terralab.soil.SoilShape;

@Mixin(HarvestFarmland.class)
abstract class FarmerMixin {
    @Redirect(method = {"validPos", "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;J)V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private Block recognizeField(BlockState state) {
        Block block = state.getBlock();
        return block instanceof SoilShape soil && soil.soilBase() == Blocks.FARMLAND ? Blocks.FARMLAND : block;
    }
}
