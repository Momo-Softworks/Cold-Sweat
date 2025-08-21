package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.ItemBreakEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class MixinArmorBreak
{
    @Inject(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private <T extends LivingEntity> void onHurtAndBreak(int damage, T entity, Consumer<T> onBroken, CallbackInfo ci)
    {   MinecraftForge.EVENT_BUS.post(new ItemBreakEvent((ItemStack) (Object) this, entity, (Consumer<LivingEntity>) onBroken));
    }
}
