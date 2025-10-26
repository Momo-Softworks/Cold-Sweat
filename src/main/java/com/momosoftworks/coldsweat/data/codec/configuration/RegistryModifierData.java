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
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import net.minecraft.nbt.*;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RegistryModifierData<T extends ConfigData> extends ConfigData
{
    private final RegistryKey<Registry<T>> registry;
    private final List<ConfigData.Type> registryTypes;
    private final NegatableList<NbtRequirement> matches;
    private final List<ResourceLocation> entries;
    private final List<Operation> operations;

    public RegistryModifierData(RegistryKey<Registry<T>> registry, List<ConfigData.Type> registryTypes, NegatableList<NbtRequirement> matches, List<ResourceLocation> entries, List<Operation> operations)
    {
        super(new NegatableList<>());
        this.registry = registry;
        this.registryTypes = registryTypes;
        this.matches = matches;
        this.entries = entries;
        this.operations = operations;
    }

    private static final Codec<List<ConfigData.Type>> CONFIG_TYPE_CODEC = Codec.either(ConfigData.Type.CODEC, ConfigData.Type.CODEC.listOf())
                                                                          .xmap(either -> either.map(Arrays::asList, r -> r), Either::right);

    public static final Codec<RegistryModifierData<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.xmap(s -> (RegistryKey)ModRegistries.getRegistryKey(s), key -> key.location()).fieldOf("registry").forGetter(data -> data.registry()),
            CONFIG_TYPE_CODEC.optionalFieldOf("config_type", Arrays.asList()).forGetter(RegistryModifierData::configTypes),
            NegatableList.listCodec(NbtRequirement.CODEC).optionalFieldOf("matches", new NegatableList<>()).forGetter(RegistryModifierData::matches),
            ResourceLocation.CODEC.listOf().optionalFieldOf("entries", Arrays.asList()).forGetter(RegistryModifierData::entries),
            Operation.CODEC.listOf().optionalFieldOf("operations", Arrays.asList()).forGetter(data -> data.operations)
    ).apply(instance, RegistryModifierData::new));

    public RegistryKey<Registry<T>> registry()
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
        Optional<INBT> serializedOpt = object.getCodec().encodeStart(NBTDynamicOps.INSTANCE, object).result();
        return serializedOpt.map(serialized ->
        {   return matches.test(nbt -> nbt.test((CompoundNBT) serialized));
        }).orElse(false);
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

    public static class Operation
    {
        private final Type type;
        private final CompoundNBT data;

        public static final Codec<Operation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Type.CODEC.fieldOf("type").forGetter(Operation::type),
                CompoundNBT.CODEC.optionalFieldOf("data", new CompoundNBT()).forGetter(Operation::data)
        ).apply(instance, Operation::new));

        public Operation(Type type, CompoundNBT data)
        {
            this.type = type;
            this.data = data;
        }

        public Type type()
        {   return type;
        }
        public CompoundNBT data()
        {   return data;
        }

        @Nullable
        public <T extends ConfigData> T modify(T element)
        {
            Codec<T> codec = element.getCodec();
            CompoundNBT elementTag = (CompoundNBT) codec.encodeStart(NBTDynamicOps.INSTANCE, element).result().orElse(new CompoundNBT());
            switch (type)
            {
                case DISABLE : return null;
                case REPLACE :
                {
                    for (String key : this.data.getAllKeys())
                    {
                        if (elementTag.contains(key))
                        {   elementTag.put(key, data.get(key));
                        }
                    }
                    break;
                }
                case MERGE :
                {   elementTag = mergeCompounds(elementTag, data);
                    break;
                }
                case APPEND :
                {   elementTag = appendCompound(elementTag, data);
                    break;
                }
                case REMOVE :
                {   elementTag = removeCompound(elementTag, data);
                    break;
                }
            }
            CompoundNBT resultTag = elementTag;
            Optional<T> result = codec.parse(NBTDynamicOps.INSTANCE, resultTag).result();
            return result.orElseGet(() ->
                   {   ColdSweat.LOGGER.error("Failed to apply registry modification of type {} with data {} to data {}", type, this.data, resultTag);
                       return element;
                   });
        }

        /**
         * Merges two CompoundNBTs together, with toMerge overwriting original's values where keys overlap.<br>
         * Also merges ListNBTs by combining their unique elements.
         */
        private static CompoundNBT mergeCompounds(CompoundNBT original, CompoundNBT toMerge)
        {
            CompoundNBT merged = original.copy();
            for (String key : toMerge.getAllKeys())
            {
                INBT originalValue = merged.get(key);
                INBT toMergeValue = toMerge.get(key);
                if (originalValue != null)
                {
                    if (originalValue instanceof CompoundNBT && toMergeValue instanceof CompoundNBT)
                    {   merged.put(key, mergeCompounds((CompoundNBT) originalValue, (CompoundNBT) toMergeValue));
                    }
                    else if (originalValue instanceof ListNBT && toMergeValue instanceof ListNBT)
                    {   merged.put(key, mergeLists((ListNBT) originalValue, (ListNBT) toMergeValue));
                    }
                    else if (originalValue instanceof NumberNBT && toMergeValue instanceof StringNBT && toMergeValue.getAsString().length() > 2)
                    {
                        NumberNBT originalNumber = (NumberNBT) originalValue;
                        StringNBT operation = (StringNBT) toMergeValue;
                        double operand = Double.parseDouble(operation.getAsString().substring(2));
                        double numberValue = originalNumber.getAsDouble();
                        switch (operation.getAsString().substring(0, 2))
                        {
                            case "+=" : numberValue += operand; break;
                            case "-=" : numberValue -= operand; break;
                            case "*=" : numberValue *= operand; break;
                            case "/=" : numberValue /= operand; break;
                            case "%=" : numberValue %= operand; break;
                            default   : merged.put(key, toMergeValue);
                        }
                        if (originalNumber instanceof ByteNBT)
                        {   merged.putByte(key, (byte) numberValue);
                        }
                        else if (originalNumber instanceof ShortNBT)
                        {   merged.putShort(key, (short) numberValue);
                        }
                        else if (originalNumber instanceof IntNBT)
                        {   merged.putInt(key, (int) numberValue);
                        }
                        else if (originalNumber instanceof LongNBT)
                        {   merged.putLong(key, (long) numberValue);
                        }
                        else if (originalNumber instanceof FloatNBT)
                        {   merged.putFloat(key, (float) numberValue);
                        }
                        else if (originalNumber instanceof DoubleNBT)
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

        private static ListNBT mergeLists(ListNBT original, ListNBT toMerge)
        {
            Set<INBT> merged = new HashSet<>(original.copy());
            merged.addAll(toMerge);
            return new ListNBT()
            {{  this.addAll(merged);
            }};
        }

        /**
         * Appends only new elements to the existing tag, leaving existing values unchanged.<br>
         * This operation is "deep", meaning that nested CompoundNBTs are also appended.<br>
         * ListNBTs are unaffected.
         */
        private static CompoundNBT appendCompound(CompoundNBT original, CompoundNBT toAppend)
        {
            CompoundNBT appended = original.copy();
            for (String key : toAppend.getAllKeys())
            {
                INBT originalValue = appended.get(key);
                INBT toAppendValue = toAppend.get(key);
                if (originalValue == null)
                {   appended.put(key, toAppendValue);
                }
                else if (originalValue instanceof CompoundNBT && toAppendValue instanceof CompoundNBT)
                {   appended.put(key, appendCompound(((CompoundNBT) originalValue), ((CompoundNBT) toAppendValue)));
                }
            }
            return appended;
        }

        private static CompoundNBT removeCompound(CompoundNBT original, CompoundNBT toRemove)
        {
            CompoundNBT modified = original.copy();
            for (String key : toRemove.getAllKeys())
            {
                INBT originalValue = modified.get(key);
                INBT toRemoveValue = toRemove.get(key);
                if (originalValue != null)
                {
                    // Inner compound
                    if (toRemoveValue instanceof CompoundNBT)
                    {
                        if (originalValue instanceof CompoundNBT)
                        {   modified.put(key, removeCompound(((CompoundNBT) originalValue), ((CompoundNBT) toRemoveValue)));
                        }
                    }
                    // Remove key
                    else if (toRemoveValue instanceof StringNBT)
                    {   modified.remove(key);
                    }
                    // Remove elements from list
                    else if (toRemoveValue instanceof ListNBT && originalValue instanceof ListNBT)
                    {
                        ListNBT newList = ((ListNBT) originalValue).copy();
                        newList.removeAll(((ListNBT) toRemoveValue));
                        modified.put(key, newList);
                    }
                }
            }
            return modified;
        }
    }
}
