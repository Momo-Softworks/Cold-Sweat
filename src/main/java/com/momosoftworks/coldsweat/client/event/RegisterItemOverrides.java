package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.entity.data.Preference;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.core.init.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT)
public class RegisterItemOverrides
{
    public static final ItemPropertyFunction SOULSPRING_LAMP_PROPERTIES = (stack, level, entity, id) ->
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
            ItemProperties.register(ModItems.SOULSPRING_LAMP.get(), ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "soulspring_state"), SOULSPRING_LAMP_PROPERTIES);

            ItemProperties.register(ModItems.FILLED_WATERSKIN.get(), ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "water_temperature"), (stack, level, entity, id) ->
            {
                return stack.getOrDefault(ModItemComponents.WATER_TEMPERATURE, 0d).floatValue();
            });

            ItemProperties.register(ModItems.THERMOMETER.get(), ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "temperature"), (stack, level, livingEntity, id) ->
            {
                if (livingEntity instanceof Player player)
                {
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

    private static final Field ITEM_PROPERTIES = ObfuscationReflectionHelper.findField(ItemProperties.class, "PROPERTIES");
    static { ITEM_PROPERTIES.setAccessible(true); }
    public static void unregister(Item item)
    {
        try
        {
            Map<Item, Map<ResourceLocation, ItemPropertyFunction>> properties = (Map<Item, Map<ResourceLocation, ItemPropertyFunction >>) ITEM_PROPERTIES.get(null);
            properties.remove(item);
        }
        catch (IllegalAccessException e)
        {   throw new RuntimeException(e);
        }
    }
}
