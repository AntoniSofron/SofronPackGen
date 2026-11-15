package pl.sofron.terralab;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public record JungleVariants(Holder<Feature> original, boolean giant) implements Feature {
    public static final MapCodec<JungleVariants> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Feature.CODEC.fieldOf("original").forGetter(JungleVariants::original),
            Codec.BOOL.fieldOf("giant").forGetter(JungleVariants::giant)
    ).apply(instance, JungleVariants::new));

    private static final List<BlockPos> SMALL = List.of(new BlockPos(3, 0, 2),
            new BlockPos(4, 0, 4), new BlockPos(5, 0, 5));
    private static final List<BlockPos> LARGE = List.of(new BlockPos(4, 0, 5), new BlockPos(5, 0, 5));

    @Override
    public MapCodec<JungleVariants> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (random.nextInt(3) == 0) return original.value().place(level, generator, random, origin);
        if (!(original.value() instanceof TreeFeature tree)) return false;
        if (giant) {
            int i = random.nextInt(LARGE.size());
            return TreeTemplates.placeTwoByTwo(level, random, origin, tree, "tall_jungle_tree_" + (i + 1),
                    LARGE.get(i), Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD, Blocks.JUNGLE_LEAVES);
        }
        int i = random.nextInt(SMALL.size());
        return TreeTemplates.place(level, random, origin, tree, "jungle_tree_" + (i + 1),
                SMALL.get(i), Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD, Blocks.JUNGLE_LEAVES);
    }
}
