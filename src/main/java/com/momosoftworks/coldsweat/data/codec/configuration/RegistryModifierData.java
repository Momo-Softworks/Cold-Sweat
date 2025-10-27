package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RegistryModifierData<T extends ConfigData> extends ConfigData
{
    private final ResourceKey<Registry<T>> registry;
    private final List<ConfigData.Type> registryTypes;
    private final NegatableList<NbtRequirement> matches;
    private final List<ResourceLocation> entries;
    private final List<Operation> operations;

    public RegistryModifierData(ResourceKey<Registry<T>> registry, List<ConfigData.Type> registryTypes, NegatableList<NbtRequirement> matches, List<ResourceLocation> entries, List<Operation> operations)
    {
        super(new NegatableList<>());
        this.registry = registry;
        this.registryTypes = registryTypes;
        this.matches = matches;
        this.entries = entries;
        this.operations = operations;
    }

    private static final Codec<List<ConfigData.Type>> CONFIG_TYPE_CODEC = Codec.either(ConfigData.Type.CODEC, ConfigData.Type.CODEC.listOf())
                                                                          .xmap(either -> either.map(List::of, r -> r), Either::right);

    public static final Codec<RegistryModifierData<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.xmap(s -> (ResourceKey)ModRegistries.getRegistryKey(s), key -> key.location()).fieldOf("registry").forGetter(data -> data.registry()),
            CONFIG_TYPE_CODEC.optionalFieldOf("config_type", List.of()).forGetter(RegistryModifierData::configTypes),
            NegatableList.listCodec(NbtRequirement.CODEC).optionalFieldOf("matches", new NegatableList<>()).forGetter(RegistryModifierData::matches),
            ResourceLocation.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(RegistryModifierData::entries),
            Operation.CODEC.listOf().optionalFieldOf("operations", List.of()).forGetter(data -> data.operations)
    ).apply(instance, RegistryModifierData::new));

    public ResourceKey<Registry<T>> registry()
    {   return registry;
    }
    public List<ConfigData.Type> configTypes()
    {   return registryTypes;
    }
    public NegatableList<NbtRequirement> matches()
    {   return matches;
    }
    public List<ResourceLocation> entries()
    {   return entries;
    }
    public List<Operation> modifications()
    {   return operations;
    }

    private boolean checkType(T object)
    {
        return this.registryTypes.isEmpty()
            || this.registryTypes.contains(object.configType());
    }

    public boolean matches(T object)
    {
        if (!checkType(object))
        {   return false;
        }
        if (matches.isEmpty()) return false;
        Optional<Tag> serializedOpt = object.getCodec().encodeStart(NbtOps.INSTANCE, object).result();
        return serializedOpt.map(serialized ->
        {   return matches.test(nbt -> nbt.test((CompoundTag) serialized));
        }).orElse(false);
    }

    public boolean matches(Holder<T> holder)
    {
        if (!checkType(holder.value()))
        {   return false;
        }
        // Check if object ID is in the entries list
        ResourceLocation key = holder.unwrapKey().map(ResourceKey::location).orElse(null);
        if (key != null && entries.contains(key))
        {   return true;
        }
        return this.matches(holder.value());
    }

    @Nullable
    public T applyModifications(T object)
    {
        T modifiedObject = object;
        for (Operation operation : operations)
        {
            T result = operation.modify(modifiedObject);
            if (result == null)
            {   return null;
            }
            modifiedObject = result;
        }
        return modifiedObject;
    }

    @Override
    public Codec<? extends ConfigData> getCodec()
    {   return CODEC;
    }

    public enum Type implements StringRepresentable
    {
        DISABLE("disable"),
        REPLACE("replace"),
        MERGE("merge"),
        APPEND("append"),
        REMOVE("remove");

        public static final Codec<Type> CODEC = ExtraCodecs.enumIgnoreCase(values());

        private final String name;

        Type(String name)
        {   this.name = name;
        }

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static Type byName(String name)
        {   return EnumHelper.byName(values(), name);
        }
    }

    public record Operation(Type type, CompoundTag data)
    {
        public static final Codec<Operation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Type.CODEC.fieldOf("type").forGetter(Operation::type),
                CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(Operation::data)
        ).apply(instance, Operation::new));

        @Nullable
        public <T extends ConfigData> T modify(T element)
        {
            Codec<T> codec = element.getCodec();
            CompoundTag elementTag = (CompoundTag) codec.encodeStart(NbtOps.INSTANCE, element).result().orElse(new CompoundTag());
            switch (type)
            {
                case DISABLE ->
                {   return null;
                }
                case REPLACE ->
                {
                    for (String key : this.data.getAllKeys())
                    {
                        if (elementTag.contains(key))
                        {   elementTag.put(key, data.get(key));
                        }
                    }
                }
                case MERGE ->
                {   elementTag = mergeCompounds(elementTag, data);
                }
                case APPEND ->
                {   elementTag = appendCompound(elementTag, data);
                }
                case REMOVE ->
                {   elementTag = removeCompound(elementTag, data);
                }
            }
            CompoundTag resultTag = elementTag;
            Optional<T> result = codec.parse(NbtOps.INSTANCE, resultTag).result();
            return result.orElseGet(() ->
                   {   ColdSweat.LOGGER.error("Failed to apply registry modification of type {} with data {} to data {}", type, this.data, resultTag);
                       return element;
                   });
        }

        /**
         * Merges two CompoundTags together, with toMerge overwriting original's values where keys overlap.<br>
         * Also merges ListTags by combining their unique elements.
         */
        private static CompoundTag mergeCompounds(CompoundTag original, CompoundTag toMerge)
        {
            CompoundTag merged = original.copy();
            for (String key : toMerge.getAllKeys())
            {
                Tag originalValue = merged.get(key);
                Tag toMergeValue = toMerge.get(key);
                if (originalValue != null)
                {
                    if (originalValue instanceof CompoundTag originalCompound && toMergeValue instanceof CompoundTag toMergeCompound)
                    {   merged.put(key, mergeCompounds(originalCompound, toMergeCompound));
                    }
                    else if (originalValue instanceof ListTag originalList && toMergeValue instanceof ListTag toMergeList)
                    {   merged.put(key, mergeLists(originalList, toMergeList));
                    }
                    else if (originalValue instanceof NumericTag originalNumber && toMergeValue instanceof StringTag operation && operation.getAsString().length() > 2)
                    {
                        double operand = Double.parseDouble(operation.getAsString().substring(2));
                        double numberValue = originalNumber.getAsDouble();
                        switch (operation.getAsString().substring(0, 2))
                        {
                            case "+=" -> numberValue += operand;
                            case "-=" -> numberValue -= operand;
                            case "*=" -> numberValue *= operand;
                            case "/=" -> numberValue /= operand;
                            case "^=" -> numberValue = Math.pow(numberValue, operand);
                            case "%=" -> numberValue %= operand;
                            default  -> merged.put(key, toMergeValue);
                        }
                        if (originalNumber instanceof ByteTag)
                        {   merged.putByte(key, (byte) numberValue);
                        }
                        else if (originalNumber instanceof ShortTag)
                        {   merged.putShort(key, (short) numberValue);
                        }
                        else if (originalNumber instanceof IntTag)
                        {   merged.putInt(key, (int) numberValue);
                        }
                        else if (originalNumber instanceof LongTag)
                        {   merged.putLong(key, (long) numberValue);
                        }
                        else if (originalNumber instanceof FloatTag)
                        {   merged.putFloat(key, (float) numberValue);
                        }
                        else if (originalNumber instanceof DoubleTag)
                        {   merged.putDouble(key, numberValue);
                        }
                    }
                }
                else
                {   merged.put(key, toMergeValue);
                }
            }
            return merged;
        }

        private static ListTag mergeLists(ListTag original, ListTag toMerge)
        {
            Set<Tag> merged = new HashSet<>(original.copy());
            merged.addAll(toMerge);
            return new ListTag()
            {{  this.addAll(merged);
            }};
        }

        /**
         * Appends only new elements to the existing tag, leaving existing values unchanged.<br>
         * This operation is "deep", meaning that nested CompoundTags are also appended.<br>
         * ListTags are unaffected.
         */
        private static CompoundTag appendCompound(CompoundTag original, CompoundTag toAppend)
        {
            CompoundTag appended = original.copy();
            for (String key : toAppend.getAllKeys())
            {
                Tag originalValue = appended.get(key);
                Tag toAppendValue = toAppend.get(key);
                if (originalValue == null)
                {   appended.put(key, toAppendValue);
                }
                else if (originalValue instanceof CompoundTag originalCompound && toAppendValue instanceof CompoundTag toAppendCompound)
                {   appended.put(key, appendCompound(originalCompound, toAppendCompound));
                }
            }
            return appended;
        }

        private static CompoundTag removeCompound(CompoundTag original, CompoundTag toRemove)
        {
            CompoundTag modified = original.copy();
            for (String key : toRemove.getAllKeys())
            {
                Tag originalValue = modified.get(key);
                Tag toRemoveValue = toRemove.get(key);
                if (originalValue != null)
                {
                    // Inner compound
                    if (toRemoveValue instanceof CompoundTag toRemoveCompound)
                    {
                        if (originalValue instanceof CompoundTag originalCompound)
                        {   modified.put(key, removeCompound(originalCompound, toRemoveCompound));
                        }
                    }
                    // Remove key
                    else if (toRemoveValue instanceof StringTag)
                    {   modified.remove(key);
                    }
                    // Remove elements from list
                    else if (toRemoveValue instanceof ListTag toRemoveList && originalValue instanceof ListTag originalList)
                    {
                        ListTag newList = originalList.copy();
                        newList.removeAll(toRemoveList);
                        modified.put(key, newList);
                    }
                }
            }
            return modified;
        }
    }
}
