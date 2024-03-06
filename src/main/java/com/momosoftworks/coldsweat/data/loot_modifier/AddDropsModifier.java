package com.momosoftworks.coldsweat.data.loot_modifier;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class AddDropsModifier extends LootModifier
{
    private final Item addition;
    private final Pair<Integer, Integer> count;

    protected AddDropsModifier(LootItemCondition[] conditionsIn, final Item addition, final Pair<Integer, Integer> count)
    {
        super(conditionsIn);
        this.addition = addition;
        this.count = count;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        int countMin = count.getFirst();
        int countMax = count.getSecond();
        int countRange = countMax - countMin + 1;
        generatedLoot.add(new ItemStack(addition,
                                        context.getRandom().nextInt(countMin, countMax + 1)
                                        + context.getRandom().nextInt(countRange * context.getLootingModifier() + 1)));
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AddDropsModifier>
    {
        @Override
        public AddDropsModifier read(@Nonnull ResourceLocation location, JsonObject object, LootItemCondition[] conditionsIn)
        {
            JsonObject countTable = GsonHelper.getAsJsonObject(object, "count");
            return new AddDropsModifier(conditionsIn,
                                        ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(object, "addition"))),
                                        Pair.of(GsonHelper.getAsInt(countTable, "min"), GsonHelper.getAsInt(countTable, "max")));
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
