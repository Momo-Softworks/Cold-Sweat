package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.common.ItemSwappedInInventoryEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public class ItemSwapMixin
{
    Container self = (Container)(Object)this;
    @Inject(method = "doClick(IILnet/minecraft/inventory/container/ClickType;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;",
            slice = @Slice(from = @At(target = "Lnet/minecraft/inventory/container/Container;consideredTheSameItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", value = "INVOKE", ordinal = 0),
                           to   = @At(target = "Lnet/minecraft/inventory/container/Container;consideredTheSameItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", value = "INVOKE", ordinal = 1)),
            at = @At(target = "Lnet/minecraft/inventory/container/Slot;set(Lnet/minecraft/item/ItemStack;)V", value = "INVOKE", ordinal = 0),
            cancellable = true, remap = ColdSweat.REMAP_MIXINS)
    private void onItemSwap(int slotId, int dragType, ClickType clickType, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir)
    {
        if (MinecraftForge.EVENT_BUS.post(new ItemSwappedInInventoryEvent(self.getSlot(slotId).getItem(), player.inventory.getCarried(), (Container)(Object)this, player)))
        {   cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
