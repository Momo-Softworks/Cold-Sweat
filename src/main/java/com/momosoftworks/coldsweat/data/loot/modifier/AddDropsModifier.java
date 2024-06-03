package com.momosoftworks.coldsweat.data.loot.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.LootEntry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddDropsModifier extends LootModifier
{
    public static Codec<AddDropsModifier> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(modifier -> modifier.conditions),
            LootEntry.CODEC.listOf().fieldOf("additions").forGetter(modifier -> modifier.additions),
            ForgeRegistries.ITEMS.getCodec().listOf().optionalFieldOf("removals", List.of()).forGetter(modifier -> modifier.removals)
    ).apply(inst, AddDropsModifier::new));

    private final List<LootEntry> additions;
    private final List<Item> removals;

    protected AddDropsModifier(LootItemCondition[] conditionsIn, List<LootEntry> additions, List<Item> removals)
    {
        super(conditionsIn);
        this.additions = additions;
        this.removals = removals;
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
    {
        for (LootEntry entry : additions)
        {
            int countRange = entry.count().max() - entry.count().min();
            generatedLoot.add(new ItemStack(entry.item(),
                                            context.getRandom().nextIntBetweenInclusive(entry.count().min(), entry.count().max())
                                          + context.getRandom().nextIntBetweenInclusive(0, countRange * context.getLootingModifier())));
        }
        if (!removals.isEmpty())
        {   generatedLoot.removeIf(stack -> removals.contains(stack.getItem()));
        }
        return generatedLoot;
    }

    @Override
    public Codec<AddDropsModifier> codec()
    {   return CODEC;
    }
}
