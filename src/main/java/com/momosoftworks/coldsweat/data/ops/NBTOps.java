package com.momosoftworks.coldsweat.data.ops;

import net.minecraft.nbt.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NBTOps extends CodecOps<NBTBase>
{
    public static final NBTOps INSTANCE = new NBTOps();

    /** NBTTagList has no public generic element accessor in 1.7.10, so we reach its backing list reflectively. */
    private static final Field TAG_LIST_FIELD;
    static
    {
        Field found = null;
        for (Field field : NBTTagList.class.getDeclaredFields())
        {
            if (List.class.isAssignableFrom(field.getType()))
            {   field.setAccessible(true);
                found = field;
                break;
            }
        }
        TAG_LIST_FIELD = found;
    }

    @Override
    public Optional<Byte> toByte(NBTBase obj)
    {   return convert(obj, NBTTagByte.class).map(NBTTagByte::func_150290_f);
    }

    @Override
    public Optional<Integer> toInt(NBTBase obj)
    {   return convert(obj, NBTTagInt.class).map(NBTTagInt::func_150287_d);
    }

    @Override
    public Optional<Short> toShort(NBTBase obj)
    {   return convert(obj, NBTTagShort.class).map(NBTTagShort::func_150289_e);
    }

    @Override
    public Optional<Long> toLong(NBTBase obj)
    {   return convert(obj, NBTTagLong.class).map(NBTTagLong::func_150291_c);
    }

    @Override
    public Optional<Double> toDouble(NBTBase obj)
    {   return convert(obj, NBTTagDouble.class).map(NBTTagDouble::func_150286_g);
    }

    @Override
    public Optional<Number> toNumber(NBTBase obj)
    {
        if (obj instanceof NBTBase.NBTPrimitive)
        {   NBTBase.NBTPrimitive primitive = (NBTBase.NBTPrimitive) obj;
            return Optional.of(primitive.func_150286_g());
        }
        else
        {   return Optional.empty();
        }
    }

    @Override
    public Optional<Boolean> toBool(NBTBase obj)
    {   return convert(obj, NBTTagByte.class).map(tag -> tag.func_150290_f() != 0);
    }

    @Override
    public Optional<String> toString(NBTBase obj)
    {   return convert(obj, NBTTagString.class).map(NBTTagString::func_150285_a_);
    }

    @Override
    public Optional<NBTBase> fromByte(byte b)
    {   return Optional.of((NBTBase) (Object) new NBTTagByte(b));
    }

    @Override
    public Optional<NBTBase> fromInt(int i)
    {   return Optional.of((NBTBase) (Object) new NBTTagInt(i));
    }

    @Override
    public Optional<NBTBase> fromShort(short s)
    {   return Optional.of((NBTBase) (Object) new NBTTagShort(s));
    }

    @Override
    public Optional<NBTBase> fromLong(long l)
    {   return Optional.of((NBTBase) (Object) new NBTTagLong(l));
    }

    @Override
    public Optional<NBTBase> fromDouble(double d)
    {   return Optional.of((NBTBase) (Object) new NBTTagDouble(d));
    }

    @Override
    public Optional<NBTBase> fromNumber(Number n)
    {   return fromDouble(n.doubleValue());
    }

    @Override
    public Optional<NBTBase> fromBool(boolean b)
    {   return Optional.of((NBTBase) (Object) new NBTTagByte((byte) (b ? 1 : 0)));
    }

    @Override
    public Optional<NBTBase> fromString(String s)
    {   return Optional.of(new NBTTagString(s));
    }

    @Override
    public NBTBase createMap()
    {   return new NBTTagCompound();
    }

    @Override
    public void put(NBTBase map, String key, NBTBase value)
    {
        if (map instanceof NBTTagCompound)
        {
            NBTTagCompound compound = (NBTTagCompound) map;
            compound.setTag(key, value);
        }
    }

    @Override
    public NBTBase get(NBTBase map, String key)
    {
        if (map instanceof NBTTagCompound)
        {   NBTTagCompound compound = (NBTTagCompound) map;
            return compound.getTag(key);
        }
        return null;
    }

    @Override
    public NBTBase createList()
    {   return new NBTTagList();
    }

    @Override
    public void add(NBTBase list, NBTBase value)
    {
        if (list instanceof NBTTagList)
        {   ((NBTTagList) list).appendTag(value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NBTBase> getList(NBTBase list)
    {
        if (!(list instanceof NBTTagList) || TAG_LIST_FIELD == null) return Collections.emptyList();
        try
        {   return new ArrayList<NBTBase>((List<NBTBase>) TAG_LIST_FIELD.get(list));
        }
        catch (IllegalAccessException e)
        {   return Collections.emptyList();
        }
    }

    protected <V> Optional<V> convert(NBTBase obj, Class<V> clazz)
    {   return clazz.isInstance(obj) ? Optional.of(clazz.cast(obj)) : Optional.empty();
    }
}
