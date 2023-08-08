package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.common.ItemSwappedInInventoryEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Container.class)
public class ItemSwapMixin
{
    @Inject(method = "doClick(IILnet/minecraft/inventory/container/ClickType;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;",
            at = @At(target = "Lnet/minecraft/inventory/container/Slot;getMaxStackSize(Lnet/minecraft/item/ItemStack;)I", value = "INVOKE", ordinal = 7),
            cancellable = true, remap = ColdSweat.REMAP_MIXINS, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onItemSwap(int slotId, int dragType, ClickType clickType, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir)
    {
        if (MinecraftForge.EVENT_BUS.post(new ItemSwappedInInventoryEvent(player.inventory.getCarried(), player.inventory.getItem(slotId), (Container)(Object)this, player)))
        {   cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
