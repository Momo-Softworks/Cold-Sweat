package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMultiplayerHack
{
    @Inject(method = "allowsChat()Z", at = @At("HEAD"), cancellable = true)
    private void allowsChat(CallbackInfoReturnable<Boolean> cir)
    {   cir.setReturnValue(cir.getReturnValueZ() || !ColdSweat.REMAP_MIXINS);
    }

    @Inject(method = "allowsMultiplayer()Z", at = @At("HEAD"), cancellable = true)
    private void allowsMultiplayer(CallbackInfoReturnable<Boolean> cir)
    {   cir.setReturnValue(cir.getReturnValueZ() || !ColdSweat.REMAP_MIXINS);
    }
}
