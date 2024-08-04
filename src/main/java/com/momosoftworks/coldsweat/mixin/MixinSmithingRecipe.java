package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.capability.ModCapabilities;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.SmithingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingRecipe.class)
public class MixinSmithingRecipe
{
    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true)
    public void copyCapabilities(IInventory container, CallbackInfoReturnable<ItemStack> cir)
    {
        ItemStack result = cir.getReturnValue();
        ItemStack base = container.getItem(1);
        result.getCapability(ModCapabilities.ITEM_INSULATION).ifPresent(resultCap ->
        {
            base.getCapability(ModCapabilities.ITEM_INSULATION).ifPresent(baseCap ->
            {
                resultCap.copy(baseCap);
            });
        });
        cir.setReturnValue(result);
    }
}
