package dev.momostudios.coldsweat.common.loot;

import com.google.gson.JsonObject;
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

public class HoglinDrops extends LootModifier
{
    private final Item addition;

    protected HoglinDrops(LootItemCondition[] conditionsIn, final Item addition)
    {
        super(conditionsIn);
        this.addition = addition;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        generatedLoot.add(new ItemStack(addition, 1 + (int) (context.getRandom().nextDouble() * (context.getLootingModifier() + 1) / 2)));
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<HoglinDrops>
    {
        @Override
        public HoglinDrops read(@Nonnull ResourceLocation location, JsonObject object, LootItemCondition[] conditionsIn)
        {
            return new HoglinDrops(conditionsIn, ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(object, "addition"))));
        }

        @Override
        public JsonObject write(HoglinDrops instance)
        {
            JsonObject json = makeConditions(instance.conditions);
            json.addProperty("addition", instance.addition.getRegistryName().toString());
            return json;
        }
    }
}
