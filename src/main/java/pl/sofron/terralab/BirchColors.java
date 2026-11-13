package pl.sofron.terralab;

import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorResolverRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BirchColors implements ClientModInitializer {
    private static final int VANILLA = 0xFF80A755;
    private static final ColorResolver BIRCH = (biome, x, z) -> {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            var key = level.registryAccess().lookupOrThrow(Registries.BIOME).getResourceKey(biome);
            if (key.isPresent() && (key.get().equals(Biomes.BIRCH_FOREST)
                    || key.get().equals(Biomes.OLD_GROWTH_BIRCH_FOREST))) return biome.getFoliageColor();
        }
        return VANILLA;
    };

    @Override
    public void onInitializeClient() {
        ColorResolverRegistry.register(BIRCH);
        BlockColorRegistry.register(List.of(new BlockTintSource() {
            public int color(BlockState state) { return VANILLA; }

            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return 0xFF000000 | level.getBlockTint(pos, BIRCH);
            }
        }), Blocks.BIRCH_LEAVES);
    }
}
