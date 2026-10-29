package com.electra.mod.block;

import com.electra.mod.blockentity.IronPipeBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tuyau en fer à vitres : transporte un fluide (en mB) entre pompes, réservoirs et machines.
 * Même logique de connexion que les câbles : il se relie aux autres tuyaux et à tout bloc
 * qui expose une capacité de fluide (Electra ou autre mod). Le fluide est visible à travers les vitres.
 */
public class IronPipeBlock extends BaseEntityBlock {

    public static final MapCodec<IronPipeBlock> CODEC = simpleCodec(IronPipeBlock::new);

    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty EAST  = PipeBlock.EAST;
    public static final BooleanProperty WEST  = PipeBlock.WEST;
    public static final BooleanProperty UP    = PipeBlock.UP;
    public static final BooleanProperty DOWN  = PipeBlock.DOWN;

    /** Épaisseur du tuyau en pixels (5 → 11). */
    private static final double THICKNESS = 6;

    private final VoxelShape[] shapes = new VoxelShape[64];

    public IronPipeBlock(Properties properties) {
        super(properties);
        buildShapes();
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ── Formes ─────────────────────────────────────────────────────────────

    private void buildShapes() {
        double min = 8 - THICKNESS / 2;
        double max = 8 + THICKNESS / 2;
        VoxelShape center = Block.box(min, min, min, max, max, max);
        VoxelShape[] arms = new VoxelShape[6];
        for (Direction dir : Direction.values()) {
            arms[dir.ordinal()] = switch (dir) {
                case DOWN  -> Block.box(min, 0, min, max, min, max);
                case UP    -> Block.box(min, max, min, max, 16, max);
                case NORTH -> Block.box(min, min, 0, max, max, min);
                case SOUTH -> Block.box(min, min, max, max, max, 16);
                case WEST  -> Block.box(0, min, min, min, max, max);
                case EAST  -> Block.box(max, min, min, 16, max, max);
            };
        }
        for (int mask = 0; mask < 64; mask++) {
            VoxelShape shape = center;
            for (Direction dir : Direction.values()) {
                if ((mask >> dir.ordinal() & 1) != 0) shape = Shapes.or(shape, arms[dir.ordinal()]);
            }
            shapes[mask] = shape;
        }
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                           @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        int mask = 0;
        for (Direction dir : Direction.values()) {
            if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir))) mask |= 1 << dir.ordinal();
        }
        return shapes[mask];
    }

    // ── États et connexions ────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = defaultBlockState();
        for (Direction dir : Direction.values()) {
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir),
                    canConnectTo(ctx.getLevel(), ctx.getClickedPos(), dir));
        }
        return state;
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction dir,
                                              @NotNull BlockState neighborState, @NotNull LevelAccessor level,
                                              @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), canConnectTo(level, pos, dir));
    }

    protected boolean canConnectTo(LevelAccessor level, BlockPos pos, Direction dir) {
        BlockPos neighborPos = pos.relative(dir);
        if (level.getBlockState(neighborPos).getBlock() instanceof IronPipeBlock) return true;
        // Tout bloc exposant un fluide (Electra ou autre mod)
        if (level instanceof Level realLevel) {
            return realLevel.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, dir.getOpposite()) != null;
        }
        return false;
    }

    // ── Entité de bloc ─────────────────────────────────────────────────────

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new IronPipeBlockEntity(pos, state);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.electra.iron_pipe").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
