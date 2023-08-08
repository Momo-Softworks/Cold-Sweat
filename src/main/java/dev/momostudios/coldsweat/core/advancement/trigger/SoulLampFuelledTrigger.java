package dev.momostudios.coldsweat.core.advancement.trigger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class SoulLampFuelledTrigger extends AbstractCriterionTrigger<SoulLampFuelledTrigger.Instance>
{
    static final ResourceLocation ID = new ResourceLocation(ColdSweat.MOD_ID, "soulspring_lamp_fuelled");

    @Override
    public Instance createInstance(JsonObject json, EntityPredicate.AndPredicate player, ConditionArrayParser context)
    {
        ItemPredicate[] fuelStack = ItemPredicate.fromJsonArray(json.get("fuel_item"));
        ItemPredicate lampStack = ItemPredicate.fromJson(json.get("lamp_item"));

        return new Instance(player, fuelStack, lampStack);
    }

    @Override
    public ResourceLocation getId()
    {   return ID;
    }

    public void trigger(ServerPlayerEntity player, ItemStack fuelStack, ItemStack lampStack)
    {
        this.trigger(player, triggerInstance -> triggerInstance.matches(fuelStack, lampStack));
    }

    public static class Instance extends CriterionInstance
    {
        private final ItemPredicate[] fuelStack;
        private final ItemPredicate lampStack;

        public Instance(EntityPredicate.AndPredicate player, ItemPredicate[] fuelStack, ItemPredicate lampStack)
        {
            super(ID, player);
            this.fuelStack = fuelStack;
            this.lampStack = lampStack;
        }

        public boolean matches(ItemStack fuelStack, ItemStack lampStack)
        {
            if (!this.lampStack.matches(lampStack)) return false;

            if (fuelStack.isEmpty()) return true;
            for (ItemPredicate predicate : this.fuelStack)
            {
                if (predicate.matches(fuelStack))
                    return true;
            }
            return false;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer context)
        {
            JsonObject obj = super.serializeToJson(context);

            obj.add("lamp_item", this.lampStack.serializeToJson());

            JsonArray jsonarray = new JsonArray();
            for (ItemPredicate itemPredicate : fuelStack)
            {   jsonarray.add(itemPredicate.serializeToJson());
            }
            obj.add("items", jsonarray);

            return obj;
        }
    }
}

