package dev.momostudios.coldsweat.util.entity;

import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import net.minecraft.nbt.*;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;

public class NBTHelper
{
    public static Object getObjectFromTag(Tag inbt)
    {
        if (inbt instanceof StringTag stringnbt)
        {
            return stringnbt.getAsString();
        }
        else if (inbt instanceof IntTag intnbt)
        {
            return intnbt.getAsInt();
        }
        else if (inbt instanceof FloatTag floatnbt)
        {
            return floatnbt.getAsFloat();
        }
        else if (inbt instanceof DoubleTag dbnbt)
        {
            return dbnbt.getAsDouble();
        }
        else if (inbt instanceof ShortTag shortnbt)
        {
            return shortnbt.getAsShort();
        }
        else if (inbt instanceof LongTag longnbt)
        {
            return longnbt.getAsLong();
        }
        else if (inbt instanceof IntArrayTag int2nbt)
        {
            return int2nbt.getAsIntArray();
        }
        else if (inbt instanceof LongArrayTag long2nbt)
        {
            return long2nbt.getAsLongArray();
        }
        else if (inbt instanceof ByteArrayTag byte2nbt)
        {
            return byte2nbt.getAsByteArray();
        }
        else if (inbt instanceof ByteTag)
        {
            return ((ByteTag) inbt).getAsByte() != 0;
        }
        else throw new UnsupportedOperationException("Unsupported Tag type: " + inbt.getClass().getName());
    }

    public static Tag getTagFromObject(Object object)
    {
        if (object instanceof String str)
        {
            return StringTag.valueOf(str);
        }
        else if (object instanceof Integer intr)
        {
            return IntTag.valueOf(intr);
        }
        else if (object instanceof Float flt)
        {
            return FloatTag.valueOf(flt);
        }
        else if (object instanceof Double dbl)
        {
            return DoubleTag.valueOf(dbl);
        }
        else if (object instanceof Short shrt)
        {
            return ShortTag.valueOf(shrt);
        }
        else if (object instanceof Long lng)
        {
            return LongTag.valueOf(lng);
        }
        else if (object instanceof int[] intarr)
        {
            return new IntArrayTag(intarr);
        }
        else if (object instanceof long[] lngarr)
        {
            return new LongArrayTag(lngarr);
        }
        else if (object instanceof byte[] bytearr)
        {
            return new ByteArrayTag(bytearr);
        }
        else if (object instanceof Boolean bool)
        {
            return ByteTag.valueOf(bool ? (byte) 1 : (byte) 0);
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported object type: " + object.getClass().getName());
        }
    }

    public static CompoundTag modifierToTag(TempModifier modifier)
    {
        // Write the modifier's data to a CompoundTag
        CompoundTag modifierTag = new CompoundTag();
        modifierTag.putString("id", modifier.getID());

        // Add the modifier's arguments
        modifier.getArguments().forEach((name, value) ->
        {
            modifierTag.put(name, getTagFromObject(value));
        });

        // Read the modifier's expiration time
        if (modifier.getExpireTime() != -1)
            modifierTag.putInt("expireTicks", modifier.getExpireTime());

        // Read the modifier's ticks left
        if (modifier.getTicksExisted() > 0)
            modifierTag.putInt("ticksLeft", modifier.getTicksExisted());

        if (modifier.getTickRate() > 1)
            modifierTag.putInt("tickRate", modifier.getTickRate());

        return modifierTag;
    }

    public static TempModifier TagToModifier(CompoundTag modifierTag)
    {
        // Create a new modifier from the CompoundTag
        TempModifier newModifier = TempModifierRegistry.getEntryFor(modifierTag.getString("id"));

        modifierTag.getAllKeys().forEach(key ->
        {
            // Add the modifier's arguments
            if (newModifier != null && key != null)
            {
                newModifier.addArgument(key, getObjectFromTag(modifierTag.get(key)));
            }
        });

        // Set the modifier's expiration time
        if (modifierTag.contains("expireTicks"))
            newModifier.expires(modifierTag.getInt("expireTicks"));

        // Set the modifier's ticks left
        if (modifierTag.contains("ticksLeft"))
            newModifier.setTicksExisted(modifierTag.getInt("ticksLeft"));

        if (modifierTag.contains("tickRate"))
            newModifier.tickRate(modifierTag.getInt("tickRate"));

        return newModifier;
    }
}
