package dev.momostudios.coldsweat.util;

import dev.momostudios.coldsweat.common.world.TempModifierEntries;
import net.minecraft.nbt.*;
import dev.momostudios.coldsweat.common.temperature.modifier.TempModifier;

public class NBTHelper
{
    public static Object getObjectFromTag(Tag inbt)
    {
        if (inbt instanceof StringTag)
        {
            return ((StringTag) inbt).getAsString();
        }
        else if (inbt instanceof IntTag)
        {
            return ((IntTag) inbt).getId();
        }
        else if (inbt instanceof FloatTag)
        {
            return ((FloatTag) inbt).getAsFloat();
        }
        else if (inbt instanceof DoubleTag)
        {
            return ((DoubleTag) inbt).getAsDouble();
        }
        else if (inbt instanceof ShortTag)
        {
            return ((ShortTag) inbt).getAsShort();
        }
        else if (inbt instanceof LongTag)
        {
            return ((LongTag) inbt).getAsLong();
        }
        else if (inbt instanceof IntArrayTag)
        {
            return ((IntArrayTag) inbt).getAsIntArray();
        }
        else if (inbt instanceof LongArrayTag)
        {
            return ((LongArrayTag) inbt).getAsLongArray();
        }
        else if (inbt instanceof ByteArrayTag)
        {
            return ((ByteArrayTag) inbt).getAsByteArray();
        }
        else if (inbt instanceof ByteTag)
        {
            return ((ByteTag) inbt).getAsByte() != 0;
        }
        else throw new UnsupportedOperationException("Unsupported Tag type: " + inbt.getClass().getName());
    }

    public static Tag getTagFromObject(Object object)
    {
        if (object instanceof String)
        {
            return StringTag.valueOf((String) object);
        }
        else if (object instanceof Integer)
        {
            return IntTag.valueOf((Integer) object);
        }
        else if (object instanceof Float)
        {
            return FloatTag.valueOf((Float) object);
        }
        else if (object instanceof Double)
        {
            return DoubleTag.valueOf((Double) object);
        }
        else if (object instanceof Byte)
        {
            return ByteTag.valueOf((Byte) object);
        }
        else if (object instanceof Short)
        {
            return ShortTag.valueOf((Short) object);
        }
        else if (object instanceof Long)
        {
            return LongTag.valueOf((Long) object);
        }
        else if (object instanceof int[])
        {
            return new IntArrayTag((int[]) object);
        }
        else if (object instanceof long[])
        {
            return new LongArrayTag((long[]) object);
        }
        else if (object instanceof byte[])
        {
            return new ByteArrayTag((byte[]) object);
        }
        else if (object instanceof Boolean)
        {
            return ByteTag.valueOf((Boolean) object ? (byte) 1 : (byte) 0);
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
        if (modifier.getExpireTicks() != -1)
            modifierTag.putInt("expireTicks", modifier.getExpireTicks());

        // Read the modifier's ticks left
        if (modifier.getTicksExisted() > 0)
            modifierTag.putInt("ticksLeft", modifier.getTicksExisted());

        return modifierTag;
    }

    public static TempModifier TagToModifier(CompoundTag modifierTag)
    {
        // Create a new modifier from the CompoundTag
        TempModifier newModifier = TempModifierEntries.getEntries().getEntryFor(modifierTag.getString("id"));

        modifierTag.getAllKeys().forEach(key ->
        {
            // Add the modifier's arguments
            if (newModifier != null && key != null)
                newModifier.addArgument(key, getObjectFromTag(modifierTag.get(key)));
        });

        // Set the modifier's expiration time
        if (modifierTag.contains("expireTicks"))
            newModifier.expires(modifierTag.getInt("expireTicks"));

        // Set the modifier's ticks left
        if (modifierTag.contains("ticksLeft"))
            newModifier.setTicksExisted(modifierTag.getInt("ticksLeft"));

        return newModifier;
    }
}
