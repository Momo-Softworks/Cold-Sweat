package dev.momostudios.coldsweat.core.advancement.trigger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockAffectTempTrigger extends SimpleCriterionTrigger<BlockAffectTempTrigger.Instance>
{
    static final ResourceLocation ID = new ResourceLocation(ColdSweat.MOD_ID, "block_affects_temperature");

    @Override
    protected Instance createInstance(JsonObject json, EntityPredicate.Composite player, DeserializationContext context)
    {
        double distance = json.has("distance") ? json.get("distance").getAsDouble() : 0;
        double totalEffect = json.has("total_effect") ? json.get("total_effect").getAsDouble() : 0;
        BlockPredicate block = BlockPredicate.fromJson(json.get("blocks"));

        List<TriggerHelper.TempCondition> conditions = new ArrayList<>();

        if (json.has("temperature"))
        {
            JsonArray tempList = json.get("temperature").getAsJsonArray();
            for (JsonElement element : tempList)
            {
                JsonObject entry = element.getAsJsonObject();

                Temperature.Type type = Temperature.Type.fromID(entry.get("type").getAsString());
                TriggerHelper.getTempValueOrRange(entry)
                        .ifLeft(either -> conditions.add(new TriggerHelper.TempCondition(type, either, either)))
                        .ifRight(pair -> conditions.add(new TriggerHelper.TempCondition(type, pair.getFirst(), pair.getSecond())));
            }
        }

        return new Instance(player, block, distance, totalEffect, conditions);
    }

    @Override
    public ResourceLocation getId()
    {   return ID;
    }

    public void trigger(ServerPlayer player, BlockPos pos, double distance, double totalEffect)
    {   this.trigger(player, triggerInstance -> triggerInstance.matches(player, pos, distance, totalEffect));
    }

    public static class Instance extends AbstractCriterionTriggerInstance
    {
        BlockPredicate block;
        MinMaxBounds.Doubles distance;
        MinMaxBounds.Doubles totalEffect;
        List<TriggerHelper.TempCondition> conditions;

        public Instance(EntityPredicate.Composite player, BlockPredicate block, double distance, double totalEffect, List<TriggerHelper.TempCondition> conditions)
        {
            super(ID, player);
            this.block = block;
            this.distance = distance > 0 ? MinMaxBounds.Doubles.atMost(distance) : MinMaxBounds.Doubles.atLeast(0);
            this.totalEffect = totalEffect < 0 ? MinMaxBounds.Doubles.atMost(totalEffect) : MinMaxBounds.Doubles.atLeast(totalEffect);
            this.conditions = conditions;
        }

        public boolean matches(ServerPlayer player, BlockPos pos, double distance, double totalEffect)
        {
            Map<Temperature.Type, Double> temps = Temperature.getTemperatures(player);
            return this.distance.matches(distance)
                && this.totalEffect.matches(totalEffect)
                && this.block.matches(player.getLevel(), pos)
                && conditions.stream().allMatch(condition -> condition.matches(temps.get(condition.type())));
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject obj = super.serializeToJson(context);

            obj.add("distance", this.distance.serializeToJson());
            obj.add("total_effect", this.totalEffect.serializeToJson());
            obj.add("blocks", this.block.serializeToJson());

            return obj;
        }
    }
}
