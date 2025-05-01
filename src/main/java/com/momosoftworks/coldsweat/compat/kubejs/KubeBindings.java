package com.momosoftworks.coldsweat.compat.kubejs;

import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Placement;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.util.serialization.DynamicHolder;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import dev.latvian.mods.kubejs.level.BlockContainerJS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class KubeBindings
{
    public static DynamicHolder<?> getConfigSetting(String id)
    {   return ConfigSettings.getSetting(id);
    }

    public static RegistryAccess getRegistryAccess()
    {   return RegistryHelper.getRegistryAccess();
    }

    public static double getTemperature(Entity entity, String trait)
    {
        if (entity instanceof LivingEntity living)
        {   return Temperature.get(living, Temperature.Trait.fromID(trait));
        }
        return 0;
    }

    public static void setTemperature(Entity entity, String trait, double temperature)
    {
        if (entity instanceof LivingEntity living)
        {   Temperature.set(living, Temperature.Trait.fromID(trait), temperature);
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
        if (entity instanceof LivingEntity living)
        {   Temperature.addModifier(living, modifier, Temperature.Trait.fromID(trait), Placement.Duplicates.ALLOW);
        }
    }

    public static Temperature.Trait getTrait(String id)
    {   return Temperature.Trait.fromID(id);
    }

    public static double getColdInsulation(Entity entity)
    {
        if (!(entity instanceof LivingEntity living))
        {   return 0;
        }
        double coldInsulation = 0;
        for (InsulatorData insulator : EntityTempManager.getInsulatorsOnEntity(living).values())
        {   coldInsulation += insulator.getCold();
        }
        return coldInsulation;
    }

    public static double getHeatInsulation(Entity entity)
    {
        if (!(entity instanceof LivingEntity living))
        {   return 0;
        }
        double heatInsulation = 0;
        for (InsulatorData insulator : EntityTempManager.getInsulatorsOnEntity(living).values())
        {   heatInsulation += insulator.getHeat();
        }
        return heatInsulation;
    }

    public static double getBlockTemperature(BlockContainerJS block)
    {   return WorldHelper.getBlockTemperature(block.getLevel().getMinecraftLevel(), block.getBlockState());
    }

    public static double getBiomeTemperature(Level level, BlockPos pos)
    {   return WorldHelper.getBiomeTemperature(level, level.getBiome(pos));
    }

    public static double getTemperatureAt(Level level, BlockPos pos)
    {   return WorldHelper.getTemperatureAt(level, pos);
    }
}
