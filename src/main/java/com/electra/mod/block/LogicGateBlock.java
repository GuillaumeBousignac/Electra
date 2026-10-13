package com.electra.mod.block;

import com.electra.mod.blockentity.LogicGateBlockEntity;
import com.electra.mod.setup.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Porte logique ou interrupteur au FE : un boîtier un peu plus large qu'un câble, posé en ligne
 * avec les câbles. La sortie pointe dans la direction où regarde le joueur au moment de la pose.
 * Répartition des faces : voir GateType.
 *
 * Les voyants (INPUT_A, INPUT_B, OUTPUT) ne servent qu'à l'affichage ; la logique est dans l'entité.
 */
public class LogicGateBlock extends BaseEntityBlock {

    public static final MapCodec<LogicGateBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    GateType.CODEC.fieldOf("gate").forGetter(LogicGateBlock::getType),
                    propertiesCodec()
            ).apply(instance, LogicGateBlock::new));

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** Entrée A (gauche de la sortie) alimentée. */
    public static final BooleanProperty INPUT_A = BooleanProperty.create("input_a");
    /** Entrée B (droite de la sortie) alimentée. */
    public static final BooleanProperty INPUT_B = BooleanProperty.create("input_b");
    /** Sortie active. */
    public static final BooleanProperty OUTPUT = BooleanProperty.create("output");
    /** Interrupteur enclenché (inutilisé par les portes). */
    public static final BooleanProperty ON = BooleanProperty.create("on");
    /** Porte OU en mode OU exclusif (XOR). Inutilisé par les autres types. */
    public static final BooleanProperty EXCLUSIVE = BooleanProperty.create("exclusive");

    private final GateType type;
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    public LogicGateBlock(GateType type, Properties properties) {
        super(properties);
        this.type = type;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(INPUT_A, false).setValue(INPUT_B, false)
                .setValue(OUTPUT, false).setValue(ON, false).setValue(EXCLUSIVE, false));
        for (Direction facing : Direction.Plane.HORIZONTAL) shapes.put(facing, buildShape(facing));
    }

    public GateType getType() { return type; }

    // ── Faces ──────────────────────────────────────────────────────────────

    public static Direction inputA(Direction facing) { return facing.getCounterClockWise(); }
    public static Direction inputB(Direction facing) { return facing.getClockWise(); }
    public static Direction powerSide(Direction facing) { return facing.getOpposite(); }

    /** Faces raccordables (câbles et capacité FE) pour une orientation donnée. */
    public boolean isPort(Direction facing, Direction side) {
        if (side == facing) return true;
        if (type.hasInputs() && (side == inputA(facing) || side == inputB(facing))) return true;
        return type.hasPowerSide() && side == powerSide(facing);
    }

    // ── Forme : boîtier 10×8×10 + embouts de raccordement ─────────────────

    private VoxelShape buildShape(Direction facing) {
        VoxelShape shape = Block.box(3, 4, 3, 13, 12, 13);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (!isPort(facing, side)) continue;
            shape = Shapes.or(shape, switch (side) {
                case NORTH -> Block.box(5, 5, 0, 11, 11, 3);
                case SOUTH -> Block.box(5, 5, 13, 11, 11, 16);
                case WEST -> Block.box(0, 5, 5, 3, 11, 11);
                default -> Block.box(13, 5, 5, 16, 11, 11);
            });
        }
        return shape;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                           @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    // ── États ──────────────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, INPUT_A, INPUT_B, OUTPUT, ON, EXCLUSIVE);
    }

    /** La sortie pointe vers l'avant du joueur, comme un répéteur posé « dans le sens du courant ». */
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // ── Interaction : l'interrupteur se manœuvre au clic droit ;
    //    la porte OU passe en XOR (et inversement) accroupi + clic droit, main vide ─────

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level,
                                                        @NotNull BlockPos pos, @NotNull Player player,
                                                        @NotNull BlockHitResult hit) {
        if (type == GateType.OR && player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                boolean exclusive = !state.getValue(EXCLUSIVE);
                level.setBlock(pos, state.setValue(EXCLUSIVE, exclusive), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.4f, exclusive ? 0.6f : 0.5f);
                player.displayClientMessage(Component.translatable(
                        exclusive ? "message.electra.gate_mode_xor" : "message.electra.gate_mode_or"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (type != GateType.SWITCH) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            boolean on = !state.getValue(ON);
            level.setBlock(pos, state.setValue(ON, on), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3f, on ? 0.6f : 0.5f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.electra." + type.id()).withStyle(ChatFormatting.GRAY));
    }

    // ── Entité de bloc ─────────────────────────────────────────────────────

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
        return new LogicGateBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.LOGIC_GATE_BE.get(), (lvl, pos, st, be) -> be.tick());
    }
}
