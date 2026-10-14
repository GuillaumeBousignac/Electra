package com.electra.mod.event;

import com.electra.mod.Electra;
import com.electra.mod.block.ElectricLampBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.item.DyeItem;

/**
 * Accroupi avec un objet en main, Minecraft n'utilise pas le bloc visé (il passe directement à l'objet).
 * Pour qu'accroupi + clic droit avec une teinture colore le motif d'une lampe, on force ici l'usage
 * du bloc. L'événement a lieu côté client et côté serveur.
 */
@EventBusSubscriber(modid = Electra.MODID)
public final class LampInteractionEvents {

    private LampInteractionEvents() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown()) return;
        if (!(event.getItemStack().getItem() instanceof DyeItem)) return;
        if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof ElectricLampBlock) {
            event.setUseBlock(TriState.TRUE);
        }
    }
}
