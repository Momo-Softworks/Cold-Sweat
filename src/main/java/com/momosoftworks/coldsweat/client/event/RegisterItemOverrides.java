package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.entity.data.Preference;
import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.lang.reflect.Field;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegisterItemOverrides
{
    public static final IItemPropertyGetter SOULSPRING_LAMP_PROPERTIES = (stack, level, entity) ->
    {
        if (SoulspringLampItem.isLit(stack))
        {
            double fuel = SoulspringLampItem.getFuel(stack);
            return fuel > 43 ? 3 :
                   fuel > 22 ? 2 : 1;
        }
        return 0;
    };

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            if (!ConfigSettings.ANIMATED_SOULSPRING_LAMP_MODEL.get())
            ItemModelsProperties.register(ItemInit.SOULSPRING_LAMP.get(), new ResourceLocation(ColdSweat.MOD_ID, "soulspring_state"), SOULSPRING_LAMP_PROPERTIES);

            ItemModelsProperties.register(ItemInit.FILLED_WATERSKIN.get(), new ResourceLocation(ColdSweat.MOD_ID, "water_temperature"), (stack, level, entity) ->
            {
                return stack.getOrCreateTag().getFloat(FilledWaterskinItem.NBT_TEMPERATURE);
            });

            ItemModelsProperties.register(ItemInit.THERMOMETER.get(), new ResourceLocation(ColdSweat.MOD_ID, "temperature"), (stack, level, livingEntity) ->
            {
                if (livingEntity instanceof PlayerEntity)
                {
                    PlayerEntity player = (PlayerEntity) livingEntity;
                    double minTemp = Temperature.get(player, Temperature.Trait.FREEZING_POINT);
                    double maxTemp = Temperature.get(player, Temperature.Trait.BURNING_POINT);

                    double worldTemp;
                    if (!player.getPersistentData().contains("WorldTempTimestamp")
                    || (player.tickCount % 2 == 0 && player.getPersistentData().getInt("WorldTempTimestamp") != player.tickCount))
                    {
                        worldTemp = Temperature.convert(Overlays.WORLD_TEMP, Preference.getOrDefault(player, Preference.UNITS, Temperature.Units.F), Temperature.Units.MC, true);

                        player.getPersistentData().putDouble("WorldTemp", worldTemp);
                        player.getPersistentData().putInt("WorldTempTimestamp", player.tickCount);
                    }
                    else worldTemp = player.getPersistentData().getDouble("WorldTemp");

                    double worldTempAdjusted = Overlays.getWorldSeverity(worldTemp, minTemp, maxTemp) * 1.01;
                    return (float) worldTempAdjusted;
                }
                return 0;
            });
        });
    }

    private static final Field ITEM_PROPERTIES = ObfuscationReflectionHelper.findField(ItemModelsProperties.class, "field_239415_f_");
    static { ITEM_PROPERTIES.setAccessible(true); }
    public static void unregister(Item item)
    {
        try
        {
            Map<Item, Map<ResourceLocation, IItemPropertyGetter>> properties = (Map<Item, Map<ResourceLocation, IItemPropertyGetter >>) ITEM_PROPERTIES.get(null);
            properties.remove(item);
        }
        catch (IllegalAccessException e)
        {   throw new RuntimeException(e);
        }
    }
}
