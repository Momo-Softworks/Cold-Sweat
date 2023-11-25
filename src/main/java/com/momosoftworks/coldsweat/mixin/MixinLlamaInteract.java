package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.capability.ShearableFurManager;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractChestedHorseEntity.class)
public class MixinLlamaInteract
{
    AbstractChestedHorseEntity self = (AbstractChestedHorseEntity) (Object) this;

    @Inject(method = "mobInteract(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResultType;", at = @At("HEAD"), cancellable = true)
    private void onMobInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResultType> cir)
    {
        if (self instanceof LlamaEntity && player.getItemInHand(hand).getItem() instanceof ShearsItem && !player.level.isClientSide)
        {
            ItemStack stack = player.getItemInHand(hand);
            if (self.isBaby() || stack.getItem() != Items.SHEARS) return;

            ShearableFurManager.getFurCap(self).ifPresent(cap ->
            {
                if (cap.isSheared())
                {   cir.setReturnValue(ActionResultType.sidedSuccess(self.level.isClientSide));
                    return;
                }

                ShearableFurManager.shearEntity(self, player, hand);
                cir.setReturnValue(ActionResultType.FAIL);
                cir.cancel();
            });
        }
    }
}
