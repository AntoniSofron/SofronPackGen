package pl.sofron.terralab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public record GiantSpruceVariants(Holder<Feature> original) implements Feature {
    public static final MapCodec<GiantSpruceVariants> CODEC = Feature.CODEC.fieldOf("original")
            .xmap(GiantSpruceVariants::new, GiantSpruceVariants::original);

    @Override
    public MapCodec<GiantSpruceVariants> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        var biome = level.getBiome(origin);
        if (!(biome.is(Biomes.OLD_GROWTH_PINE_TAIGA) || biome.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA))
                || random.nextBoolean()) return original.value().place(level, generator, random, origin);
        if (!(original.value() instanceof TreeFeature tree)) return false;

        int variant = 1 + random.nextInt(2);
        int root = variant == 1 ? 4 : 5;
        var rotation = Rotation.getRandom(random);
        // Keep the four rotated trunk cells on the same 2x2 sapling footprint.
        var diagonal = new BlockPos(1, 0, 1).rotate(rotation);
        var anchor = origin.offset(-Math.min(0, diagonal.getX()), 0, -Math.min(0, diagonal.getZ()));
        if (!TreeTemplates.place(level, random, anchor, tree, "tall_spruce_" + variant,
                new BlockPos(root, 0, root), Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD, Blocks.SPRUCE_LEAVES,
                rotation)) return false;

        // The vanilla giant-tree decorator spreads podzol. Restore the four grass footings.
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) level.setBlock(origin.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 19);
        }
        return true;
    }
}
