package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.event.common.ChatComponentClickedEvent;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
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
        {   event.getPlayer().displayClientMessage(new StringTextComponent("[Cold Sweat]: ").withStyle(TextFormatting.LIGHT_PURPLE)
                                           .append(new StringTextComponent("Warning! ").withStyle(TextFormatting.BOLD, TextFormatting.RED))
                                           .append(new StringTextComponent("Entering the creative inventory will clear insulation from all armor items! ").withStyle(TextFormatting.GRAY))
                                           .append(new StringTextComponent("(click to disable warning)")
                                                            .withStyle(Style.EMPTY
                                                            .withColor(TextFormatting.LIGHT_PURPLE)
                                                            .withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "cold sweat disable message")))), false);
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
            event.getPlayer().displayClientMessage(new StringTextComponent("[Cold Sweat]: ").withStyle(TextFormatting.LIGHT_PURPLE)
                                           .append(new StringTextComponent("Warning message disabled.").withStyle(TextFormatting.GRAY)), false);
        }
    }
}
