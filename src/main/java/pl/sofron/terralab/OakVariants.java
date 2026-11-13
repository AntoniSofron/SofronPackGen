package pl.sofron.terralab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public record OakVariants(Holder<Feature> original) implements Feature {
    public static final MapCodec<OakVariants> CODEC = Feature.CODEC.fieldOf("original")
            .xmap(OakVariants::new, OakVariants::original);
    private static final ResourceKey<Feature> SMALL = ResourceKey.create(Registries.FEATURE,
            Identifier.withDefaultNamespace("oak"));
    private static final ResourceKey<Feature> TALL = ResourceKey.create(Registries.FEATURE,
            Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, "tall_oak"));

    @Override
    public MapCodec<OakVariants> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        var biome = level.getBiome(origin);
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
            return original.value().place(level, generator, random, origin);
        }
        if (!(original.value() instanceof TreeFeature local)) return false;

        int ticket = random.nextInt(15);
        if (ticket >= 7) {
            boolean first = ticket < 11;
            return TreeTemplates.place(level, random, origin, local, first ? "oak_1" : "oak_2",
                    first ? new BlockPos(6, 0, 6) : new BlockPos(6, 0, 7),
                    Blocks.OAK_LOG, Blocks.OAK_WOOD, Blocks.OAK_LEAVES);
        }
        var shapeKey = ticket < 3 ? SMALL : TALL;
        var shape = level.registryAccess().lookupOrThrow(Registries.FEATURE).getOrThrow(shapeKey).value();
        if (!(shape instanceof TreeFeature tree)) return false;
        var oak = new TreeFeature(local.trunkProvider(), tree.trunkPlacer(), local.foliageProvider(),
                tree.foliagePlacer(), tree.rootPlacer(), tree.minimumSize(), local.decorators(),
                local.ignoreVines(), local.belowTrunkProvider());
        return oak.place(level, generator, random, origin);
    }
}
