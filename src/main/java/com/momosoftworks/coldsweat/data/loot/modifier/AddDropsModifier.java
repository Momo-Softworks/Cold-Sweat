package com.momosoftworks.coldsweat.data.loot.modifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.data.codec.LootEntry;
import com.momosoftworks.coldsweat.util.serialization.JsonHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AddDropsModifier extends LootModifier
{
    private final List<LootEntry> additions;

    protected AddDropsModifier(ILootCondition[] conditionsIn, List<LootEntry> additions)
    {
        super(conditionsIn);
        this.additions = additions;
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
            return new AddDropsModifier(conditionsIn, additions);
        }

        @Override
        public JsonObject write(AddDropsModifier instance)
        {
            JsonObject object = new JsonObject();
            JsonArray additions = new JsonArray();
            for (LootEntry entry : instance.additions)
            {
                additions.add(LootEntry.CODEC.encodeStart(JsonOps.INSTANCE, entry).result().orElseThrow(RuntimeException::new));
            }
            object.add("additions", additions);
            return object;
        }
    }
}
