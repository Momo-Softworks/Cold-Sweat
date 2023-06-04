package dev.momostudios.coldsweat.common.loot;

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

public class HoglinDrops extends LootModifier
{
    public static Codec<HoglinDrops> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).and(ResourceLocation.CODEC.fieldOf("addition").forGetter(o -> o.addition))
            .apply(inst, HoglinDrops::new));
    private final ResourceLocation addition;

    protected HoglinDrops(LootItemCondition[] conditionsIn, final ResourceLocation addition)
    {
        super(conditionsIn);
        this.addition = addition;
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
    {
        generatedLoot.add(new ItemStack(ForgeRegistries.ITEMS.getValue(addition), 1 + (int) (context.getRandom().nextDouble() * (context.getLootingModifier() + 1) / 2)));
        return generatedLoot;
    }

    @Override
    public Codec<HoglinDrops> codec() {
        return CODEC;
    }
}
