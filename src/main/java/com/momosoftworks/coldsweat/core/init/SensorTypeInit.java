package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.entity.sensor.TemptationsSensor;
import com.momosoftworks.coldsweat.common.entity.task.GoatTasks;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SensorTypeInit
{
    public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, ColdSweat.MOD_ID);

    public static final RegistryObject<SensorType<TemptationsSensor>> GOAT_TEMPTATIONS = SENSORS.register("goat_temptations", () -> new SensorType<>(() -> new TemptationsSensor(GoatTasks.getTemptItems())));
}
