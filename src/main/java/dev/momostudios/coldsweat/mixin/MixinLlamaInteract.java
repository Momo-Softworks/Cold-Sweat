package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.capability.ShearableFurManager;
import dev.momostudios.coldsweat.util.registries.ModItems;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
        if (self instanceof LlamaEntity && player.getItemInHand(hand).getItem() instanceof ShearsItem)
        {
            ItemStack stack = player.getItemInHand(hand);
            if (self.isBaby() || stack.getItem() != Items.SHEARS) return;

            ShearableFurManager.getFurCap(self).ifPresent(cap ->
            {
                if (cap.isSheared())
                {   cir.setReturnValue(ActionResultType.sidedSuccess(self.level.isClientSide));
                    return;
                }

                // Use shears
                player.swing(hand, true);

                if (self.level.isClientSide) return;

                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                // Play sound
                self.level.playSound(null, self, SoundEvents.SHEEP_SHEAR, SoundCategory.NEUTRAL, 1.0F, 1.0F);

                // Spawn item
                WorldHelper.entityDropItem(self, new ItemStack(ModItems.FUR));

                // Set sheared
                cap.setSheared(true);
                cap.setLastSheared(self.tickCount);
                ShearableFurManager.syncData(self, null);
                cir.setReturnValue(ActionResultType.FAIL);
                cir.cancel();
            });
        }
    }
}
