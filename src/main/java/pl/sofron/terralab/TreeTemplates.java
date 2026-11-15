package pl.sofron.terralab;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

final class TreeTemplates {
    private TreeTemplates() {}

    static boolean place(WorldGenLevel level, RandomSource random, BlockPos origin,
                                  TreeFeature tree, String path, BlockPos root, Block log, Block woodBlock, Block leavesBlock) {
        return place(level, random, origin, tree, path, root, log, woodBlock, leavesBlock, Rotation.getRandom(random));
    }

    static boolean placeTwoByTwo(WorldGenLevel level, RandomSource random, BlockPos origin, TreeFeature tree,
                                 String path, BlockPos root, Block log, Block wood, Block leaves) {
        var rotation = Rotation.getRandom(random);
        // Anchor the rotated trunk to the four sapling cells.
        var diagonal = new BlockPos(1, 0, 1).rotate(rotation);
        var anchor = origin.offset(-Math.min(0, diagonal.getX()), 0, -Math.min(0, diagonal.getZ()));
        return place(level, random, anchor, tree, path, root, log, wood, leaves, rotation);
    }

    static boolean place(WorldGenLevel level, RandomSource random, BlockPos origin, TreeFeature tree,
                         String path, BlockPos root, Block log, Block woodBlock, Block leavesBlock, Rotation rotation) {
        var id = Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, path);
        var template = level.getLevel().getServer().getStructureTemplateManager().get(id).orElse(null);
        if (template == null || origin.getY() + template.getSize().getY() - 1 > level.getMaxY()) return false;
        var settings = new StructurePlaceSettings().setRotation(rotation);
        var corner = origin.subtract(root.rotate(rotation));
        var wood = template.filterBlocks(corner, settings, log);
        wood.addAll(template.filterBlocks(corner, settings, woodBlock));
        if (wood.isEmpty()) return false;
        for (var block : wood) {
            if (!TreeFeature.validTreePos(level, block.pos())) return false;
        }
        var canopy = template.filterBlocks(corner, settings, leavesBlock);
        if (canopy.isEmpty()) return false;
        if (!supportRoots(level, origin, wood)) return false;
        boolean decorate = !tree.decorators().isEmpty();
        Set<BlockPos> logs = decorate ? new HashSet<>() : Set.of();
        Set<BlockPos> leaves = decorate ? new HashSet<>() : Set.of();
        for (StructureBlockInfo block : wood) {
            level.setBlock(block.pos(), block.state(), 19);
            if (decorate) logs.add(block.pos());
        }
        for (var block : canopy) {
            if (TreeFeature.validTreePos(level, block.pos())) {
                level.setBlock(block.pos(), block.state().setValue(BlockStateProperties.PERSISTENT, false), 19);
                if (decorate) leaves.add(block.pos());
            }
        }
        if (log == Blocks.JUNGLE_LOG) {
            // Place the authored fruit and hanging vines after their supports, before random decorators.
            for (Block attachment : List.of(Blocks.COCOA, Blocks.VINE)) {
                var blocks = template.filterBlocks(corner, settings, attachment);
                blocks.sort(java.util.Comparator.comparingInt((StructureBlockInfo b) -> b.pos().getY()).reversed());
                for (var b : blocks) {
                    if (level.isEmptyBlock(b.pos()) && b.state().canSurvive(level, b.pos())) {
                        level.setBlock(b.pos(), b.state(), 19);
                    }
                }
            }
        }
        if (!decorate) return true;
        var context = new TreeDecorator.Context(level, (pos, state) -> level.setBlock(pos, state, 19),
                random, logs, leaves, Set.of());
        tree.decorators().forEach(decorator -> decorator.place(context));
        return true;
    }

    /** Check the entire rotated footprint before changing any terrain. */
    static boolean supportRoots(WorldGenLevel level, BlockPos origin, List<StructureBlockInfo> wood) {
        var footprint = wood.stream().map(StructureBlockInfo::pos)
                .filter(pos -> pos.getY() == origin.getY()).map(BlockPos::below).toList();
        if (footprint.isEmpty() || origin.getY() <= level.getMinY()) return false;
        for (var pos : footprint) {
            var state = level.getBlockState(pos);
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || level.getBlockEntity(pos) != null) return false;
            if (state.isFaceSturdy(level, pos, Direction.UP)) continue;
            var base = pos.below();
            var ground = level.getBlockState(base);
            if (!state.canBeReplaced() || !ground.isFaceSturdy(level, base, Direction.UP)
                    || ground.is(BlockTags.LEAVES) || ground.is(BlockTags.LOGS)) return false;
        }
        for (var pos : footprint) {
            level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 19);
        }
        return true;
    }
}
