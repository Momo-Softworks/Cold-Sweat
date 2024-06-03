package com.momosoftworks.coldsweat.data.loot.modifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.data.codec.LootEntry;
import com.momosoftworks.coldsweat.util.serialization.JsonHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AddDropsModifier extends LootModifier
{
    private final List<LootEntry> additions;
    private final List<Item> removals;

    protected AddDropsModifier(ILootCondition[] conditionsIn, List<LootEntry> additions, List<Item> removals)
    {
        super(conditionsIn);
        this.additions = additions;
        this.removals = removals;
    }

    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        for (LootEntry entry : additions)
        {
            int countRange = entry.count.max - entry.count.min;
            generatedLoot.add(new ItemStack(entry.item,
                                            context.getRandom().nextInt(entry.count.max - entry.count.min + 1) + entry.count.min
                                          + context.getRandom().nextInt(countRange * context.getLootingModifier() + 1)));
        }
        if (!removals.isEmpty())
        {   generatedLoot.removeIf(stack -> removals.contains(stack.getItem()));
        }
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AddDropsModifier>
    {
        @Override
        public AddDropsModifier read(@Nonnull ResourceLocation location, JsonObject object, ILootCondition[] conditionsIn)
        {
            List<LootEntry> additions = new ArrayList<>();
            for (JsonElement element : JsonHelper.getAsJsonArray(object, "additions"))
            {   additions.add(LootEntry.CODEC.parse(JsonOps.INSTANCE, element).result().orElseThrow(RuntimeException::new));
            }
            List<Item> removals = new ArrayList<>();
            if (object.has("removals"))
            {   for (JsonElement element : JsonHelper.getAsJsonArray(object, "removals"))
                {   removals.add(ForgeRegistries.ITEMS.getValue(new ResourceLocation(element.getAsString())));
                }
            }
            return new AddDropsModifier(conditionsIn, additions, removals);
        }

        @Override
        public JsonObject write(AddDropsModifier instance)
        {
            JsonObject object = new JsonObject();
            JsonArray additions = new JsonArray();
            JsonArray removals = new JsonArray();
            for (LootEntry entry : instance.additions)
            {    additions.add(LootEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry).result().orElseThrow(RuntimeException::new));
            }
            for (Item item : instance.removals)
            {    removals.add(item.getRegistryName().toString());
            }
            object.add("additions", additions);
            object.add("removals", removals);
            return object;
        }
    }
}
