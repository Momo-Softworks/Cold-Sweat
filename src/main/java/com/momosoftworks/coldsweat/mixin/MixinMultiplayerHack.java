package com.momosoftworks.coldsweat.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Screw you Microsoft & Mojang for disabling multiplayer
 */
@Mixin(Minecraft.class)
public class MixinMultiplayerHack
{
    @Inject(method = "allowsChat()Z", at = @At("HEAD"), cancellable = true)
    private void allowsChat(CallbackInfoReturnable<Boolean> cir)
    {   cir.setReturnValue(true);
    }

    @Inject(method = "allowsMultiplayer()Z", at = @At("HEAD"), cancellable = true)
    private void allowsMultiplayer(CallbackInfoReturnable<Boolean> cir)
    {   cir.setReturnValue(true);
    }
}
