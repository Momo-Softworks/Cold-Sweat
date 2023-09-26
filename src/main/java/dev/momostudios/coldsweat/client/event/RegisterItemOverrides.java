package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.Overlays;
import dev.momostudios.coldsweat.common.capability.EntityTempManager;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.core.init.ItemInit;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
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
            ItemProperties.register(ItemInit.SOULSPRING_LAMP.get(), new ResourceLocation(ColdSweat.MOD_ID, "soulspring_state"), (stack, level, entity, id) ->
            {
                if (stack.getOrCreateTag().getBoolean("isOn"))
                {
                    return stack.getOrCreateTag().getInt("fuel") > 43 ? 3 :
                           stack.getOrCreateTag().getInt("fuel") > 22 ? 2 : 1;
                }
                return 0;
            });

            ItemProperties.register(ItemInit.THERMOMETER.get(), new ResourceLocation(ColdSweat.MOD_ID, "temperature"), (stack, level, livingEntity, id) ->
            {
                Entity entity = (livingEntity != null ? livingEntity : stack.getEntityRepresentation());
                if (entity != null)
                {
                    double minTemp = ConfigSettings.MIN_TEMP.get();
                    double maxTemp = ConfigSettings.MAX_TEMP.get();

                    double worldTemp;
                    if (!entity.getPersistentData().contains("WorldTempTimestamp")
                    || (entity.tickCount % 20 == 0 || (entity instanceof Player && entity.tickCount % 2 == 0)) && entity.getPersistentData().getInt("WorldTempTimestamp") != entity.tickCount)
                    {
                        worldTemp = entity instanceof LivingEntity living
                                ? EntityTempManager.getTemperatureCap(living).map(cap -> cap.getTemp(Temperature.Type.WORLD)).orElse(0.0)
                                : Temperature.getTemperatureAt(entity.blockPosition(), entity.level);

                        entity.getPersistentData().putDouble("WorldTemp", worldTemp);
                        entity.getPersistentData().putInt("WorldTempTimestamp", entity.tickCount);
                    }
                    else worldTemp = entity.getPersistentData().getDouble("WorldTemp");

                    if (entity instanceof ItemFrame frame)
                    {
                        if (Minecraft.getInstance().getEntityRenderDispatcher().crosshairPickEntity == frame)
                        {
                            boolean celsius = ClientSettingsConfig.getInstance().isCelsius();
                            String tempColor = switch (Overlays.getWorldSeverity(worldTemp, minTemp, maxTemp, 0, 0))
                            {
                                case 0 -> "§f";
                                case 2,3 -> "§6";
                                case 4 -> "§c";
                                case -2,-3 -> "§b";
                                case -4 -> "§9";
                                default -> "§r";
                            };
                            int convertedTemp = (int) Temperature.convertUnits(worldTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true) + ClientSettingsConfig.getInstance().getTempOffset();
                            frame.getItem().setHoverName(Component.literal(tempColor + convertedTemp + " °" + (celsius ? "C" : "F")));
                        }
                    }

                    double worldTempAdjusted = CSMath.blend(-1.01d, 1d, worldTemp, minTemp, maxTemp);
                    return (float) worldTempAdjusted;
                }
                return 0;
            });
        });
    }
}
