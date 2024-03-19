package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.util.math.CSMath;
import com.simibubi.create.content.equipment.armor.RemainingAirOverlay;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinCreateOverlay
{
    @Inject(method = "isInLava", at = @At("HEAD"), cancellable = true)
    private void isInLava(CallbackInfoReturnable<Boolean> cir)
    {
        if (CSMath.getCallerClass(1) == RemainingAirOverlay.class)
        {   cir.setReturnValue(true);
        }
    }
}
