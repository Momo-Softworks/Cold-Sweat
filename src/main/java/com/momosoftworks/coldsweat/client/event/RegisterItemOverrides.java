package com.momosoftworks.coldsweat.client.event;

import com.mojang.serialization.MapCodec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.client.renderer.item.SoulSpringLampRenderer;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.common.entity.data.Preference;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

import javax.annotation.Nullable;

/**
 * Item model properties used by Cold Sweat's item model definitions ({@code assets/cold_sweat/items/*.json}).<br>
 * These replace the item property overrides used before 1.21.4.
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class RegisterItemOverrides
{
    @SubscribeEvent
    public static void registerRangeProperties(RegisterRangeSelectItemModelPropertyEvent event)
    {
        event.register(ColdSweat.createKey("soulspring_state"), SoulspringLampState.MAP_CODEC);
        event.register(ColdSweat.createKey("water_temperature"), WaterTemperature.MAP_CODEC);
        event.register(ColdSweat.createKey("temperature"), ThermometerTemperature.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerConditionalProperties(RegisterConditionalItemModelPropertyEvent event)
    {   event.register(ColdSweat.createKey("animated_soulspring_lamp"), AnimatedSoulspringLamp.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event)
    {   event.register(ColdSweat.createKey("soulspring_lamp"), SoulSpringLampRenderer.Unbaked.MAP_CODEC);
    }

    /**
     * 0 when unlit, otherwise 1-3 based on the lamp's fuel
     */
    public record SoulspringLampState() implements RangeSelectItemModelProperty
    {
        public static final MapCodec<SoulspringLampState> MAP_CODEC = MapCodec.unit(new SoulspringLampState());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed)
        {
            if (SoulspringLampItem.isLit(stack))
            {
                double fuel = SoulspringLampItem.getFuel(stack);
                return fuel > 43 ? 3 :
                       fuel > 22 ? 2 : 1;
            }
            return 0;
        }

        @Override
        public MapCodec<SoulspringLampState> type()
        {   return MAP_CODEC;
        }
    }

    /**
     * Whether the animated soulspring lamp model is enabled in the config
     */
    public record AnimatedSoulspringLamp() implements ConditionalItemModelProperty
    {
        public static final MapCodec<AnimatedSoulspringLamp> MAP_CODEC = MapCodec.unit(new AnimatedSoulspringLamp());

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext)
        {   return ConfigSettings.ANIMATED_SOULSPRING_LAMP_MODEL.get();
        }

        @Override
        public MapCodec<AnimatedSoulspringLamp> type()
        {   return MAP_CODEC;
        }
    }

    public record WaterTemperature() implements RangeSelectItemModelProperty
    {
        public static final MapCodec<WaterTemperature> MAP_CODEC = MapCodec.unit(new WaterTemperature());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed)
        {   return stack.getOrDefault(ModItemComponents.WATER_TEMPERATURE, 0d).floatValue();
        }

        @Override
        public MapCodec<WaterTemperature> type()
        {   return MAP_CODEC;
        }
    }

    /**
     * The world temperature severity (-1 to 1) for the player holding the thermometer
     */
    public record ThermometerTemperature() implements RangeSelectItemModelProperty
    {
        public static final MapCodec<ThermometerTemperature> MAP_CODEC = MapCodec.unit(new ThermometerTemperature());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed)
        {
            if (owner != null && owner.asLivingEntity() instanceof Player player)
            {
                double minTemp = Temperature.get(player, Temperature.Trait.FREEZING_POINT);
                double maxTemp = Temperature.get(player, Temperature.Trait.BURNING_POINT);

                double worldTemp;
                if (!player.getPersistentData().contains("WorldTempTimestamp")
                || (player.tickCount % 2 == 0 && player.getPersistentData().getIntOr("WorldTempTimestamp", 0) != player.tickCount))
                {
                    worldTemp = Temperature.convert(Overlays.WORLD_TEMP, Preference.getOrDefault(player, Preference.UNITS, Temperature.Units.F), Temperature.Units.MC, true);

                    player.getPersistentData().putDouble("WorldTemp", worldTemp);
                    player.getPersistentData().putInt("WorldTempTimestamp", player.tickCount);
                }
                else worldTemp = player.getPersistentData().getDoubleOr("WorldTemp", 0);

                return (float) (Overlays.getWorldSeverity(worldTemp, minTemp, maxTemp) * 1.01);
            }
            return 0;
        }

        @Override
        public MapCodec<ThermometerTemperature> type()
        {   return MAP_CODEC;
        }
    }
}
