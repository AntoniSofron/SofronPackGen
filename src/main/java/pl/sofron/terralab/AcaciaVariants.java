package pl.sofron.terralab;

import java.util.List;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public record AcaciaVariants(Holder<Feature> original) implements Feature {
    public static final MapCodec<AcaciaVariants> CODEC = Feature.CODEC.fieldOf("original")
            .xmap(AcaciaVariants::new, AcaciaVariants::original);

    private static final List<BlockPos> ROOTS = List.of(
            new BlockPos(5, 0, 6), new BlockPos(3, 0, 4), new BlockPos(5, 0, 4));

    @Override
    public MapCodec<AcaciaVariants> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int choice = random.nextInt(4);
        if (choice == 0) return original.value().place(level, generator, random, origin);
        if (!(original.value() instanceof TreeFeature tree)) return false;
        return TreeTemplates.place(level, random, origin, tree, "acacia_" + choice, ROOTS.get(choice - 1),
                Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD, Blocks.ACACIA_LEAVES);
    }
}
