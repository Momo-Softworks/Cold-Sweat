package dev.momostudios.coldsweat.core.advancement.trigger;

import com.google.gson.JsonObject;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class SoulLampFuelledTrigger extends SimpleCriterionTrigger<SoulLampFuelledTrigger.Instance>
{
    static final ResourceLocation ID = new ResourceLocation(ColdSweat.MOD_ID, "soulspring_lamp_fuelled");

    @Override
    protected Instance createInstance(JsonObject json, EntityPredicate.Composite player, DeserializationContext context)
    {
        ItemPredicate[] fuelStack = ItemPredicate.fromJsonArray(json.get("fuel_item"));
        ItemPredicate lampStack = ItemPredicate.fromJson(json.get("lamp_item"));

        return new Instance(player, fuelStack, lampStack);
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    public void trigger(ServerPlayer player, ItemStack fuelStack, ItemStack lampStack)
    {
        this.trigger(player, triggerInstance -> triggerInstance.matches(fuelStack, lampStack));
    }

    public static class Instance extends AbstractCriterionTriggerInstance
    {
        private final ItemPredicate[] fuelStack;
        private final ItemPredicate lampStack;

        public Instance(EntityPredicate.Composite player, ItemPredicate[] fuelStack, ItemPredicate lampStack)
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
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject obj = super.serializeToJson(context);

            obj.add("lamp_item", this.lampStack.serializeToJson());
            JsonObject fuelStack = new JsonObject();
            for (int i = 0; i < this.fuelStack.length; i++)
            {   fuelStack.add("fuel_item_" + i, this.fuelStack[i].serializeToJson());
            }
            obj.add("fuel_item", fuelStack);

            return obj;
        }
    }
}

