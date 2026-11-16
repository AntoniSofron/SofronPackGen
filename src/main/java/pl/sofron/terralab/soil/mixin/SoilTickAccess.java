package pl.sofron.terralab.soil.mixin;

import pl.sofron.terralab.soil.*;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockBehaviour.class)
public interface SoilTickAccess {
    @Invoker("randomTick")
    void sofronTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random);
}
