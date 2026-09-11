package com.momosoftworks.coldsweat.core.advancement.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ArmorInsulatedTrigger extends SimpleCriterionTrigger<ArmorInsulatedTrigger.Instance>
{
    @Override
    public Codec<Instance> codec()
    {   return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack armorStack, ItemStack insulatorStack)
    {   this.trigger(player, triggerInstance -> triggerInstance.matches(armorStack, insulatorStack));
    }

    public record Instance(Optional<ContextAwarePredicate> player, List<ItemPredicate> armorPredicates, List<ItemPredicate> insulatorPredicates) implements SimpleInstance
    {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ItemPredicate.CODEC.listOf().optionalFieldOf("armor", List.of()).forGetter(Instance::armorPredicates),
            ItemPredicate.CODEC.listOf().optionalFieldOf("insulated", List.of()).forGetter(Instance::insulatorPredicates)
        ).apply(instance, Instance::new));

        public boolean matches(ItemStack armorStack, ItemStack insulatorStack)
        {
            boolean armorMatches = this.armorPredicates.isEmpty();
            for (ItemPredicate predicate : this.armorPredicates)
            {
                if (predicate.test(armorStack))
                {   armorMatches = true;
                    break;
                }
            }
            if (!armorMatches) return false;

            boolean insulatorMatches = this.insulatorPredicates.isEmpty();
            for (ItemPredicate predicate : this.insulatorPredicates)
            {
                if (predicate.test(insulatorStack))
                {   insulatorMatches = true;
                    break;
                }
            }
            return insulatorMatches;
        }
    }
}

