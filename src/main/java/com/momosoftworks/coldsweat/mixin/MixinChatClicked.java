package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.vanilla.ChatComponentClickedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinChatClicked
{
    @Inject(method = "defaultHandleGameClickEvent", at = @At("HEAD"), cancellable = true)
    private static void onChatComponentClicked(ClickEvent event, Minecraft minecraft, @Nullable Screen activeScreen, CallbackInfo ci)
    {
        NeoForge.EVENT_BUS.post(new ChatComponentClickedEvent(Style.EMPTY.withClickEvent(event), minecraft.player));
        // Cold Sweat's custom click actions are handled client-side; don't forward them to the server
        if (event instanceof ClickEvent.Custom custom && custom.id().getNamespace().equals(ColdSweat.MOD_ID))
        {   ci.cancel();
        }
    }
}
