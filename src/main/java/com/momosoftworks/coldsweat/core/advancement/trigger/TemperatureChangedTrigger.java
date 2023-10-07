package com.momosoftworks.coldsweat.core.advancement.trigger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TemperatureChangedTrigger extends SimpleCriterionTrigger<TemperatureChangedTrigger.Instance>
{
    static final ResourceLocation ID = new ResourceLocation(ColdSweat.MOD_ID, "temperature");

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context)
    {
        JsonArray tempList = json.get("temperature").getAsJsonArray();
        List<TriggerHelper.TempCondition> conditions = new ArrayList<>();

        for (JsonElement element : tempList)
        {
            JsonObject entry = element.getAsJsonObject();

            Temperature.Type type = Temperature.Type.fromID(entry.get("type").getAsString());

            TriggerHelper.getTempValueOrRange(entry)
                 .ifLeft(either -> conditions.add(new TriggerHelper.TempCondition(type, either, either)))
                 .ifRight(pair  -> conditions.add(new TriggerHelper.TempCondition(type, pair.getFirst(), pair.getSecond())));
        }

        return new Instance(player, conditions);
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    public void trigger(ServerPlayer player, Map<Temperature.Type, Double> temps)
    {
        this.trigger(player, triggerInstance -> triggerInstance.matches(temps));
    }

    public static class Instance extends AbstractCriterionTriggerInstance
    {
        List<TriggerHelper.TempCondition> conditions;

        public Instance(ContextAwarePredicate player, List<TriggerHelper.TempCondition> conditions)
        {
            super(ID, player);
            this.conditions = conditions;
        }

        public boolean matches(Map<Temperature.Type, Double> temps)
        {
            for (TriggerHelper.TempCondition condition : conditions)
            {
                double value = temps.get(condition.type());

                if (!condition.matches(value))
                    return false;
            }
            return true;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject obj = super.serializeToJson(context);
            obj.add("temperature", TriggerHelper.serializeConditions(this.conditions));

            return obj;
        }
    }
}

