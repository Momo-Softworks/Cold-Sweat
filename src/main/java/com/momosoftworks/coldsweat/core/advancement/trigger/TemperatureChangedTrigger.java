package com.momosoftworks.coldsweat.core.advancement.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TemperatureChangedTrigger extends SimpleCriterionTrigger<TemperatureChangedTrigger.Instance>
{
    @Override
    public Codec<Instance> codec()
    {   return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, Map<Temperature.Trait, Double> temps)
    {   this.trigger(player, triggerInstance -> triggerInstance.matches(temps));
    }

    public record Instance(Optional<ContextAwarePredicate> player, List<TriggerHelper.TempCondition> conditions) implements SimpleInstance
    {
        public static Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.list(TriggerHelper.TempCondition.CODEC).fieldOf("temperature").forGetter(Instance::conditions)
        ).apply(instance, Instance::new));

        public boolean matches(Map<Temperature.Trait, Double> temps)
        {
            for (TriggerHelper.TempCondition condition : conditions)
            {
                double value = temps.get(condition.trait());

                if (!condition.matches(value))
                    return false;
            }
            return true;
        }
    }
}

