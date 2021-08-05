package net.momostudios.coldsweat.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.nbt.ListNBTHelper;
import net.momostudios.coldsweat.temperature.PlayerTempHandler;
import net.momostudios.coldsweat.temperature.Temperature;
import net.momostudios.coldsweat.temperature.modifier.BiomeTempModifier;
import net.momostudios.coldsweat.temperature.modifier.DepthTempModifier;
import net.momostudios.coldsweat.temperature.modifier.TempModifier;
import java.util.List;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerTempUpdater
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            PlayerEntity player = event.player;

            // Add TempModifies if not present
            PlayerTempHandler.applyToWorld(player, new BiomeTempModifier());
            PlayerTempHandler.applyToWorld(player, new DepthTempModifier());

            List<TempModifier> modList = ListNBTHelper.asList((ListNBT) player.getPersistentData().get("ambient_temp_modifiers"), TempModifier.class);
            PlayerTempHandler.setPlayerAmbientTemp(player, new Temperature().with(modList, player));

            player.sendStatusMessage(new StringTextComponent
            (
                (int) (PlayerTempHandler.getPlayerAmbientTemp(player).get() * 40 + 40) + " " +
                (int) (PlayerTempHandler.getPlayerBodyTemp(player).get() * 40 + 40)
            ), true);
        }
    }
}