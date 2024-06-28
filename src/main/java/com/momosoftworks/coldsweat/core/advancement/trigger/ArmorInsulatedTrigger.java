package com.momosoftworks.coldsweat.core.advancement.trigger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
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

    public void trigger(ServerPlayer player, ItemStack fuelStack, ItemStack lampStack)
    {   this.trigger(player, triggerInstance -> triggerInstance.matches(fuelStack, lampStack));
    }

    public record Instance(Optional<ContextAwarePredicate> player, ItemPredicate armorStack, ItemPredicate[] insulStack) implements SimpleInstance
    {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ItemPredicate.CODEC.fieldOf("armor").forGetter(Instance::armorStack),
            ItemPredicate.CODEC.listOf().xmap(list -> list.toArray(new ItemPredicate[0]), arr -> List.of(arr)).fieldOf("insulated").forGetter(Instance::insulStack)
        ).apply(instance, Instance::new));

        public boolean matches(ItemStack fuelStack, ItemStack lampStack)
        {
            return this.armorStack.test(fuelStack)
                && (this.insulStack.length == 0 || Arrays.stream(this.insulStack).anyMatch(predicate -> predicate.test(lampStack)));
        }
    }
}

