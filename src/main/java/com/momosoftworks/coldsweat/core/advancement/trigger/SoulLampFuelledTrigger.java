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

//TODO: Make sure these codecs work
public class SoulLampFuelledTrigger extends SimpleCriterionTrigger<SoulLampFuelledTrigger.Instance>
{
    @Override
    public Codec<Instance> codec()
    {   return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack fuelStack, ItemStack lampStack)
    {   this.trigger(player, triggerInstance -> triggerInstance.matches(fuelStack, lampStack));
    }

    public record Instance(Optional<ContextAwarePredicate> player, ItemPredicate[] fuelStack, ItemPredicate lampStack) implements SimpleInstance
    {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
                ItemPredicate.CODEC.listOf().xmap(list -> list.toArray(new ItemPredicate[0]), arr -> List.of(arr)).fieldOf("fuel_item").forGetter(Instance::fuelStack),
                ItemPredicate.CODEC.fieldOf("lamp_item").forGetter(Instance::lampStack)
        ).apply(instance, Instance::new));

        public boolean matches(ItemStack fuelStack, ItemStack lampStack)
        {
            if (!this.lampStack.test(lampStack)) return false;

            if (fuelStack.isEmpty()) return true;
            for (ItemPredicate predicate : this.fuelStack)
            {
                if (predicate.test(fuelStack))
                    return true;
            }
            return false;
        }
    }
}