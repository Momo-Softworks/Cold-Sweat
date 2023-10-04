package com.momosoftworks.coldsweat.util.math;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MapN<K, V> extends AbstractMap<K,V>
{
    final Object[] table; // pairs of key, value

    final int size; // number of pairs

    MapN(Object... input)
    {
        if ((input.length & 1) != 0)
        {   // implicit nullcheck of input
            throw new InternalError("length is odd");
        }
        size = input.length >> 1;

        int len = 2 * input.length;
        len = (len + 1) & ~1; // ensure table is even length
        table = new Object[len];

        for (int i = 0; i < input.length; i += 2)
        {   @SuppressWarnings("unchecked")
            K k = Objects.requireNonNull((K) input[i]);
            @SuppressWarnings("unchecked")
            V v = Objects.requireNonNull((V) input[i + 1]);
            int idx = probe(k);
            if (idx >= 0)
            {   throw new IllegalArgumentException("duplicate key: " + k);
            }
            else
            {   int dest = -(idx + 1);
                table[dest] = k;
                table[dest + 1] = v;
            }
        }
    }

    private int probe(Object pk)
    {
        int idx = Math.floorMod(pk.hashCode(), table.length >> 1) << 1;
        while (true)
        {   @SuppressWarnings("unchecked")
            K ek = (K) table[idx];
            if (ek == null)
            {   return -idx - 1;
            }
            else if (pk.equals(ek))
            {   return idx;
            }
            else if ((idx += 2) == table.length)
            {   idx = 0;
            }
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        Set<Entry<K, V>> entries = new HashSet<>();
        for (int i = 0; i < table.length; i += 2)
        {   @SuppressWarnings("unchecked")
            K k = (K) table[i];
            if (k != null)
            {   @SuppressWarnings("unchecked")
                V v = (V) table[i + 1];
                entries.add(new SimpleEntry<>(k, v));
            }
        }
        return entries;
    }
}
