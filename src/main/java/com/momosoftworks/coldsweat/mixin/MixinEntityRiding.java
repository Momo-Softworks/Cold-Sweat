package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class MixinEntityRiding
{
    @Redirect(method = "baseTick()V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;stopRiding()V"))
    private void baseTick(LivingEntity entity)
    {
        if (!(entity instanceof ChameleonEntity && entity.getVehicle() instanceof PlayerEntity))
        {   entity.stopRiding();
        }
    }
}
