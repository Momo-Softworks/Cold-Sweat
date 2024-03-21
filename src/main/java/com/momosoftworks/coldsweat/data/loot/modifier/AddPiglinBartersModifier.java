package com.momosoftworks.coldsweat.data.loot.modifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.data.codec.LootEntry;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.JsonHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AddPiglinBartersModifier extends LootModifier
{
    private final List<LootEntry> additions;

    protected AddPiglinBartersModifier(ILootCondition[] conditions, List<LootEntry> additions)
    {   super(conditions);
        this.additions = additions;
    }

    static ResourceLocation PIGLIN_BARTER_LOCATION = new ResourceLocation("gameplay/piglin_bartering");
    static Field POOLS;
    static Field ENTRIES;
    static
    {
        POOLS = ObfuscationReflectionHelper.findField(LootTable.class, "field_186466_c");
        ENTRIES = ObfuscationReflectionHelper.findField(LootPool.class, "field_186453_a");
        POOLS.setAccessible(true);
        ENTRIES.setAccessible(true);
    }

    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        try
        {
            List<ILootGenerator> entries = new ArrayList<>();
            MutableInt totalWeight = new MutableInt();
            // Build vanilla items
            for (LootPool pool : ((List<LootPool>) POOLS.get(context.getLootTable(PIGLIN_BARTER_LOCATION))))
            {
                for (net.minecraft.loot.LootEntry container : ((net.minecraft.loot.LootEntry[]) ENTRIES.get(pool)))
                {
                    container.expand(context, entry ->
                    {   entries.add(entry);
                        totalWeight.add(entry.getWeight(context.getLuck()));
                    });
                }
            }

            // Build added items
            for (LootEntry addition : additions)
            {
                entries.add(new ILootGenerator()
                {
                    @Override
                    public int getWeight(float luck)
                    {   return CSMath.floor(addition.weight * (1 + context.getLuck()));
                    }

                    @Override
                    public void createItemStack(Consumer<ItemStack> consumer, LootContext context1)
                    {   consumer.accept(new ItemStack(addition.item, context.getRandom().nextInt(addition.count.max + 1) + addition.count.min));
                    }
                });
                totalWeight.add(addition.weight);
            }

            AtomicReference<ItemStack> stack = new AtomicReference<>(ItemStack.EMPTY);
            int j = context.getRandom().nextInt(totalWeight.intValue());

            for (ILootGenerator lootpoolentry : entries)
            {   j -= lootpoolentry.getWeight(context.getLuck());
                if (j < 0)
                {   lootpoolentry.createItemStack(stack::set, context);
                    return ObjectArrayList.wrap(new ItemStack[]{stack.get()});
                }
            }
        } catch (Exception ignored) {}

        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AddPiglinBartersModifier>
    {
        @Override
        public AddPiglinBartersModifier read(@Nonnull ResourceLocation location, JsonObject object, ILootCondition[] conditionsIn)
        {
            List<LootEntry> additions = new ArrayList<>();
            for (JsonElement addition : JsonHelper.getAsJsonArray(object, "additions", new JsonArray()))
            {
                JsonObject additionObject = addition.getAsJsonObject();
                Optional<CompoundNBT> tag = additionObject.has("tag") ? Optional.of(CompoundNBT.CODEC.parse(JsonOps.INSTANCE, additionObject.get("tag")).getOrThrow(false, s -> {})) : Optional.empty();
                JsonObject count = JsonHelper.getAsJsonObject(additionObject, "count", new JsonObject());

                additions.add(new LootEntry(
                        ForgeRegistries.ITEMS.getValue(new ResourceLocation(JsonHelper.getAsString(additionObject, "item"))), tag,
                        // read "min" and "max" from the pair called "count"
                        new IntegerBounds(JsonHelper.getAsInt(count, "min", 1), JsonHelper.getAsInt(count, "max", 1)),
                        JsonHelper.getAsInt(additionObject, "weight", 1)
                ));
            }
            return new AddPiglinBartersModifier(conditionsIn, additions);
        }

        @Override
        public JsonObject write(AddPiglinBartersModifier instance)
        {
            JsonObject object = new JsonObject();
            JsonArray additions = new JsonArray();
            for (LootEntry addition : instance.additions)
            {
                JsonObject additionObject = new JsonObject();
                additionObject.addProperty("item", ForgeRegistries.ITEMS.getKey(addition.item).toString());
                JsonObject count = new JsonObject();
                count.addProperty("min", addition.count.min);
                count.addProperty("max", addition.count.max);
                additionObject.add("count", count);
                additionObject.addProperty("weight", addition.weight);
                addition.tag.ifPresent(tag -> additionObject.add("nbt", CompoundNBT.CODEC.encodeStart(JsonOps.INSTANCE, tag).result().orElseThrow(RuntimeException::new)));
                additions.add(additionObject);
            }
            object.add("additions", additions);
            return object;
        }
    }
}
