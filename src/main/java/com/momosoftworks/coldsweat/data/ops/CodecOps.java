package com.momosoftworks.coldsweat.data.ops;

import java.util.List;
import java.util.Optional;

public abstract class CodecOps<T>
{
    public abstract Optional<Byte> toByte(T obj);
    public abstract Optional<Integer> toInt(T obj);
    public abstract Optional<Short> toShort(T obj);
    public abstract Optional<Long> toLong(T obj);
    public abstract Optional<Double> toDouble(T obj);
    public abstract Optional<Number> toNumber(T obj);
    public abstract Optional<Boolean> toBool(T obj);
    public abstract Optional<String> toString(T obj);

    public abstract Optional<T> fromByte(byte b);
    public abstract Optional<T> fromInt(int i);
    public abstract Optional<T> fromShort(short s);
    public abstract Optional<T> fromLong(long l);
    public abstract Optional<T> fromDouble(double d);
    public abstract Optional<T> fromNumber(Number n);
    public abstract Optional<T> fromBool(boolean b);
    public abstract Optional<T> fromString(String s);

    public abstract T createMap();
    public abstract void put(T map, String key, T value);
    public abstract T get(T map, String key);

    public abstract T createList();
    public abstract void add(T list, T value);
    public abstract List<T> getList(T list);
}
