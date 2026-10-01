package com.momosoftworks.coldsweat.util.serialization;

import java.util.stream.Stream;
import net.minecraft.nbt.NbtOps;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@EventBusSubscriber
public class NBTHelper
{
    /**
     * Encodes a CompoundTag's entries directly as fields of the surrounding map.<br>
     * Used to write a CompoundTag into a {@link net.minecraft.world.level.storage.ValueOutput} without nesting it.
     */
    public static final MapCodec<CompoundTag> COMPOUND_MAP_CODEC = new MapCodec<>()
    {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops)
        {   return Stream.empty();
        }

        @Override
        public <T> DataResult<CompoundTag> decode(DynamicOps<T> ops, MapLike<T> input)
        {
            CompoundTag tag = new CompoundTag();
            input.entries().forEach(pair ->
            {
                ops.getStringValue(pair.getFirst()).result().ifPresent(key ->
                        tag.put(key, ops.convertTo(NbtOps.INSTANCE, pair.getSecond())));
            });
            return DataResult.success(tag);
        }

        @Override
        public <T> RecordBuilder<T> encode(CompoundTag input, DynamicOps<T> ops, RecordBuilder<T> prefix)
        {
            for (String key : input.keySet())
            {   prefix.add(key, NbtOps.INSTANCE.convertTo(ops, input.get(key)));
            }
            return prefix;
        }
    };

    private NBTHelper() {}

    /**
     * DEPRECATED. use {@link TempModifier#CODEC} instead.
     */
    @Deprecated(since = "2.4.1", forRemoval = true)
    public static CompoundTag modifierToTag(TempModifier modifier)
    {
        // Write the modifier's data to a CompoundTag
        CompoundTag modifierTag = new CompoundTag();
        Identifier modifierId = modifier.getID();
        if (modifierId == null)
        {
            ColdSweat.LOGGER.error("Failed to get key for TempModifier: {}", modifier.getClass().getSimpleName());
            return modifierTag;
        }
        modifierTag.putString("Id", modifierId.toString());

        // Add the modifier's arguments
        modifierTag.put("ModifierData", modifier.getNBT());

        // Read the modifier's expiration time
        if (modifier.getExpireTime() != -1)
            modifierTag.putInt("ExpireTicks", modifier.getExpireTime());

        // Read the modifier's tick rate
        if (modifier.getTickRate() > 1)
            modifierTag.putInt("TickRate", modifier.getTickRate());

        // Read the modifier's ticks existed
        modifierTag.putInt("TicksExisted", modifier.getTicksExisted());

        modifierTag.putInt("Hash", modifier.hashCode());

        return modifierTag;
    }

    /**
     * DEPRECATED. use {@link TempModifier#CODEC} instead.
     */
    @Deprecated(since = "2.4.1", forRemoval = true)
    public static Optional<TempModifier> tagToModifier(CompoundTag modifierTag)
    {
        // Create a new modifier from the CompoundTag
        Optional<TempModifier> optional = TempModifierRegistry.getValue(Identifier.parse(modifierTag.getStringOr("Id", "")));
        optional.ifPresent(modifier ->
        {
            modifier.setNBT(modifierTag.getCompoundOrEmpty("ModifierData"));

            // Set the modifier's expiration time
            if (modifierTag.contains("ExpireTicks"))
            {   modifier.expires(modifierTag.getIntOr("ExpireTicks", 0));
            }

            // Set the modifier's tick rate
            if (modifierTag.contains("TickRate"))
            {   modifier.tickRate(modifierTag.getIntOr("TickRate", 0));
            }

            // Set the modifier's ticks existed
            modifier.setTicksExisted(modifierTag.getIntOr("TicksExisted", 0));
        });

        return optional;
    }

    public static void incrementTag(CompoundTag tag, String key, int amount, Predicate<Integer> predicate)
    {
        int value = tag.getIntOr(key, 0);
        if (predicate.test(value))
        {   tag.putInt(key, value + amount);
        }
    }

    public static void incrementTag(Object owner, String key, int amount)
    {   incrementTag(owner, key, amount, (tag) -> true);
    }

    public static int incrementTag(Object owner, String key, int amount, Predicate<Integer> predicate)
    {
        CompoundTag tag;
        switch (owner)
        {
            case Entity entity -> tag = entity.getPersistentData();
            case ItemStack stack -> tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
            case BlockEntity blockEntity -> tag = blockEntity.getPersistentData();
            case TempModifier modifier -> tag = modifier.getNBT();
            case null, default ->
            {   return 0;
            }
        }

        int value = tag.getIntOr(key, 0);
        if (predicate.test(value))
        {
            tag.putInt(key, value + amount);
            if (owner instanceof ItemStack stack)
            {   stack.set(DataComponents.CUSTOM_DATA, stack.get(DataComponents.CUSTOM_DATA).update(tg -> tg.putInt(key, value + amount)));
            }
        }
        return value + amount;
    }

    /**
     * Gets an item's tag, without instantiating one on the item if it isn't present.<br>
     * An empty {@link CompoundTag} will be returned in that case, so a null check will not be necessary.<br>
     * <br>
     * Use {@link NBTHelper#getOrCreateTag(ItemStack)} if you need to write to the tag.<br>
     * @return The item's tag, or an empty tag if it is not present
     */
    public static CompoundTag getTagOrEmpty(ItemStack stack)
    {   return stack.has(DataComponents.CUSTOM_DATA) ? stack.get(DataComponents.CUSTOM_DATA).copyTag() : new CompoundTag();
    }

    public static CustomData getOrCreateTag(ItemStack stack)
    {
        if (!stack.has(DataComponents.CUSTOM_DATA))
        {   stack.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
        }
        return stack.get(DataComponents.CUSTOM_DATA);
    }

    public static void ensureTagAndDo(ItemStack stack, Consumer<CompoundTag> action)
    {   CustomData nbt = getOrCreateTag(stack);
        stack.set(DataComponents.CUSTOM_DATA, nbt.update(action));
    }

    public static <T extends Tag> T getOrPutTag(LivingEntity entity, String tag, T dfault)
    {
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(tag) || data.get(tag).getId() != dfault.getId())
        {   data.put(tag, dfault);
        }
        return (T) data.get(tag);
    }

    /**
     * Used for storing Temperature values in the player's persistent data (NBT). <br>
     * <br>
     * @param trait The type of Temperature to be stored.
     * @return The NBT tag name for the given trait.
     */
    public static String getTraitTagKey(Temperature.Trait trait)
    {   return trait.getSerializedName();
    }

    @SubscribeEvent
    public static void convertTagsInContainer(PlayerContainerEvent.Open event)
    {   updateItemTags(event.getContainer().getItems());
    }

    private static void updateItemTags(Collection<ItemStack> items)
    {
        for (ItemStack stack : items)
        {
            Item item = stack.getItem();
            if (!stack.has(DataComponents.CUSTOM_DATA)) return;
            CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();

            // Convert old tags on existing items
            if (item == ModItems.SOULSPRING_LAMP.value())
            {
                IntTag oldTemp = ((IntTag) CSMath.orElse(tag.get("fuel"), tag.get("Fuel")));
                if (oldTemp != null)
                {
                    stack.set(ModItemComponents.WATER_TEMPERATURE, oldTemp.doubleValue());
                    stack.get(DataComponents.CUSTOM_DATA).update(tg ->
                    {   tg.remove("fuel");
                        tg.remove("Fuel");
                    });
                }
            }
            else if (item == ModItems.FILLED_WATERSKIN.value())
            {
                DoubleTag oldTemp = ((DoubleTag) CSMath.orElse(tag.get("temperature"), tag.get("Temperature")));
                if (oldTemp != null)
                {
                    stack.set(ModItemComponents.WATER_TEMPERATURE, oldTemp.doubleValue());
                    stack.get(DataComponents.CUSTOM_DATA).update(tg ->
                    {   tg.remove("temperature");
                        tg.remove("Temperature");
                    });
                }
            }
        }
    }

    public static CompoundTag parseCompoundNbt(String tag)
    {
        if (tag.isBlank()) return new CompoundTag();
        try
        {   return TagParser.parseCompoundFully(tag);
        }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Error parsing compound tag \"{}\": {}", tag, e.getMessage());
            e.printStackTrace();
            return new CompoundTag();
        }
    }

    public static ListTag listTagOf(List<?> list)
    {
        ListTag tag = new ListTag();
        for (Object obj : list)
        {   if (obj instanceof Tag tg)
            {   tag.add(tg);
            }
            else tag.add(serialize(obj));
        }
        return tag;
    }

    @Nullable
    public static Object deserialize(Tag tag)
    {
        return switch (tag)
        {
            case IntTag integer -> integer.intValue();
            case FloatTag floating -> floating.floatValue();
            case DoubleTag doubleTag -> doubleTag.doubleValue();
            case LongTag longTag -> longTag.longValue();
            case ShortTag shortTag -> shortTag.shortValue();
            case ByteTag byteTag -> byteTag.byteValue();
            case ByteArrayTag byteArray -> byteArray.toString();
            case IntArrayTag intArray -> intArray.getAsIntArray();
            case LongArrayTag longArray -> longArray.getAsLongArray();
            case StringTag string -> string.value();
            case CompoundTag compound ->
            {
                // Attempt to read an enum from the compound tag
                if (compound.contains("value") && compound.contains("class"))
                {   yield tryReadEnum(compound);
                }
                yield null;
            }
            case ListTag list -> list.stream().map(NBTHelper::deserialize).toList();
            default -> null;
        };
    }

    @Nullable
    private static <T extends Enum<T>> Enum<T> tryReadEnum(CompoundTag tag)
    {
        try
        {
            Class<?> clazz = Class.forName(tag.getStringOr("class", ""));
            return Enum.valueOf((Class<T>) clazz, tag.getStringOr("value", ""));
        }
        catch (ClassNotFoundException e)
        {   ColdSweat.LOGGER.error("Failed to read enum from compound tag: {}", e.getMessage());
        }
        return null;
    }

    @Nullable
    public static Tag serialize(Object obj)
    {
        return switch (obj)
        {
            case Integer integer -> IntTag.valueOf(integer);
            case Float floating ->  FloatTag.valueOf(floating);
            case Double doubleTag -> DoubleTag.valueOf(doubleTag);
            case Long longTag -> LongTag.valueOf(longTag);
            case Short shortTag -> ShortTag.valueOf(shortTag);
            case Byte byteTag -> ByteTag.valueOf(byteTag);
            case String string -> StringTag.valueOf(string);
            case List<?> list ->
            {
                ListTag tag = new ListTag();
                for (Object item : list)
                {
                    Tag itemTag = serialize(item);
                    if (itemTag != null) tag.add(itemTag);
                }
                yield tag;
            }
            case Enum<?> enm ->
            {
                CompoundTag tag = new CompoundTag();
                tag.putString("value", enm.name());
                tag.putString("class", enm.getClass().getName());
                yield tag;
            }
            default -> null;
        };
    }
}
