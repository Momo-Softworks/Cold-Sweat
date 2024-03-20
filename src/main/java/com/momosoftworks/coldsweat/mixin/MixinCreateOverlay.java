package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
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
        if (!CompatManager.isCreateLoaded()) return;
        try
        {
            Class<?> createClass = Class.forName("com.simibubi.create.content.equipment.armor.RemainingAirOverlay");
            if (CSMath.getCallerClass(1) == createClass && CompatManager.USING_BACKTANK)
            {   cir.setReturnValue(true);
            }
        }
        catch (ClassNotFoundException ignored) {}
    }
}
