package com.momosoftworks.coldsweat.mixin;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.List;
import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class MixinPreventFreezing
{
    LivingEntity self = (LivingEntity) (Object) this;

    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    public void preventFreezing(CallbackInfoReturnable<Boolean> cir)
    {
        int[] freezeImmuneItems = new int[] {0};

        for (ItemStack armorItem : self.getArmorSlots())
        {
            Optional<ItemInsulationCap> cap = ItemInsulationManager.getInsulationCap(armorItem);
            if (cap.isPresent())
            {
                for (Pair<ItemStack, List<InsulatorData>> pair : cap.get().getInsulation())
                {
                    if (pair.getFirst().is(ItemTags.FREEZE_IMMUNE_WEARABLES))
                    {
                        if (++freezeImmuneItems[0] >= 4)
                        {   cir.setReturnValue(false);
                            return;
                        }
                    }
                }
            }
        }
    }
}
