package com.momosoftworks.coldsweat.core.advancement.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlockAffectTempTrigger extends SimpleCriterionTrigger<BlockAffectTempTrigger.Instance>
{
    @Override
    public Codec<Instance> codec()
    {   return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockPos pos, double distance, double totalEffect)
    {   this.trigger(player, triggerInstance -> triggerInstance.matches(player, pos, distance, totalEffect));
    }

    public record Instance(Optional<ContextAwarePredicate> player, BlockPredicate block, MinMaxBounds.Doubles distance, MinMaxBounds.Doubles totalEffect, List<TriggerHelper.TempCondition> conditions) implements SimpleInstance
    {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                BlockPredicate.CODEC.fieldOf("block").forGetter(Instance::block),
                MinMaxBounds.Doubles.CODEC.fieldOf("distance").forGetter(Instance::distance),
                MinMaxBounds.Doubles.CODEC.fieldOf("total_effect").forGetter(Instance::totalEffect),
                Codec.list(TriggerHelper.TempCondition.CODEC).fieldOf("conditions").forGetter(Instance::conditions)
        ).apply(instance, Instance::new));

        public boolean matches(ServerPlayer player, BlockPos pos, double distance, double totalEffect)
        {
            Map<Temperature.Trait, Double> temps = Temperature.getTemperatures(player);
            return this.distance.matches(distance)
                    && this.totalEffect.matches(totalEffect)
                    && this.block.matches(player.serverLevel(), pos)
                    && conditions.stream().allMatch(condition -> condition.matches(temps.get(condition.trait())));
        }
    }
}