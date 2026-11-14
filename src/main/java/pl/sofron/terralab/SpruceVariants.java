package pl.sofron.terralab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public record SpruceVariants(Holder<Feature> original) implements Feature {
    public static final MapCodec<SpruceVariants> CODEC = Feature.CODEC.fieldOf("original")
            .xmap(SpruceVariants::new, SpruceVariants::original);
    static final TagKey<Biome> TAIGAS = TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, "spruce_variants"));
    private static final BlockPos NARROW_ROOT = new BlockPos(2, 0, 2);

    private static final BlockPos WIDE_ROOT = new BlockPos(3, 0, 3);

    @Override
    public MapCodec<SpruceVariants> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!level.getBiome(origin).is(TAIGAS) || random.nextInt(5) >= 3) {
            return original.value().place(level, generator, random, origin);
        }
        if (!(original.value() instanceof TreeFeature tree)) return false;
        int variant = 1 + random.nextInt(5);
        BlockPos root = variant >= 4 ? WIDE_ROOT : NARROW_ROOT;
        return TreeTemplates.place(level, random, origin, tree, "spruce_" + variant, root,
                Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD, Blocks.SPRUCE_LEAVES);
    }
}
