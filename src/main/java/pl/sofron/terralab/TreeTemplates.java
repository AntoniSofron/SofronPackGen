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
        var id = Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, path);
        var template = level.getLevel().getServer().getStructureTemplateManager().get(id).orElse(null);
        if (template == null) return false;
        var rotation = Rotation.getRandom(random);
        var settings = new StructurePlaceSettings().setRotation(rotation);
        var corner = origin.subtract(root.rotate(rotation));
        var wood = template.filterBlocks(corner, settings, log);
        wood.addAll(template.filterBlocks(corner, settings, woodBlock));
        var canopy = template.filterBlocks(corner, settings, leavesBlock);
        if (wood.isEmpty() || canopy.isEmpty() || origin.getY() + template.getSize().getY() - 1 > level.getMaxY()) return false;
        for (var block : wood) {
            if (!TreeFeature.validTreePos(level, block.pos())) return false;
        }
        if (!supportRoots(level, origin, wood)) return false;
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
