package pl.sofron.terralab;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShelfMushroomBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

/** The placed feature supplies the local vanilla birch, including its bee and litter decorators. */
public record BirchVariants(Holder<Feature> original) implements Feature {
    public static final MapCodec<BirchVariants> CODEC = Feature.CODEC.fieldOf("original")
            .xmap(BirchVariants::new, BirchVariants::original);
    private static final List<BlockPos> ROOTS = List.of(new BlockPos(4, 0, 4), new BlockPos(4, 0, 4),
            new BlockPos(4, 0, 3), new BlockPos(5, 0, 5), new BlockPos(5, 0, 5));

    @Override
    public MapCodec<BirchVariants> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!(original.value() instanceof TreeFeature tree)) return false;
        // Three short tickets, two tall tickets, and two per authored silhouette.
        int ticket = random.nextInt(15);
        boolean placed;
        if (ticket < 5) {
            int height = ticket < 3 ? 5 : 13;
            var trunk = new StraightTrunkPlacer(height, ticket < 3 ? 2 : 0, 0);
            var variant = new TreeFeature(tree.trunkProvider(), trunk, tree.foliageProvider(),
                    tree.foliagePlacer(), tree.rootPlacer(), tree.minimumSize(), tree.decorators(),
                    tree.ignoreVines(), tree.belowTrunkProvider());
            placed = variant.place(level, generator, random, origin);
        } else {
            int index = (ticket - 5) / 2;
            placed = TreeTemplates.place(level, random, origin, tree, "birch_" + (index + 1),
                    ROOTS.get(index), Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD, Blocks.BIRCH_LEAVES);
        }
        if (placed && random.nextFloat() < 0.05F) placeMushroom(level, random, origin);
        return placed;
    }

    private void placeMushroom(WorldGenLevel level, RandomSource random, BlockPos origin) {
        // Try all 16 faces in random order until one can support the mushroom.
        var faces = new java.util.ArrayList<BlockPos>();
        for (int y = 1; y <= 4; y++) faces.add(origin.above(y));
        net.minecraft.util.Util.shuffle(faces, random);
        for (var log : faces) {
            if (!level.getBlockState(log).is(Blocks.BIRCH_LOG)) continue;
            for (var direction : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                var target = log.relative(direction);
                var state = Blocks.SHELF_MUSHROOM.defaultBlockState()
                        .setValue(ShelfMushroomBlock.FACING, direction)
                        .setValue(ShelfMushroomBlock.AGE, random.nextInt(2));
                if (level.isEmptyBlock(target) && state.canSurvive(level, target)) {
                    level.setBlock(target, state, 19);
                    return;
                }
            }
        }
    }
}
