package pl.sofron.terralab.soil;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.tags.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import pl.sofron.terralab.TerraLabMod;

public final class SoilBlocks {
    private record Shapes(Block slab, Block stairs) {}
    private static final Map<Block, Shapes> SHAPES = new LinkedHashMap<>();

    public static void register() {
        for (Block base : new Block[]{Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.DIRT_PATH, Blocks.MYCELIUM,
                Blocks.PODZOL, Blocks.MUD, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.FARMLAND, Blocks.CLAY}) {
            SHAPES.put(base, new Shapes(registerShape(base, false), registerShape(base, true)));
        }
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("natural_blocks"))).register(output -> SHAPES.values().forEach(pair -> {
                    output.accept(pair.slab);
                    output.accept(pair.stairs);
                    if (pair.slab == slab(Blocks.FARMLAND)) {
                        for (Block block : new Block[]{pair.slab, pair.stairs}) {
                            ItemStack wet = new ItemStack(block);
                            wet.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,
                                    net.minecraft.world.item.component.BlockItemStateProperties.EMPTY.with(FarmlandBlock.MOISTURE, 7));
                            output.accept(wet);
                        }
                    }
                }));
    }

    private static Block registerShape(Block base, boolean stairs) {
        String name = BuiltInRegistries.BLOCK.getKey(base).getPath() + (stairs ? "_stairs" : "_slab");
        Identifier id = Identifier.fromNamespaceAndPath(TerraLabMod.MOD_ID, name);
        var key = ResourceKey.create(Registries.BLOCK, id);
        var props = BlockBehaviour.Properties.ofFullCopy(base).noOcclusion().setId(key);
        // Full paths and farmland opt into suffocation, which pushes players off recessed steps.
        if (base == Blocks.DIRT_PATH || base == Blocks.FARMLAND)
            props.isSuffocating((state, level, pos) -> false);
        Block block;
        if (base == Blocks.FARMLAND) block = stairs ? new FarmStairs(base, props) : new FarmSlab(base, props);
        else if (base.defaultBlockState().hasProperty(SnowyBlock.SNOWY))
            block = stairs ? new SnowyStairs(base, props) : new SnowySlab(base, props);
        else block = stairs ? new SoilStairs(base, props) : new SoilSlab(base, props);
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block,
                new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix()));
        return block;
    }

    public static Block slab(Block base) { return SHAPES.get(base).slab; }
    public static Block stairs(Block base) { return SHAPES.get(base).stairs; }

    public static BlockState convert(BlockState source, Block base) {
        Shapes shapes = SHAPES.get(base);
        return (source.getBlock() instanceof StairBlock ? shapes.stairs : shapes.slab).withPropertiesOf(source);
    }

    public static BlockState surface(BlockState state, LevelReader level, BlockPos pos) {
        if (state == null) return null;
        if (state.hasProperty(SnowyBlock.SNOWY))
            return state.setValue(SnowyBlock.SNOWY, level.getBlockState(pos.above()).is(BlockTags.SNOW));
        return state;
    }

    static void checkCover(Block base, BlockState state, ServerLevel level, BlockPos pos) {
        if ((base == Blocks.FARMLAND || base == Blocks.DIRT_PATH)
                && !base.defaultBlockState().canSurvive(level, pos))
            level.setBlockAndUpdate(pos, convert(state, Blocks.DIRT));
    }

    static InteractionResult tool(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit, Block base) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        var contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        if (stack.is(Items.POTION) && contents != null && contents.is(net.minecraft.world.item.alchemy.Potions.WATER)
                && base.defaultBlockState().is(BlockTags.CONVERTIBLE_TO_MUD) && hit.getDirection() != Direction.DOWN) {
            if (!level.isClientSide()) {
                BlockState next = convert(state, Blocks.MUD);
                level.setBlockAndUpdate(pos, next);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, next));
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            }
            level.playSound(player, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1, 1);
            return InteractionResult.SUCCESS;
        }
        boolean hoe = stack.is(ItemTags.HOES);
        Block target;
        if (hoe && base == Blocks.ROOTED_DIRT) target = Blocks.DIRT;
        else {
            if (hit.getDirection() == Direction.DOWN || !level.isEmptyBlock(pos.above())) return InteractionResult.PASS;
            if (hoe && base == Blocks.COARSE_DIRT) target = Blocks.DIRT;
            else if (hoe && base.defaultBlockState().is(BlockTags.TURNS_INTO_FARMLAND)) target = Blocks.FARMLAND;
            else if (stack.is(ItemTags.SHOVELS) && base.defaultBlockState().is(BlockTags.TURNS_INTO_DIRT_PATH))
                target = Blocks.DIRT_PATH;
            else return InteractionResult.PASS;
        }
        level.playSound(player, pos, (hoe ? SoundEvents.HOE_TILL : SoundEvents.SHOVEL_FLATTEN).value(), SoundSource.BLOCKS, 1, 1);
        if (!level.isClientSide()) {
            BlockState next = convert(state, target);
            level.setBlockAndUpdate(pos, next);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, next));
            if (hoe && base == Blocks.ROOTED_DIRT)
                Block.popResourceFromFace(level, pos, hit.getDirection(), new ItemStack(Items.HANGING_ROOTS));
            stack.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }
}
