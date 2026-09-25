package com.momosoftworks.coldsweat.util.serialization;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MapBuilder<K, V>
{
    private final Map<K, V> map;

    protected MapBuilder(Type type, Class<K> keyType)
    {
        switch (type)
        {
            case HASH:
                map = new HashMap<>();
                break;
            case LINKED:
                map = new LinkedHashMap<>();
                break;
            case TREE:
                map = new TreeMap<>();
                break;
            case IDENTITY:
                map = new IdentityHashMap<>();
                break;
            case CONCURRENT_HASH:
                map = new ConcurrentHashMap<>();
                break;
            case ENUM:
                map = new EnumMap(keyType);
                break;
            default:
                throw new IllegalArgumentException("Unknown map type: " + type);
        }
    }

    public static <K, V> MapBuilder<K, V> start()
    {   return new MapBuilder<>(Type.HASH, null);
    }
    public static <K, V> MapBuilder<K, V> start(K key, V value)
    {   return new MapBuilder<K, V>(Type.HASH, null).put(key, value);
    }
    public static <K, V> MapBuilder<K, V> start(Type type, K key, V value)
    {   return new MapBuilder<K, V>(type, null).put(key, value);
    }
    public static <K extends Enum<K>, V> MapBuilder<K, V> start(Class<K> keyType, K key, V value)
    {   return new MapBuilder<K, V>(Type.ENUM, keyType).put(key, value);
    }

    public MapBuilder<K, V> put(K key, V value)
    {   map.put(key, value);
        return this;
    }

    public Map<K, V> build()
    {   return map;
    }

    public enum Type
    {
        HASH,
        LINKED,
        TREE,
        IDENTITY,
        CONCURRENT_HASH,
        ENUM
    }
}
