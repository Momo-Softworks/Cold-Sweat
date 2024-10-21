package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.ChatComponentClickedEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class MixinChatClicked
{
    Screen self = (Screen) (Object) this;

    @Inject(method = "handleComponentClicked", at = @At("HEAD"))
    private void onChatComponentClicked(Style style, CallbackInfoReturnable<Boolean> ci)
    {   NeoForge.EVENT_BUS.post(new ChatComponentClickedEvent(style, self.getMinecraft().player));
    }
}
