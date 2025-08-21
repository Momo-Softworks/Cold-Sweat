package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.ItemBreakEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class MixinArmorBreak
{
    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void onHurtAndBreak(int damage, ServerLevel level, LivingEntity entity, Consumer<Item> onBroken, CallbackInfo ci)
    {   NeoForge.EVENT_BUS.post(new ItemBreakEvent((ItemStack) (Object) this, entity, onBroken));
    }
}
