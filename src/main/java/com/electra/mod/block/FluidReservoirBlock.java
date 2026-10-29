package com.electra.mod.block;

import com.electra.mod.blockentity.FluidReservoirBlockEntity;
import com.electra.mod.setup.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Réservoir à fluides : une cuve en fer à vitres, le niveau du liquide se voit de l'extérieur.
 * Les réservoirs qui se touchent forment une seule grande cuve (voir FluidReservoirBlockEntity).
 * Se remplit et se vide au seau (clic droit) ou par les tuyaux. Il ne donne aux tuyaux
 * que ce que les machines raccordées consomment, et reprend le surplus des pompes.
 */
public class FluidReservoirBlock extends BaseEntityBlock {

    public static final MapCodec<FluidReservoirBlock> CODEC = simpleCodec(FluidReservoirBlock::new);

    /** Vrai quand un autre réservoir touche cette face : la vitre et le cadre disparaissent pour former une seule cuve. */
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty EAST  = PipeBlock.EAST;
    public static final BooleanProperty WEST  = PipeBlock.WEST;
    public static final BooleanProperty UP    = PipeBlock.UP;
    public static final BooleanProperty DOWN  = PipeBlock.DOWN;

    public FluidReservoirBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false).setValue(EAST, false)
                .setValue(WEST, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = defaultBlockState();
        for (Direction dir : Direction.values()) {
            BlockState neighbor = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(dir));
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), neighbor.is(this));
        }
        return state;
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction dir,
                                              @NotNull BlockState neighborState, @NotNull LevelAccessor level,
                                              @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), neighborState.is(this));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FluidReservoirBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.FLUID_RESERVOIR_BE.get(), (lvl, pos, st, be) -> be.tick());
    }

    /** Seau (ou autre récipient à fluide) : remplit ou vide le réservoir. Accroupi, on pose le seau à côté. */
    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state,
                                                       @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand,
                                                       @NotNull BlockHitResult hit) {
        if (FluidUtil.getFluidHandler(stack.copyWithCount(1)).isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection());
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    // ── Comparateur : niveau de remplissage ────────────────────────────────

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof FluidReservoirBlockEntity reservoir)) return 0;
        return reservoir.comparatorSignal();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.electra.fluid_reservoir").withStyle(ChatFormatting.GRAY));
    }
}
