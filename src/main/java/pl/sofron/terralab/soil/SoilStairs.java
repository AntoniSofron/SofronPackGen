package pl.sofron.terralab.soil;

import net.minecraft.core.*;
import pl.sofron.terralab.soil.mixin.SoilTickAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

class SoilStairs extends StairBlock implements SoilShape {
    private final Block base;
    SoilStairs(Block base, Properties props) {
        super(base.defaultBlockState(), props);
        this.base = base;
    }
    public Block soilBase() { return base; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        VoxelShape shape = super.getShape(state, level, pos, ctx);
        return base == Blocks.FARMLAND || base == Blocks.DIRT_PATH ? SoilGeometry.lower(shape, 1) : shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return base == Blocks.MUD ? SoilGeometry.lower(super.getShape(state, level, pos, ctx), 2)
                : getShape(state, level, pos, ctx);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = SoilBlocks.surface(super.getStateForPlacement(ctx), ctx.getLevel(), ctx.getClickedPos());
        if (state != null && (base == Blocks.FARMLAND || base == Blocks.DIRT_PATH)
                && !base.defaultBlockState().canSurvive(ctx.getLevel(), ctx.getClickedPos()))
            return SoilBlocks.convert(state, Blocks.DIRT);
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction side, BlockPos neighbor, BlockState adjacent, RandomSource random) {
        state = super.updateShape(state, level, ticks, pos, side, neighbor, adjacent, random);
        if (side != Direction.UP) return state;
        if ((base == Blocks.FARMLAND || base == Blocks.DIRT_PATH) && !base.defaultBlockState().canSurvive(level, pos))
            ticks.scheduleTick(pos, this, 1);
        if (base == Blocks.FARMLAND)
            SoilCropRules.onTopChanged(state, level, ticks, pos, adjacent);
        return SoilBlocks.surface(state, level, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        SoilBlocks.checkCover(base, state, level, pos);
        if (base == Blocks.FARMLAND && level.getBlockState(pos).getBlock() == this)
            SoilCropRules.onFieldTick(level, pos, level.getBlockState(pos));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ((SoilTickAccess)base).sofronTick(state, level, pos, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (base == Blocks.MYCELIUM) base.animateTick(state, level, pos, random);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double distance) {
        if (base == Blocks.FARMLAND) base.fallOn(level, state, pos, entity, distance);
        else super.fallOn(level, state, pos, entity, distance);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = SoilBlocks.tool(stack, state, level, pos, player, hand, hit, base);
        return result == InteractionResult.PASS ? super.useItemOn(stack, state, level, pos, player, hand, hit) : result;
    }
}

final class FarmStairs extends SoilStairs {
    FarmStairs(Block base, Properties props) {
        super(base, props);
        registerDefaultState(defaultBlockState().setValue(FarmlandBlock.MOISTURE, 0));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FarmlandBlock.MOISTURE);
    }
}

final class SnowyStairs extends SoilStairs {
    SnowyStairs(Block base, Properties props) {
        super(base, props);
        registerDefaultState(defaultBlockState().setValue(SnowyBlock.SNOWY, false));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SnowyBlock.SNOWY);
    }
}
