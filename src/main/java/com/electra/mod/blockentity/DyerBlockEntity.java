package com.electra.mod.blockentity;

import com.electra.mod.block.DyerBlock;
import com.electra.mod.config.ElectraConfig;
import com.electra.mod.energy.ModEnergyStorage;
import com.electra.mod.fluid.FluidPort;
import com.electra.mod.fluid.FluidPorts;
import com.electra.mod.fluid.ModFluidTank;
import com.electra.mod.menu.DyerMenu;
import com.electra.mod.recipe.DyeingRecipe;
import com.electra.mod.recipe.DyeingRecipeInput;
import com.electra.mod.setup.ModBlockEntities;
import com.electra.mod.setup.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Teinturière électrique : une cuve d'eau (tuyaux ou seau), une case d'objet, trois cases de colorant, une case de sortie.
 * Teint tout ce qui se teint, un objet à la fois, depuis n'importe quelle couleur ; plusieurs colorants se mélangent
 * pour les armures en cuir.
 */
public class DyerBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_DYE_1 = 1;
    public static final int SLOT_DYE_2 = 2;
    public static final int SLOT_DYE_3 = 3;
    public static final int SLOT_OUTPUT = 4;

    /** Eau et énergie des teintures « par composant » (armures en cuir) ; les recettes fixent les leurs. */
    private static final int COMPONENT_DYE_WATER = 100;
    private static final int COMPONENT_DYE_ENERGY = 40;

    private static final int[] DYE_SLOTS = {SLOT_DYE_1, SLOT_DYE_2, SLOT_DYE_3};
    private static final int[] SLOTS_INPUT  = {SLOT_INPUT, SLOT_DYE_1, SLOT_DYE_2, SLOT_DYE_3};
    private static final int[] SLOTS_OUTPUT = {SLOT_OUTPUT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);

    private final ModEnergyStorage energyStorage = new ModEnergyStorage(
            ElectraConfig.DYER_CAPACITY.get(), ElectraConfig.DYER_MAX_RECEIVE.get(), 0, this::onChanged);

    /** Eau seulement ; les tuyaux et les seaux peuvent la remplir, rien ne peut la vider de l'extérieur. */
    private final ModFluidTank tank = new ModFluidTank(ElectraConfig.DYER_TANK.get(),
            stack -> stack.getFluid().isSame(Fluids.WATER), this::onChanged);
    private final FluidPort port = FluidPorts.sink(tank);

    private int progress = 0;
    private boolean needsSync = false;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            int energy = energyStorage.getEnergyStored();
            return switch (index) {
                case 0 -> progress;
                case 1 -> ElectraConfig.DYER_TIME.get();
                case 2 -> energy & 0xFFFF;
                case 3 -> (energy >>> 16) & 0xFFFF;
                case 4 -> tank.getFluidAmount();
                default -> 0;
            };
        }

        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 5; }
    };

    public DyerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DYER_BE.get(), pos, state);
    }

    private void onChanged() {
        setChanged();
        needsSync = true;
    }

    // ── Logique ────────────────────────────────────────────────────────────

    public void tick() {
        if (level == null || level.isClientSide()) return;

        DyeJob job = findJob();
        boolean canRun = job != null
                && energyStorage.getEnergyStored() >= job.energy()
                && canInsertOutput(items.get(SLOT_OUTPUT), job.result());

        if (!canRun) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            DyerBlock.setLit(level, worldPosition, getBlockState(), false);
        } else {
            DyerBlock.setLit(level, worldPosition, getBlockState(), true);
            if (++progress >= ElectraConfig.DYER_TIME.get()) {
                progress = 0;
                craft(job);
            }
        }

        if (needsSync && level.getGameTime() % 10 == 0) {
            needsSync = false;
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    /** Une opération prête : résultat, eau et énergie consommées. */
    private record DyeJob(ItemStack result, int fluid, int energy) {}

    private void craft(DyeJob job) {
        energyStorage.extractInternal(job.energy());
        tank.drain(job.fluid(), IFluidHandler.FluidAction.EXECUTE);
        items.get(SLOT_INPUT).shrink(1);
        for (int slot : DYE_SLOTS) {
            if (!items.get(slot).isEmpty()) items.get(slot).shrink(1);
        }

        ItemStack result = job.result().copy();
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) items.set(SLOT_OUTPUT, result);
        else output.grow(result.getCount());

        setChanged();
        needsSync = true;
    }

    /**
     * Armures en cuir et autres objets de la balise « dyeable » : les colorants (1 à 3) se mélangent comme à l'établi.
     * Sinon, une seule couleur à la fois, d'après les recettes (laine, béton, bannières, boîtes de shulker...).
     */
    @Nullable
    private DyeJob findJob() {
        if (level == null || tank.isEmpty()) return null;
        ItemStack input = items.get(SLOT_INPUT);
        if (input.isEmpty()) return null;

        List<ItemStack> dyes = new ArrayList<>();
        for (int slot : DYE_SLOTS) {
            if (!items.get(slot).isEmpty()) dyes.add(items.get(slot));
        }
        if (dyes.isEmpty()) return null;

        if (input.is(ItemTags.DYEABLE)) {
            if (tank.getFluidAmount() < COMPONENT_DYE_WATER) return null;
            List<DyeItem> dyeItems = new ArrayList<>();
            for (ItemStack dye : dyes) {
                if (!(dye.getItem() instanceof DyeItem dyeItem)) return null;
                dyeItems.add(dyeItem);
            }
            return new DyeJob(DyedItemColor.applyDyes(input, dyeItems), COMPONENT_DYE_WATER, COMPONENT_DYE_ENERGY);
        }

        if (dyes.size() != 1) return null;
        DyeingRecipeInput recipeInput = new DyeingRecipeInput(input, dyes.get(0), tank.getFluidAmount());
        for (RecipeHolder<DyeingRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.DYEING_TYPE.get())) {
            DyeingRecipe recipe = holder.value();
            if (recipe.matches(recipeInput, level)) {
                return new DyeJob(recipe.assemble(recipeInput, level.registryAccess()), recipe.getFluidAmount(), recipe.getEnergyCost());
            }
        }
        return null;
    }

    private boolean isDyeable(ItemStack stack) {
        if (level == null) return false;
        if (stack.is(ItemTags.DYEABLE)) return true;
        for (RecipeHolder<DyeingRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.DYEING_TYPE.get())) {
            if (holder.value().getInput().test(stack)) return true;
        }
        return false;
    }

    private boolean isDye(ItemStack stack) {
        if (level == null) return false;
        if (stack.getItem() instanceof DyeItem) return true;
        for (RecipeHolder<DyeingRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.DYEING_TYPE.get())) {
            if (holder.value().getDye().test(stack)) return true;
        }
        return false;
    }

    private static boolean canInsertOutput(ItemStack existing, ItemStack result) {
        if (result.isEmpty()) return false;
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, result)) return false;
        return existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    // ── Accesseurs ─────────────────────────────────────────────────────────

    public ModEnergyStorage getEnergyStorage() { return energyStorage; }
    public FluidPort getFluidPort()            { return port; }
    public ModFluidTank getTank()              { return tank; }
    public ContainerData getContainerData()    { return data; }

    // ── MenuProvider ───────────────────────────────────────────────────────

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.electra.dyer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new DyerMenu(windowId, playerInventory, this, data);
    }

    // ── Inventaire (entonnoirs et tuyaux) ──────────────────────────────────

    @Override
    public int[] getSlotsForFace(@NotNull Direction side) {
        return side == Direction.DOWN ? SLOTS_OUTPUT : SLOTS_INPUT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction direction) {
        return slot == SLOT_OUTPUT;
    }

    /** Seuls les objets qu'une recette de teinture transforme entrent dans la case d'entrée. */
    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return (slot == SLOT_INPUT && isDyeable(stack)) || ((slot == SLOT_DYE_1 || slot == SLOT_DYE_2 || slot == SLOT_DYE_3) && isDye(stack));
    }

    @Override public int getContainerSize()                              { return items.size(); }
    @Override public boolean isEmpty()                                   { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public @NotNull ItemStack getItem(int slot)                { return items.get(slot); }
    @Override public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }
    @Override public @NotNull ItemStack removeItemNoUpdate(int slot)     { return ContainerHelper.takeItem(items, slot); }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // ── Sauvegarde et synchronisation ──────────────────────────────────────

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("progress", progress);
        tank.save(tag, registries);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        energyStorage.setEnergy(tag.getInt("energy"));
        progress = tag.getInt("progress");
        tank.load(tag, registries);
    }
}
