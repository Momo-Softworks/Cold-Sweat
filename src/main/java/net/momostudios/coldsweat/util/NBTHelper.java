package net.momostudios.coldsweat.util;

import net.minecraft.nbt.*;

public class NBTHelper
{
    public static Object getObjectFromINBT(INBT inbt)
    {
        if (inbt instanceof StringNBT)
        {
            return ((StringNBT) inbt).getString();
        }
        else if (inbt instanceof IntNBT)
        {
            return ((IntNBT) inbt).getInt();
        }
        else if (inbt instanceof FloatNBT)
        {
            return ((FloatNBT) inbt).getFloat();
        }
        else if (inbt instanceof DoubleNBT)
        {
            return ((DoubleNBT) inbt).getDouble();
        }
        else if (inbt instanceof ByteNBT)
        {
            return ((ByteNBT) inbt).getByte();
        }
        else if (inbt instanceof ShortNBT)
        {
            return ((ShortNBT) inbt).getShort();
        }
        else if (inbt instanceof LongNBT)
        {
            return ((LongNBT) inbt).getLong();
        }
        else if (inbt instanceof IntArrayNBT)
        {
            return ((IntArrayNBT) inbt).getIntArray();
        }
        else if (inbt instanceof LongArrayNBT)
        {
            return ((LongArrayNBT) inbt).getAsLongArray();
        }
        else if (inbt instanceof ByteArrayNBT)
        {
            return ((ByteArrayNBT) inbt).getByteArray();
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported NBT type: " + inbt.getClass().getName());
        }
    }

    public static INBT getINBTFromObject(Object object)
    {
        if (object instanceof String)
        {
            return StringNBT.valueOf((String) object);
        }
        else if (object instanceof Integer)
        {
            return IntNBT.valueOf((Integer) object);
        }
        else if (object instanceof Float)
        {
            return FloatNBT.valueOf((Float) object);
        }
        else if (object instanceof Double)
        {
            return DoubleNBT.valueOf((Double) object);
        }
        else if (object instanceof Byte)
        {
            return ByteNBT.valueOf((Byte) object);
        }
        else if (object instanceof Short)
        {
            return ShortNBT.valueOf((Short) object);
        }
        else if (object instanceof Long)
        {
            return LongNBT.valueOf((Long) object);
        }
        else if (object instanceof int[])
        {
            return new IntArrayNBT((int[]) object);
        }
        else if (object instanceof long[])
        {
            return new LongArrayNBT((long[]) object);
        }
        else if (object instanceof byte[])
        {
            return new ByteArrayNBT((byte[]) object);
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported object type: " + object.getClass().getName());
        }
    }
}
