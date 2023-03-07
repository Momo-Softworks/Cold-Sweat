package dev.momostudios.coldsweat.core.advancement.trigger;

import com.google.gson.JsonObject;
import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ArmorInsulatedTrigger extends SimpleCriterionTrigger<ArmorInsulatedTrigger.Instance>
{
    static final ResourceLocation ID = new ResourceLocation(ColdSweat.MOD_ID, "armor_insulated");

    @Override
    protected Instance createInstance(JsonObject json, EntityPredicate.Composite player, DeserializationContext context)
    {
        ItemPredicate armorStack = ItemPredicate.fromJson(json.get("armor_item"));
        ItemPredicate insulStack = ItemPredicate.fromJson(json.get("insulation_item"));

        return new Instance(player, armorStack, insulStack);
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
        private final ItemPredicate armorStack;
        private final ItemPredicate insulStack;

        public Instance(EntityPredicate.Composite player, ItemPredicate armorStack, ItemPredicate insulStack)
        {
            super(ID, player);
            this.armorStack = armorStack;
            this.insulStack = insulStack;
        }

        public boolean matches(ItemStack fuelStack, ItemStack lampStack)
        {
            return this.armorStack.matches(fuelStack) && this.insulStack.matches(lampStack);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject obj = super.serializeToJson(context);

            obj.add("armor_item", this.insulStack.serializeToJson());
            obj.add("insulation_item", this.insulStack.serializeToJson());

            return obj;
        }
    }
}

