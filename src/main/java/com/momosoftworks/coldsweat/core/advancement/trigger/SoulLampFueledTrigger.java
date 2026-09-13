package com.momosoftworks.coldsweat.core.advancement.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class SoulLampFueledTrigger extends SimpleCriterionTrigger<SoulLampFueledTrigger.Instance>
{
    @Override
    public Codec<Instance> codec()
    {   return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack fuelStack, ItemStack lampStack)
    {   this.trigger(player, triggerInstance -> triggerInstance.matches(fuelStack, lampStack));
    }

    public record Instance(Optional<ContextAwarePredicate> player, List<ItemPredicate> fuelPredicates, List<ItemPredicate> lampPredicates) implements SimpleInstance
    {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                ItemPredicate.CODEC.listOf().optionalFieldOf("fuel_item", List.of()).forGetter(Instance::fuelPredicates),
                ItemPredicate.CODEC.listOf().optionalFieldOf("lamp_item", List.of()).forGetter(Instance::lampPredicates)
        ).apply(instance, Instance::new));

        public boolean matches(ItemStack fuelStack, ItemStack lampStack)
        {
            boolean lampMatches = this.lampPredicates.isEmpty();
            for (ItemPredicate predicate : this.lampPredicates)
            {
                if (predicate.test(lampStack))
                {   lampMatches = true;
                    break;
                }
            }
            if (!lampMatches) return false;

            boolean fuelMatches = this.fuelPredicates.isEmpty();
            for (ItemPredicate predicate : this.fuelPredicates)
            {
                if (predicate.test(fuelStack))
                {   fuelMatches = true;
                    break;
                }
            }
            return fuelMatches;
        }
    }
}