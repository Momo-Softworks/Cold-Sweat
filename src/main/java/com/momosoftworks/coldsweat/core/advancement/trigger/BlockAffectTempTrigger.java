package com.momosoftworks.coldsweat.core.advancement.trigger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.mixin.MixinTriggerBounds;
import net.minecraft.advancements.criterion.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockAffectTempTrigger extends AbstractCriterionTrigger<BlockAffectTempTrigger.Instance>
{
    static final ResourceLocation ID = new ResourceLocation(ColdSweat.MOD_ID, "block_affects_temperature");

    @Override
    protected Instance createInstance(JsonObject json, EntityPredicate.AndPredicate player, ConditionArrayParser context)
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

                Temperature.Trait trait = Temperature.Trait.fromID(entry.get("type").getAsString());
                TriggerHelper.getTempValueOrRange(entry)
                        .ifLeft(either -> conditions.add(new TriggerHelper.TempCondition(trait, either, either)))
                        .ifRight(pair -> conditions.add(new TriggerHelper.TempCondition(trait, pair.getFirst(), pair.getSecond())));
            }
        }

        return new Instance(player, block, distance, totalEffect, conditions);
    }

    @Override
    public ResourceLocation getId()
    {   return ID;
    }

    public void trigger(ServerPlayerEntity player, BlockPos pos, double distance, double totalEffect)
    {   this.trigger(player, triggerInstance -> triggerInstance.matches(player, pos, distance, totalEffect));
    }

    public static class Instance extends CriterionInstance
    {
        BlockPredicate block;
        MinMaxBounds.FloatBound distance;
        MinMaxBounds.FloatBound totalEffect;
        List<TriggerHelper.TempCondition> conditions;

        public Instance(EntityPredicate.AndPredicate player, BlockPredicate block, double distance, double totalEffect, List<TriggerHelper.TempCondition> conditions)
        {
            super(ID, player);
            this.block = block;
            this.distance = distance > 0 ? new MixinTriggerBounds.MixinFloatBound().atMost((float) distance) : MinMaxBounds.FloatBound.atLeast(0);
            this.totalEffect = totalEffect < 0 ? new MixinTriggerBounds.MixinFloatBound().atMost((float) totalEffect) : MinMaxBounds.FloatBound.atLeast((float) totalEffect);
            this.conditions = conditions;
        }

        public boolean matches(ServerPlayerEntity player, BlockPos pos, double distance, double totalEffect)
        {
            Map<Temperature.Trait, Double> temps = Temperature.getTemperatures(player);
            return this.distance.matches(((float) distance))
                && this.totalEffect.matches(((float) totalEffect))
                && this.block.matches(player.getLevel(), pos)
                && conditions.stream().allMatch(condition -> condition.matches(temps.get(condition.trait())));
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer context)
        {
            JsonObject obj = super.serializeToJson(context);

            obj.add("distance", this.distance.serializeToJson());
            obj.add("total_effect", this.totalEffect.serializeToJson());
            obj.add("blocks", this.block.serializeToJson());

            return obj;
        }
    }
}
