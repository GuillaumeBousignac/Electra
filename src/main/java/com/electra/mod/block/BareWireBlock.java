package com.electra.mod.block;

import com.electra.mod.blockentity.BareWireBlockEntity;
import com.electra.mod.setup.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BareWireBlock extends BaseEntityBlock {

    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty EAST  = PipeBlock.EAST;
    public static final BooleanProperty WEST  = PipeBlock.WEST;
    public static final BooleanProperty UP    = PipeBlock.UP;
    public static final BooleanProperty DOWN  = PipeBlock.DOWN;

    private static final VoxelShape SHAPE_CENTER = Block.box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape SHAPE_NORTH  = Block.box(6, 6, 0,  10, 10, 6);
    private static final VoxelShape SHAPE_SOUTH  = Block.box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape SHAPE_EAST   = Block.box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape SHAPE_WEST   = Block.box(0,  6, 6, 6,  10, 10);
    private static final VoxelShape SHAPE_UP     = Block.box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape SHAPE_DOWN   = Block.box(6, 0,  6, 10, 6,  10);

    public BareWireBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0f, 2.0f)
                .noOcclusion());
        registerDefaultState(defaultBlockState()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST,  false)
                .setValue(WEST,  false)
                .setValue(UP,    false)
                .setValue(DOWN,  false));
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                        @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        VoxelShape shape = SHAPE_CENTER;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, SHAPE_NORTH);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SHAPE_SOUTH);
        if (state.getValue(EAST))  shape = Shapes.or(shape, SHAPE_EAST);
        if (state.getValue(WEST))  shape = Shapes.or(shape, SHAPE_WEST);
        if (state.getValue(UP))    shape = Shapes.or(shape, SHAPE_UP);
        if (state.getValue(DOWN))  shape = Shapes.or(shape, SHAPE_DOWN);
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return updateConnections(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos());
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction dir,
                                           @NotNull BlockState neighborState, @NotNull LevelAccessor level,
                                           @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        return updateConnections(state, level, pos);
    }

    private BlockState updateConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            state = state.setValue(dirToProperty(dir), canConnectTo(neighbor));
        }
        return state;
    }

    private boolean canConnectTo(BlockState neighbor) {
        return neighbor.getBlock() instanceof BareWireBlock
                || neighbor.getBlock() instanceof LightningCollectorBlock
                || neighbor.getBlock() instanceof AdvancedFurnaceBlock
                || neighbor.getBlock() instanceof RedstoneConverterBlock;
    }

    private BooleanProperty dirToProperty(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST  -> EAST;
            case WEST  -> WEST;
            case UP    -> UP;
            case DOWN  -> DOWN;
        };
    }

    @Override
    protected @Nullable MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BareWireBlockEntity(pos, state);
    }

    @Override
    public void entityInside(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        if (level.isClientSide()) return;
        if (entity instanceof LivingEntity living) {
            living.hurt(level.damageSources().lightningBolt(), 4.0f);
        }
    }
}