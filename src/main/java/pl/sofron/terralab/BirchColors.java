package pl.sofron.terralab;

import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorResolverRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BirchColors implements ClientModInitializer {
    private static final int VANILLA_BIRCH = 0xFF80A755;

    private static final TagKey<Biome> WINTER = TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, "winter_palette"));

    private static boolean usesTaigaLeaves(Biome biome) {
        var level = Minecraft.getInstance().level;
        if (level == null) return false;
        var holder = level.registryAccess().lookupOrThrow(Registries.BIOME).wrapAsHolder(biome);
        return holder.is(SpruceVariants.TAIGAS) || holder.is(WINTER);
    }

    private static final ColorResolver BIRCH = (biome, x, z) -> {
        if (usesTaigaLeaves(biome)) return 0xFFFFCC32;
        var level = Minecraft.getInstance().level;
        if (level != null) {
            var key = level.registryAccess().lookupOrThrow(Registries.BIOME).getResourceKey(biome);
            if (key.isPresent() && (key.get().equals(Biomes.FOREST) || key.get().equals(Biomes.DARK_FOREST))) return 0xFF92BF00;
            if (key.isPresent() && (key.get().equals(Biomes.BIRCH_FOREST)
                    || key.get().equals(Biomes.OLD_GROWTH_BIRCH_FOREST))) return biome.getFoliageColor();
        }
        return VANILLA_BIRCH;
    };

    @Override
    public void onInitializeClient() {
        BlockColorRegistry.register(List.of(BlockTintSources.grass()),
                pl.sofron.terralab.soil.SoilBlocks.slab(Blocks.GRASS_BLOCK),
                pl.sofron.terralab.soil.SoilBlocks.stairs(Blocks.GRASS_BLOCK));
        register(Blocks.BIRCH_LEAVES, VANILLA_BIRCH, BIRCH);
        register(Blocks.OAK_LEAVES, BlockTintSources.foliage().color(Blocks.OAK_LEAVES.defaultBlockState()),
                (biome, x, z) -> usesTaigaLeaves(biome) ? 0xFFC13D42 : biome.getFoliageColor());
    }

    private static void register(Block block, int defaultColor, ColorResolver resolver) {
        ColorResolverRegistry.register(resolver);
        BlockColorRegistry.register(List.of(new BlockTintSource() {
            public int color(BlockState state) { return defaultColor; }

            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return 0xFF000000 | level.getBlockTint(pos, resolver);
            }
        }), block);
    }
}
