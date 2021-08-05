package net.momostudios.coldsweat.events;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.temperature.PlayerTempHandler;
import net.momostudios.coldsweat.temperature.Temperature;
import net.momostudios.coldsweat.temperature.capabilities.TempModifiersCapability;
import net.momostudios.coldsweat.temperature.capabilities.TemperatureCapability;
import net.momostudios.coldsweat.temperature.modifier.BiomeTempModifier;
import net.momostudios.coldsweat.temperature.modifier.DepthTempModifier;
import net.momostudios.coldsweat.temperature.modifier.TempModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ApplyTempEffectsTest
{
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {/*
        PlayerEntity player = event.player;
        PlayerTempHandler pth = new PlayerTempHandler();

        TempModifier[] modArray = {new BiomeTempModifier()};
        List<TempModifier> modifiers = Arrays.asList(modArray);

        player.getCapability(TempModifiersCapability.CAPABILITY_TEMP_MODIFIERS).ifPresent(data ->
        {
            if (!data.hasModifier(new BiomeTempModifier())) data.add(new BiomeTempModifier());
            player.sendStatusMessage(new StringTextComponent(data.getModifiers() + ""), true);
        });

        player.getCapability(TemperatureCapability.CAPABILITY_TEMPERATURE).ifPresent(data ->
        {
            data.setCoreTemperature(new Temperature(data.getCoreTemperature().get() + 0.1));
        });
        System.out.println("hello world");*/
    }
}