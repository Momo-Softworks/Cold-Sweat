package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.entity.Chameleon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class MixinEntityRiding
{
    @Redirect(method = "baseTick()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;stopRiding()V"))
    private void baseTick(LivingEntity entity)
    {
        if (!(entity instanceof Chameleon && entity.getVehicle() instanceof Player))
        {   entity.stopRiding();
        }
    }
}
