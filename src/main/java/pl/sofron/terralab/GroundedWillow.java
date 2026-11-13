package pl.sofron.terralab;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/** Anchor swamp templates at the trunk, including when the template rotates in shallow water. */
public final class GroundedWillow implements Feature {
    public static final MapCodec<GroundedWillow> CODEC = MapCodec.unit(new GroundedWillow());
    private static final List<BlockPos> ROOTS = List.of(new BlockPos(5, 0, 5), new BlockPos(5, 0, 3),
            new BlockPos(5, 0, 6), new BlockPos(5, 0, 6), new BlockPos(5, 0, 5));

    @Override
    public MapCodec<GroundedWillow> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int index = random.nextInt(ROOTS.size());
        var id = Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, "swamp_willow_" + (index + 1));
        var template = level.getLevel().getServer().getStructureTemplateManager().get(id).orElse(null);
        if (template == null || origin.getY() + template.getSize().getY() - 1 > level.getMaxY()) return false;
        var rotation = Rotation.getRandom(random);
        var settings = new StructurePlaceSettings().setRandom(random).setRotation(rotation);
        var corner = origin.subtract(ROOTS.get(index).rotate(rotation));
        var wood = template.filterBlocks(corner, settings, Blocks.OAK_LOG);
        wood.addAll(template.filterBlocks(corner, settings, Blocks.OAK_WOOD));
        if (!TreeTemplates.supportRoots(level, origin, wood)) return false;
        return template.placeInWorld(level, corner, corner, settings, random, 3);
    }
}
