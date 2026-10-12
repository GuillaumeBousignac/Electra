package com.electra.mod.block;

import com.electra.mod.setup.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Bloc de fer rouge « monté » sur une Redstone Dynamo.
 *
 * Il remplace le vrai bloc de fer rouge quand la structure se forme : invisible en tant que bloc,
 * il est dessiné en rotation par le rendu de la Dynamo. Il redevient un bloc de fer rouge normal
 * dès que la Dynamo disparaît, et lâche un bloc de fer rouge quand on le casse.
 * Il n'a pas d'objet : on ne peut pas le poser à la main.
 */
public class DynamoRotorBlock extends Block {

    public DynamoRotorBlock(Properties properties) {
        super(properties);
    }

    /** Pas de modèle fixe : le rotor est dessiné, en rotation, par RedstoneDynamoRenderer. */
    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** Plus de Dynamo en dessous : redevient un bloc de fer rouge ordinaire. */
    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction,
                                              @NotNull BlockState neighborState, @NotNull LevelAccessor level,
                                              @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (direction == Direction.DOWN && !(neighborState.getBlock() instanceof RedstoneDynamoBlock)) {
            return ModBlocks.RED_IRON_BLOCK.get().defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    /** Clic molette en créatif : donne un bloc de fer rouge. */
    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state) {
        return new ItemStack(ModBlocks.RED_IRON_BLOCK_ITEM.get());
    }
}
