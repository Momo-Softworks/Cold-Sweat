package com.momosoftworks.coldsweat.data.loot_modifiers;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class AddDropsModifier extends LootModifier
{
    public static Codec<AddDropsModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .and(ResourceLocation.CODEC.fieldOf("addition").forGetter(o -> o.addition))
            .and(Codec.pair(Codec.INT.fieldOf("min").codec(), Codec.INT.fieldOf("max").codec()).fieldOf("count").forGetter(o -> o.count))
            .apply(inst, AddDropsModifier::new));
    private final ResourceLocation addition;
    private final Pair<Integer, Integer> count;

    protected AddDropsModifier(LootItemCondition[] conditionsIn, ResourceLocation addition, Pair<Integer, Integer> count)
    {
        super(conditionsIn);
        this.addition = addition;
        this.count = count;
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
    {
        int multiplier = count.getSecond() - count.getFirst() + 1;
        generatedLoot.add(new ItemStack(ForgeRegistries.ITEMS.getValue(addition),
                                        context.getRandom().nextInt(multiplier) + count.getFirst()
                                        + context.getRandom().nextInt(multiplier * context.getLootingModifier() + 1)));
        return generatedLoot;
    }

    @Override
    public Codec<AddDropsModifier> codec() {
        return CODEC;
    }
}
