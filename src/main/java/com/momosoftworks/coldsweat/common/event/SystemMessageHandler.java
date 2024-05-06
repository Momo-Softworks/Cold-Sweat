package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.event.common.ChatComponentClickedEvent;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SystemMessageHandler
{
    private static PlayerController GAME_MODE = null;

    @SubscribeEvent
    public static void onPlayerEnterCreative(TickEvent.PlayerTickEvent event)
    {
        if (event.player instanceof ClientPlayerEntity && Minecraft.getInstance().gameMode != GAME_MODE && Minecraft.getInstance().gameMode.getPlayerMode().isCreative()
        && ConfigSettings.SHOW_CREATIVE_WARNING.get() && !Minecraft.getInstance().isLocalServer())
        {
            event.player.displayClientMessage(getSystemPrefix()
                                .append(new TranslationTextComponent("message.cold_sweat.warning").append(" ").withStyle(TextFormatting.BOLD, TextFormatting.RED))
                                .append(new TranslationTextComponent("message.cold_sweat.creative_warning_message").append(" ").withStyle(TextFormatting.GRAY))
                                .append(new TranslationTextComponent("message.cold_sweat.disable")
                                                 .withStyle(Style.EMPTY
                                                 .withColor(TextFormatting.LIGHT_PURPLE)
                                                 .withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "cold sweat disable message")))), false);
            GAME_MODE = Minecraft.getInstance().gameMode;
        }
    }

    @SubscribeEvent
    //called when a player clicks a clickable chat message
    public static void onPlayerClickChatMessage(ChatComponentClickedEvent event)
    {
        if (ConfigSettings.SHOW_CREATIVE_WARNING.get()
        && event.getStyle().getClickEvent() != null && event.getStyle().getClickEvent().getValue().equals("cold sweat disable message"))
        {
            ConfigSettings.SHOW_CREATIVE_WARNING.set(false);
            event.getPlayer().displayClientMessage(getSystemPrefix()
                                           .append(new TranslationTextComponent("message.cold_sweat.disable_feedback").withStyle(TextFormatting.GRAY)), false);
        }
    }

    public static IFormattableTextComponent getSystemPrefix()
    {   return new StringTextComponent("[").append(new TranslationTextComponent("message.cold_sweat.mod_name")).append("]: ").withStyle(TextFormatting.LIGHT_PURPLE);
    }
}
