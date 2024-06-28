package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.core.advancement.trigger.ArmorInsulatedTrigger;
import com.momosoftworks.coldsweat.core.advancement.trigger.BlockAffectTempTrigger;
import com.momosoftworks.coldsweat.core.advancement.trigger.SoulLampFuelledTrigger;
import com.momosoftworks.coldsweat.core.advancement.trigger.TemperatureChangedTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAdvancementTriggers
{
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, ColdSweat.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, TemperatureChangedTrigger> TEMPERATURE_CHANGED = TRIGGERS.register("temperature_changed", TemperatureChangedTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, SoulLampFuelledTrigger> SOUL_LAMP_FUELED = TRIGGERS.register("soulspring_lamp_fueled", SoulLampFuelledTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, BlockAffectTempTrigger> BLOCK_AFFECTS_TEMP = TRIGGERS.register("block_affects_temperature", BlockAffectTempTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, ArmorInsulatedTrigger> ARMOR_INSULATED = TRIGGERS.register("armor_insulated", ArmorInsulatedTrigger::new);
}
