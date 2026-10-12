package com.electra.mod.block;

import com.electra.mod.blockentity.RedstoneDynamoBlockEntity;
import com.electra.mod.setup.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Redstone Dynamo : cœur d'un multibloc.
 *
 *   Vu de dessus (un seul niveau)
 *     .  R  R  R  .      R = blocs de redstone (12 ; les 4 coins « . » sont facultatifs)
 *     R  ·  ·  ·  R      · = couloir : 8 cases vides où passe le fer rouge
 *     R  F──D  ·  R      D = Dynamo ; F = bloc de fer rouge posé contre un côté de la Dynamo
 *     R  ·  ·  ·  R
 *     .  R  R  R  .
 *
 * Le fer rouge s'accroche au bras de la Dynamo puis tourne dans le sens horaire quand la Dynamo
 * reçoit un signal redstone (levier posé dessus, par exemple). L'énergie sort par le dessous.
 * Clic droit accroupi, main vide : décroche le fer rouge.
 *
 * La structure peut aussi être verticale : l'axe de la Dynamo (AXIS) dépend de la face sur laquelle
 * on la pose, comme une bûche. L'anneau et le couloir se trouvent dans le plan perpendiculaire à cet axe ;
 * le signal et le câble se branchent sur les deux extrémités de l'axe.
 */
public class RedstoneDynamoBlock extends BaseEntityBlock {

    public static final MapCodec<RedstoneDynamoBlock> CODEC = simpleCodec(RedstoneDynamoBlock::new);

    /** Vrai quand l'anneau est complet et que le fer rouge est accroché au bras. */
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    /** Axe de rotation du bras : Y = structure horizontale, X ou Z = structure verticale. */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public RedstoneDynamoBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FORMED, false).setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED, AXIS);
    }

    /** Comme une bûche : posée contre un mur, l'axe est horizontal. */
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    protected @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        Direction.Axis axis = state.getValue(AXIS);
        boolean swap = (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90)
                && axis != Direction.Axis.Y;
        return swap ? state.setValue(AXIS, axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X) : state;
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
        return new RedstoneDynamoBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        // Serveur : structure, énergie. Client : rotation fluide du bras.
        if (level.isClientSide()) {
            return createTickerHelper(type, ModBlockEntities.REDSTONE_DYNAMO_BE.get(),
                    (lvl, pos, st, be) -> be.clientTick());
        }
        return createTickerHelper(type, ModBlockEntities.REDSTONE_DYNAMO_BE.get(),
                (lvl, pos, st, be) -> be.serverTick());
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                   @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RedstoneDynamoBlockEntity dynamo) {
            dynamo.markStructureDirty();
        }
    }

    /** Clic droit accroupi, main vide : rend le bloc de fer rouge au joueur. */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level,
                                                        @NotNull BlockPos pos, @NotNull Player player,
                                                        @NotNull BlockHitResult hit) {
        if (!player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof RedstoneDynamoBlockEntity dynamo)
                || !dynamo.hasRotor()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) dynamo.dismountRotor(player);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Dynamo cassée : le fer rouge accroché tombe avec elle. */
    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof RedstoneDynamoBlockEntity dynamo
                && dynamo.hasRotor()) {
            Block.popResource(level, pos, dynamo.getRotorStack());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
