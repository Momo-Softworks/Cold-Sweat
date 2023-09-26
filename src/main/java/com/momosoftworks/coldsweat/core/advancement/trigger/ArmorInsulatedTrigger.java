package com.momosoftworks.coldsweat.core.advancement.trigger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

public class ArmorInsulatedTrigger extends AbstractCriterionTrigger<ArmorInsulatedTrigger.Instance>
{
    static final ResourceLocation ID = new ResourceLocation(ColdSweat.MOD_ID, "armor_insulated");

    @Override
    protected Instance createInstance(JsonObject json, EntityPredicate.AndPredicate player, ConditionArrayParser context)
    {
        ItemPredicate armorStack = ItemPredicate.fromJson(json.get("armor_item"));
        ItemPredicate[] insulStack = ItemPredicate.fromJsonArray(json.get("insulation_item"));

        return new Instance(player, armorStack, insulStack);
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    public void trigger(ServerPlayerEntity player, ItemStack fuelStack, ItemStack lampStack)
    {
        this.trigger(player, triggerInstance -> triggerInstance.matches(fuelStack, lampStack));
    }

    public static class Instance extends CriterionInstance
    {
        private final ItemPredicate armorStack;
        private final ItemPredicate[] insulStack;

        public Instance(EntityPredicate.AndPredicate player, ItemPredicate armorStack, ItemPredicate[] insulStack)
        {
            super(ID, player);
            this.armorStack = armorStack;
            this.insulStack = insulStack;
        }

        public boolean matches(ItemStack fuelStack, ItemStack lampStack)
        {
            return this.armorStack.matches(fuelStack)
                && (this.insulStack.length == 0 || Arrays.stream(this.insulStack).anyMatch(predicate -> predicate.matches(lampStack)));
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer context)
        {
            JsonObject obj = super.serializeToJson(context);

            obj.add("armor_item", this.armorStack.serializeToJson());

            JsonArray jsonarray = new JsonArray();
            for (ItemPredicate itemPredicate : insulStack)
            {   jsonarray.add(itemPredicate.serializeToJson());
            }
            obj.add("insulation_item", jsonarray);

            return obj;
        }
    }
}

