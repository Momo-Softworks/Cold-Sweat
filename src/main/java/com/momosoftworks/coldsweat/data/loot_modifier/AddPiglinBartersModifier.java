package com.momosoftworks.coldsweat.data.loot_modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.LootEntryCodec;
import com.momosoftworks.coldsweat.util.math.CSMath;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class AddPiglinBartersModifier extends LootModifier
{
    public static Codec<AddPiglinBartersModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .and(new LootEntryCodec(ResourceLocation.CODEC.fieldOf("item").codec(),
                                    Codec.pair(Codec.INT.fieldOf("min").codec(), Codec.INT.fieldOf("max").codec()).fieldOf("count").codec(),
                                    Codec.INT.fieldOf("weight").codec(),
                                    CompoundTag.CODEC.optionalFieldOf("tag").codec()).listOf().fieldOf("additions").forGetter(o -> o.additions))
            .and(Codec.BOOL.fieldOf("replace").forGetter(o -> o.replace))
            .apply(inst, AddPiglinBartersModifier::new));
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
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
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

    @Override
    public Codec<AddPiglinBartersModifier> codec() {
        return CODEC;
    }
}
