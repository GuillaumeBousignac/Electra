package com.electra.mod.item;

import com.electra.mod.block.LampSymbol;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Autocollant pour lampe électrique : clic droit sur une lampe sans motif pour y coller le premier
 * motif de sa famille (A pour l'autocollant alphabétique). L'autocollant est consommé.
 * La pose est gérée par la lampe (ElectricLampBlock#useItemOn).
 */
public class LampStickerItem extends Item {

    private final LampSymbol.Family family;

    public LampStickerItem(LampSymbol.Family family, Properties properties) {
        super(properties);
        this.family = family;
    }

    public LampSymbol.Family getFamily() { return family; }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.electra.lamp_sticker").withStyle(ChatFormatting.GRAY));
    }
}
