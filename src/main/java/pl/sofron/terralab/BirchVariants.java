package pl.sofron.terralab;

import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.ShelfMushroomBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

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
            var id = Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, "birch_" + (index + 1));
            var template = level.getLevel().getServer().getStructureTemplateManager().get(id);
            placed = template.isPresent() && placeTemplate(level, random, origin, tree, template.get(), index);
        }
        if (placed && random.nextFloat() < 0.05F) placeMushroom(level, random, origin);
        return placed;
    }

    private boolean placeTemplate(WorldGenLevel level, RandomSource random, BlockPos origin,
                                  TreeFeature tree, StructureTemplate template, int index) {
        var rotation = Rotation.getRandom(random);
        var settings = new StructurePlaceSettings().setRotation(rotation);
        var corner = origin.subtract(ROOTS.get(index).rotate(rotation));
        var wood = template.filterBlocks(corner, settings, Blocks.BIRCH_LOG);
        wood.addAll(template.filterBlocks(corner, settings, Blocks.BIRCH_WOOD));
        var canopy = template.filterBlocks(corner, settings, Blocks.BIRCH_LEAVES);
        if (wood.isEmpty() || canopy.isEmpty() || origin.getY() + template.getSize().getY() - 1 > level.getMaxY()) return false;
        for (var block : wood) {
            if (!TreeFeature.validTreePos(level, block.pos())) return false;
        }
        var below = origin.below();
        var soil = tree.belowTrunkProvider().value().getOptionalState(level, random, below);
        if (soil != null) level.setBlock(below, soil, 19);
        Set<BlockPos> logs = new HashSet<>();
        Set<BlockPos> leaves = new HashSet<>();
        for (StructureBlockInfo block : wood) {
            level.setBlock(block.pos(), block.state(), 19);
            logs.add(block.pos());
        }
        for (var block : canopy) {
            if (TreeFeature.validTreePos(level, block.pos())) {
                level.setBlock(block.pos(), block.state().setValue(BlockStateProperties.PERSISTENT, false), 19);
                leaves.add(block.pos());
            }
        }
        var context = new TreeDecorator.Context(level, (pos, state) -> level.setBlock(pos, state, 19),
                random, logs, leaves, Set.of());
        tree.decorators().forEach(decorator -> decorator.place(context));
        return true;
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
