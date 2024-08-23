package com.momosoftworks.coldsweat.core.init;

import com.blackgear.cavesandcliffs.common.entity.GoatTasks;
import com.blackgear.cavesandcliffs.common.entity.ai.sensor.TemptationsSensor;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SensorTypeInit
{
    public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, ColdSweat.MOD_ID);

    public static final RegistryObject<SensorType<TemptationsSensor>> GOAT_TEMPTATIONS = SENSORS.register("goat_temptations", () -> new SensorType<>(() -> new TemptationsSensor(GoatTasks.getTemptItems())));
}
