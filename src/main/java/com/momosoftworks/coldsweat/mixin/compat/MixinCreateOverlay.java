package com.momosoftworks.coldsweat.mixin.compat;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//TODO: Reimplement when Create gets updated
//@Mixin(RemainingAirOverlay.class)
public class MixinCreateOverlay
{
    //@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInLava()Z"))
    //private boolean isInLava(LocalPlayer instance)
    //{
    //    return true;
    //}
}
