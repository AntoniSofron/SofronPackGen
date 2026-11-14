package pl.sofron.terralab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public record DarkOakVariants(Holder<Feature> original) implements Feature {
    public static final MapCodec<DarkOakVariants> CODEC = Feature.CODEC.fieldOf("original")
            .xmap(DarkOakVariants::new, DarkOakVariants::original);

    @Override
    public MapCodec<DarkOakVariants> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (random.nextBoolean()) return original.value().place(level, generator, random, origin);
        if (!(original.value() instanceof TreeFeature tree)) return false;
        int variant = 1 + random.nextInt(2);
        var root = variant == 1 ? new BlockPos(4, 0, 3) : new BlockPos(3, 0, 4);
        return TreeTemplates.placeTwoByTwo(level, random, origin, tree, "dark_oak_" + variant, root,
                Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD, Blocks.DARK_OAK_LEAVES);
    }
}
