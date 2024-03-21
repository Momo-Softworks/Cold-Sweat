package com.momosoftworks.coldsweat.data.loot.modifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.LootEntry;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddDropsModifier extends LootModifier
{
    private final List<LootEntry> additions;

    protected AddDropsModifier(LootItemCondition[] conditionsIn, List<LootEntry> additions)
    {
        super(conditionsIn);
        this.additions = additions;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        for (LootEntry entry : additions)
        {
            int countRange = entry.count().max() - entry.count().min();
            generatedLoot.add(new ItemStack(entry.item(),
                                            context.getRandom().nextInt(entry.count().min(), entry.count().max() + 1)
                                          + context.getRandom().nextInt(0, countRange * context.getLootingModifier() + 1)));
        }
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AddDropsModifier>
    {
        @Override
        public AddDropsModifier read(@Nonnull ResourceLocation location, JsonObject object, LootItemCondition[] conditionsIn)
        {
            List<LootEntry> additions = new ArrayList<>();
            for (JsonElement element : GsonHelper.getAsJsonArray(object, "additions"))
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
