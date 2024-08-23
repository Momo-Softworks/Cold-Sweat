package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class ActivityInit
{
    public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(Activity.class, ColdSweat.MOD_ID);

    public static final RegistryObject<Activity> LONG_JUMP = ACTIVITIES.register("long_jump", () -> new Activity("cold_sweat:long_jump"));
    public static final RegistryObject<Activity> RAM = ACTIVITIES.register("ram", () -> new Activity("cold_sweat:ram"));
}
