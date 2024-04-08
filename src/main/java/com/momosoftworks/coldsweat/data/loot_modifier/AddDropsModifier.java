package com.momosoftworks.coldsweat.data.loot_modifier;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
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
import java.util.List;

public class AddDropsModifier extends LootModifier
{
    private final Item addition;
    private final Pair<Integer, Integer> count;

    protected AddDropsModifier(ILootCondition[] conditionsIn, final Item addition, final Pair<Integer, Integer> count)
    {
        super(conditionsIn);
        this.addition = addition;
        this.count = count;
    }

    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        int multiplier = count.getSecond() - count.getFirst() + 1;
        generatedLoot.add(new ItemStack(addition,
                                        context.getRandom().nextInt(multiplier) + count.getFirst()
                                      + context.getRandom().nextInt(multiplier * context.getLootingModifier() + 1)));
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AddDropsModifier>
    {
        @Override
        public AddDropsModifier read(@Nonnull ResourceLocation location, JsonObject object, ILootCondition[] conditionsIn)
        {
            JsonObject countTable = JsonHelper.getAsJsonObject(object, "count");
            return new AddDropsModifier(conditionsIn,
                                        ForgeRegistries.ITEMS.getValue(new ResourceLocation(JsonHelper.getAsString(object, "addition"))),
                                        Pair.of(JsonHelper.getAsInt(countTable, "min"), JsonHelper.getAsInt(countTable, "max")));
        }

        @Override
        public JsonObject write(AddDropsModifier instance)
        {
            JsonObject json = makeConditions(instance.conditions);
            json.addProperty("addition", instance.addition.getRegistryName().toString());
            return json;
        }
    }
}
