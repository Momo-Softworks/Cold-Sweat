package dev.momostudios.coldsweat.data.loot_modifiers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.momostudios.coldsweat.data.codec.LootEntryCodec;
import dev.momostudios.coldsweat.util.math.CSMath;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AddPiglinBartersModifier extends LootModifier
{
    private final List<LootEntryCodec.LootEntry> additions;
    private final boolean replace;

    protected AddPiglinBartersModifier(LootItemCondition[] conditionsIn, List<LootEntryCodec.LootEntry> additions, boolean replace)
    {
        super(conditionsIn);
        this.additions = additions;
        this.replace = replace;
    }

    static ResourceLocation PIGLIN_BARTER_LOCATION = new ResourceLocation("gameplay/piglin_bartering");
    static Field POOLS;
    static Field ENTRIES;
    static
    {
        POOLS = ObfuscationReflectionHelper.findField(LootTable.class, "f_79109_");
        ENTRIES = ObfuscationReflectionHelper.findField(LootPool.class, "f_79023_");
        POOLS.setAccessible(true);
        ENTRIES.setAccessible(true);
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        try
        {
            List<LootPoolEntry> entries = new ArrayList<>();
            MutableInt totalWeight = new MutableInt();
            if (!replace)
            {
                // Build vanilla items
                for (LootPool pool : ((List<LootPool>) POOLS.get(context.getLootTable(PIGLIN_BARTER_LOCATION))))
                {
                    for (LootPoolEntryContainer container : ((LootPoolEntryContainer[]) ENTRIES.get(pool)))
                    {
                        container.expand(context, entry ->
                        {   entries.add(entry);
                            totalWeight.add(entry.getWeight(context.getLuck()));
                        });
                    }
                }
            }

            // Build added items
            for (LootEntryCodec.LootEntry addition : additions)
            {
                Item item = ForgeRegistries.ITEMS.getValue(addition.itemID());
                if (item == null) continue;
                entries.add(new LootPoolEntry()
                {
                    @Override
                    public int getWeight(float luck)
                    {   return CSMath.floor(addition.weight() * (1 + context.getLuck()));
                    }

                    @Override
                    public void createItemStack(Consumer<ItemStack> consumer, LootContext context1)
                    {   consumer.accept(new ItemStack(item, context.getRandom().nextInt(addition.count().getFirst(), addition.count().getSecond() + 1)));
                    }
                });
                totalWeight.add(addition.weight());
            }

            AtomicReference<ItemStack> stack = new AtomicReference<>(ItemStack.EMPTY);
            int j = context.getRandom().nextInt(totalWeight.intValue());

            for(LootPoolEntry lootpoolentry : entries)
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
        public AddPiglinBartersModifier read(@Nonnull ResourceLocation location, JsonObject object, LootItemCondition[] conditionsIn)
        {
            List<LootEntryCodec.LootEntry> additions = new ArrayList<>();
            for (JsonElement addition : GsonHelper.getAsJsonArray(object, "additions", new JsonArray()))
            {
                JsonObject additionObject = addition.getAsJsonObject();
                Optional<CompoundTag> tag = additionObject.has("tag") ? Optional.of(CompoundTag.CODEC.parse(JsonOps.INSTANCE, additionObject.get("tag")).getOrThrow(false, s -> {})) : Optional.empty();
                additions.add(new LootEntryCodec.LootEntry(
                        new ResourceLocation(GsonHelper.getAsString(additionObject, "item")), tag,
                        Pair.of(GsonHelper.getAsInt(additionObject, "min", 1), GsonHelper.getAsInt(additionObject, "max", 1)),
                        GsonHelper.getAsInt(additionObject, "weight", 1)
                ));
            }
            return new AddPiglinBartersModifier(conditionsIn, additions, GsonHelper.getAsBoolean(object, "replace", false));
        }

        @Override
        public JsonObject write(AddPiglinBartersModifier instance)
        {
            JsonObject object = new JsonObject();
            object.addProperty("replace", instance.replace);
            JsonArray additions = new JsonArray();
            for (LootEntryCodec.LootEntry addition : instance.additions)
            {
                JsonObject additionObject = new JsonObject();
                additionObject.addProperty("item", addition.itemID().toString());
                additionObject.addProperty("min", addition.count().getFirst());
                additionObject.addProperty("max", addition.count().getSecond());
                additionObject.addProperty("weight", addition.weight());
                addition.tag().ifPresent(tag -> additionObject.add("nbt", CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, tag).result().orElseThrow(RuntimeException::new)));
                additions.add(additionObject);
            }
            object.add("additions", additions);
            return object;
        }
    }
}
