package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.event.common.ChatComponentClickedEvent;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CreativeWarningMessage
{
    @SubscribeEvent
    public static void onPlayerEnterCreative(PlayerEvent.PlayerChangeGameModeEvent event)
    {
        if (event.getNewGameMode().isCreative() && ClientSettingsConfig.getInstance().isCreativeWarningEnabled())
        {   event.getEntity().displayClientMessage(Component.literal("§d[Cold Sweat]: §c§lWarning! §7Entering the creative inventory will clear insulation from all armor items! ")
                                           .append(Component.literal("(click to disable warning)")
                                                           .withStyle(Style.EMPTY
                                                           .withColor(ChatFormatting.LIGHT_PURPLE)
                                                           .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "cold sweat disable message")))), false);
        }
    }

    @SubscribeEvent
    //called when a player clicks a clickable chat message
    public static void onPlayerClickChatMessage(ChatComponentClickedEvent event)
    {
        if (ClientSettingsConfig.getInstance().isCreativeWarningEnabled()
        && event.getStyle().getClickEvent() != null && event.getStyle().getClickEvent().getValue().equals("cold sweat disable message"))
        {
            ClientSettingsConfig.getInstance().setCreativeWarningEnabled(false);
            event.getPlayer().displayClientMessage(Component.literal("§d[Cold Sweat]: §7Warning message disabled."), false);
        }
    }
}
