package com.momosoftworks.coldsweat.compat.kubejs;

import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.util.serialization.DynamicHolder;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import dev.latvian.kubejs.world.BlockContainerJS;
import dev.latvian.kubejs.world.WorldJS;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class KubeBindings
{
    public static DynamicHolder<?> getConfigSetting(String id)
    {   return ConfigSettings.getSetting(new ResourceLocation(id));
    }

    public static DynamicRegistries getRegistryAccess()
    {   return RegistryHelper.getDynamicRegistries();
    }

    public static double getTemperature(Entity entity, String trait)
    {
        if (entity instanceof LivingEntity)
        {   return Temperature.get(((LivingEntity) entity), Temperature.Trait.fromID(trait));
        }
        return 0;
    }

    public static void setTemperature(Entity entity, String trait, double temperature)
    {
        if (entity instanceof LivingEntity)
        {   Temperature.set(((LivingEntity) entity), Temperature.Trait.fromID(trait), temperature);
        }
    }

    public static double convertTemperature(double temperature, String from, String to, boolean absolute)
    {   return Temperature.convert(temperature, Temperature.Units.fromID(from), Temperature.Units.fromID(to), absolute);
    }

    @Nullable
    public static TempModifier createModifier(String id)
    {   return TempModifierRegistry.getValue(new ResourceLocation(id)).orElse(null);
    }

    public static void addModifier(Entity entity, TempModifier modifier, String trait)
    {
        if (entity instanceof LivingEntity)
        {   Temperature.addModifier(((LivingEntity) entity), modifier, Temperature.Trait.fromID(trait), Placement.LAST);
        }
    }

    public static List<TempModifier> getModifiers(Entity entity, String id, String trait)
    {
        if (entity instanceof LivingEntity)
        {
            ResourceLocation modifierId = new ResourceLocation(id);
            return Temperature.getModifiers(((LivingEntity) entity), Temperature.Trait.fromID(trait), mod -> mod.getID().equals(modifierId));
        }
        return null;
    }

    public static Temperature.Trait getTrait(String id)
    {   return Temperature.Trait.fromID(id);
    }

    public static double getColdInsulation(Entity entity)
    {
        if (!(entity instanceof LivingEntity))
        {   return 0;
        }
        double coldInsulation = 0;
        for (InsulatorData insulator : EntityTempManager.getInsulatorsOnEntity(((LivingEntity) entity)).values())
        {   coldInsulation += insulator.getCold();
        }
        return coldInsulation;
    }

    public static double getHeatInsulation(Entity entity)
    {
        if (!(entity instanceof LivingEntity))
        {   return 0;
        }
        double heatInsulation = 0;
        for (InsulatorData insulator : EntityTempManager.getInsulatorsOnEntity(((LivingEntity) entity)).values())
        {   heatInsulation += insulator.getHeat();
        }
        return heatInsulation;
    }

    public static double getBlockTemperature(BlockContainerJS block)
    {   return WorldHelper.getBlockTemperature(block.getLevel().minecraftLevel, block.getBlockState());
    }

    public static double getBiomeTemperature(WorldJS level, BlockPos pos)
    {   return WorldHelper.getBiomeTemperature(level.minecraftLevel, level.minecraftLevel.getBiome(pos));
    }

    public static double getTemperatureAt(World level, BlockPos pos)
    {   return WorldHelper.getTemperatureAt(level, pos);
    }
}
