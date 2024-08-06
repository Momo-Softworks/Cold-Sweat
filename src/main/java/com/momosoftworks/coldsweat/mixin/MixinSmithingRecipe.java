package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.capability.ModCapabilities;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UpgradeRecipe.class)
public class MixinSmithingRecipe
{
    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true)
    public void copyCapabilities(Container container, CallbackInfoReturnable<ItemStack> cir)
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
