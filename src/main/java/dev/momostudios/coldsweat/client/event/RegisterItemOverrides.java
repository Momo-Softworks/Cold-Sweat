package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.Overlays;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.core.init.ItemInit;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
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
            ItemModelsProperties.register(ItemInit.SOULSPRING_LAMP.get(), new ResourceLocation(ColdSweat.MOD_ID, "soulspring_state"), (stack, level, entity) ->
            {
                if (stack.getOrCreateTag().getBoolean("isOn"))
                {
                    return stack.getOrCreateTag().getInt("fuel") > 43 ? 3 :
                           stack.getOrCreateTag().getInt("fuel") > 22 ? 2 : 1;
                }
                return 0;
            });

            ItemModelsProperties.register(ItemInit.THERMOMETER.get(), new ResourceLocation(ColdSweat.MOD_ID, "temperature"), (stack, level, livingEntity) ->
            {
                Entity entity = (livingEntity != null ? livingEntity : stack.getEntityRepresentation());
                if (entity != null)
                {
                    double minTemp = ConfigSettings.MIN_TEMP.get();
                    double maxTemp = ConfigSettings.MAX_TEMP.get();

                    double worldTemp;
                    if (!entity.getPersistentData().contains("WorldTempTimestamp")
                    || (entity.tickCount % 20 == 0 || (entity instanceof PlayerEntity && entity.tickCount % 2 == 0)) && entity.getPersistentData().getInt("WorldTempTimestamp") != entity.tickCount)
                    {
                        if (entity instanceof LivingEntity)
                        {   worldTemp = Temperature.getTemperatureCap(((LivingEntity) entity)).map(cap -> cap.getTemp(Temperature.Type.WORLD)).orElse(0.0);
                        }
                        else worldTemp = Temperature.getTemperatureAt(entity.blockPosition(), entity.level);

                        entity.getPersistentData().putDouble("WorldTemp", worldTemp);
                        entity.getPersistentData().putInt("WorldTempTimestamp", entity.tickCount);
                    }
                    else worldTemp = entity.getPersistentData().getDouble("WorldTemp");

                    if (entity instanceof ItemFrameEntity)
                    {
                        ItemFrameEntity frame = (ItemFrameEntity) entity;
                        if (Minecraft.getInstance().getEntityRenderDispatcher().crosshairPickEntity == frame)
                        {
                            boolean celsius = ClientSettingsConfig.getInstance().isCelsius();
                            String tempColor;
                            switch (Overlays.getWorldSeverity(worldTemp, minTemp, maxTemp, 0, 0))
                            {
                                case 0 : tempColor = "§f";
                                case 2 : case 3 : tempColor = "§6";
                                case 4 : tempColor = "§c";
                                case -2 : case -3 : tempColor = "§b";
                                case -4 : tempColor = "§9";
                                default : tempColor = "§r";
                            };
                            int convertedTemp = (int) CSMath.convertTemp(worldTemp, Temperature.Units.MC, celsius ? Temperature.Units.C : Temperature.Units.F, true) + ClientSettingsConfig.getInstance().getTempOffset();
                            frame.getItem().setHoverName(new StringTextComponent(tempColor + convertedTemp + " °" + (celsius ? "C" : "F")));
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
