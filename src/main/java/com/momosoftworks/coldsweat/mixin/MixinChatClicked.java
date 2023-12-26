package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.common.ChatComponentClickedEvent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.Style;
import net.minecraftforge.common.MinecraftForge;
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
    {   MinecraftForge.EVENT_BUS.post(new ChatComponentClickedEvent(style, self.getMinecraft().player, self));
    }
}
