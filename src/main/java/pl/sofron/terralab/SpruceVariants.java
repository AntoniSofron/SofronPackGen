package pl.sofron.terralab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public record SpruceVariants(Holder<Feature> original) implements Feature {
    public static final MapCodec<SpruceVariants> CODEC = Feature.CODEC.fieldOf("original")
            .xmap(SpruceVariants::new, SpruceVariants::original);
    static final TagKey<Biome> TAIGAS = TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, "spruce_variants"));
    private static final TagKey<Biome> MIXED_CONIFERS = TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, "mixed_conifers"));
    private static final ResourceKey<Feature> VANILLA_PINE = ResourceKey.create(Registries.FEATURE,
            Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, "vanilla_pine"));
    private static final ResourceKey<Feature> VANILLA_SPRUCE = ResourceKey.create(Registries.FEATURE,
            Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, "vanilla_spruce"));
    private static final BlockPos NARROW_ROOT = new BlockPos(2, 0, 2);

    private static final BlockPos WIDE_ROOT = new BlockPos(3, 0, 3);

    @Override
    public MapCodec<SpruceVariants> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        var biome = level.getBiome(origin);
        boolean mixed = biome.is(MIXED_CONIFERS);
        if (!mixed && !biome.is(TAIGAS)) return original.value().place(level, generator, random, origin);

        boolean pine = biome.is(Biomes.OLD_GROWTH_PINE_TAIGA);
        Feature vanilla = original.value();
        if (mixed) {
            // Pick the species first so the 3:1 ratio also applies to vanilla trees.
            pine = random.nextInt(4) < 3;
            vanilla = level.registryAccess().lookupOrThrow(Registries.FEATURE)
                    .getOrThrow(pine ? VANILLA_PINE : VANILLA_SPRUCE).value();
        }
        if (random.nextInt(5) >= 3) return vanilla.place(level, generator, random, origin);
        if (!(vanilla instanceof TreeFeature tree)) return false;
        int variant = 1 + random.nextInt(pine ? 3 : 5);
        BlockPos root = variant >= (pine ? 3 : 4) ? WIDE_ROOT : NARROW_ROOT;
        String template = (pine ? "pine_" : "spruce_") + variant;
        return TreeTemplates.place(level, random, origin, tree, template, root,
                Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD, Blocks.SPRUCE_LEAVES);
    }
}
