package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.client.gui.Overlays;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.core.init.ItemInit;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegisterItemOverrides
{
    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            ItemProperties.register(ItemInit.HELLSPRING_LAMP.get(), new ResourceLocation(ColdSweat.MOD_ID, "hellspring_state"), (stack, level, entity, id) ->
            {
                if (stack.getOrCreateTag().getBoolean("isOn"))
                {
                    return stack.getOrCreateTag().getInt("fuel") > 43 ? 3 :
                            stack.getOrCreateTag().getInt("fuel") > 22 ? 2 : 1;
                }
                return 0;
            });

            ItemProperties.register(ItemInit.THERMOMETER.get(), new ResourceLocation(ColdSweat.MOD_ID, "temperature"), (stack, level, entity, id) ->
            {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null)
                {
                    ConfigCache config = ConfigCache.getInstance();
                    double minTemp = config.minTemp;
                    double maxTemp = config.maxTemp;

                    double worldTemp = (float) CSMath.convertUnits(Overlays.WORLD_TEMP, ClientSettingsConfig.getInstance().celsius() ? Temperature.Units.C : Temperature.Units.F, Temperature.Units.MC, true);

                    double worldTempAdjusted = CSMath.blend(-1.01d, 1d, worldTemp, minTemp, maxTemp);
                    return (float) worldTempAdjusted;
                }
                return 1;
            });
        });
    }
}
