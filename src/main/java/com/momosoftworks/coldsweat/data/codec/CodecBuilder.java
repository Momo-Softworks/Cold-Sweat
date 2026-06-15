package com.momosoftworks.coldsweat.data.codec;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.ops.CodecOps;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CodecBuilder<T>
{
    private final Class<T> clazz;
    private final List<Param<T, ?>> params = new ArrayList<Param<T, ?>>();

    public CodecBuilder(Class<T> clazz)
    {   this.clazz = clazz;
    }

    /**
     * Adds a required field. Decoding fails if the field is missing or malformed.
     */
    public <V> CodecBuilder<T> param(String name, Class<V> clazz, Codec<V> codec, Function<T, V> getter)
    {
        params.add(new Param<T, V>(name, clazz, codec, getter, false, null));
        return this;
    }

    /**
     * Adds an optional field. If the field is missing or fails to decode, {@code defaultValue} is used instead.
     */
    public <V> CodecBuilder<T> optional(String name, Class<V> clazz, Codec<V> codec, Function<T, V> getter, V defaultValue)
    {
        params.add(new Param<T, V>(name, clazz, codec, getter, true, defaultValue));
        return this;
    }

    public Codec<T> build()
    {
        final Constructor<T> constructor = findConstructor();
        constructor.setAccessible(true);

        return new Codec<T>()
        {
            @Override
            public <E> Optional<T> decode(CodecOps<E> ops, E obj)
            {
                if (obj == null) return Optional.empty();
                try
                {
                    Object[] args = new Object[params.size()];
                    for (int i = 0; i < params.size(); i++)
                    {
                        Param<T, ?> param = params.get(i);
                        // Read the named field out of the map, then decode just that field's value
                        E field = ops.get(obj, param.name);
                        if (field == null)
                        {
                            if (param.optional)
                            {   args[i] = param.defaultValue;
                                continue;
                            }
                            ColdSweat.LOGGER.error("Codec for " + CodecBuilder.this.clazz.getSimpleName()
                                                 + " failed: missing required field \"" + param.name + "\"");
                            return Optional.empty();
                        }
                        Optional<?> decoded = param.codec.decode(ops, field);
                        if (!decoded.isPresent())
                        {
                            if (param.optional)
                            {   args[i] = param.defaultValue;
                                continue;
                            }
                            ColdSweat.LOGGER.error("Codec for " + CodecBuilder.this.clazz.getSimpleName()
                                                 + " failed: could not decode field \"" + param.name + "\"");
                            return Optional.empty();
                        }
                        args[i] = decoded.get();
                    }
                    return Optional.of(constructor.newInstance(args));
                }
                catch (Exception e)
                {   ColdSweat.LOGGER.error("Codec for " + CodecBuilder.this.clazz.getSimpleName() + " failed to construct instance", e);
                    return Optional.empty();
                }
            }

            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public <E> Optional<E> encode(CodecOps<E> ops, T obj)
            {
                try
                {
                    E map = ops.createMap();
                    for (Param param : params)
                    {
                        Object value = param.getter.apply(obj);
                        Optional<?> encoded = param.codec.encode(ops, value);
                        if (!encoded.isPresent())
                        {
                            ColdSweat.LOGGER.error("Codec for " + CodecBuilder.this.clazz.getSimpleName()
                                                 + " failed: could not encode field \"" + param.name + "\"");
                            return Optional.empty();
                        }
                        ops.put(map, param.name, (E) encoded.get());
                    }
                    return Optional.of(map);
                }
                catch (Exception e)
                {   ColdSweat.LOGGER.error("Codec for " + CodecBuilder.this.clazz.getSimpleName() + " failed to encode", e);
                    return Optional.empty();
                }
            }
        };
    }

    /**
     * Finds a constructor whose parameters match the declared params, treating primitive and wrapper
     * types as compatible (so a record with a {@code double}/{@code int} constructor still resolves).
     */
    @SuppressWarnings("unchecked")
    private Constructor<T> findConstructor()
    {
        for (Constructor<?> candidate : clazz.getDeclaredConstructors())
        {
            Class<?>[] ctorTypes = candidate.getParameterTypes();
            if (ctorTypes.length != params.size()) continue;

            boolean matches = true;
            for (int i = 0; i < ctorTypes.length; i++)
            {
                if (!isCompatible(ctorTypes[i], params.get(i).clazz))
                {   matches = false;
                    break;
                }
            }
            if (matches) return (Constructor<T>) candidate;
        }
        throw new IllegalArgumentException("Class " + clazz.getName() + " must have a constructor with parameters: "
            + buildParamSignature());
    }

    private String buildParamSignature()
    {
        StringBuilder sb = new StringBuilder();
        for (Param<T, ?> p : params)
        {   if (sb.length() > 0) sb.append(", ");
            sb.append(p.name).append(": ").append(p.clazz.getSimpleName());
        }
        return sb.length() == 0 ? "none" : sb.toString();
    }

    private static boolean isCompatible(Class<?> ctorType, Class<?> paramType)
    {
        if (ctorType.isAssignableFrom(paramType)) return true;
        // Treat primitive <-> wrapper as compatible (newInstance auto-(un)boxes the actual args)
        return wrap(ctorType).equals(wrap(paramType));
    }

    private static final Map<Class<?>, Class<?>> WRAPPERS = new HashMap<Class<?>, Class<?>>();
    static
    {
        WRAPPERS.put(boolean.class, Boolean.class);
        WRAPPERS.put(byte.class, Byte.class);
        WRAPPERS.put(short.class, Short.class);
        WRAPPERS.put(int.class, Integer.class);
        WRAPPERS.put(long.class, Long.class);
        WRAPPERS.put(float.class, Float.class);
        WRAPPERS.put(double.class, Double.class);
        WRAPPERS.put(char.class, Character.class);
    }

    private static Class<?> wrap(Class<?> c)
    {   return c.isPrimitive() ? WRAPPERS.get(c) : c;
    }

    protected static class Param<P, V>
    {
        private final String name;
        private final Class<V> clazz;
        private final Codec<V> codec;
        private final Function<P, V> getter;
        private final boolean optional;
        private final V defaultValue;

        public Param(String name, Class<V> clazz, Codec<V> codec, Function<P, V> getter, boolean optional, V defaultValue)
        {   this.name = name;
            this.clazz = clazz;
            this.codec = codec;
            this.getter = getter;
            this.optional = optional;
            this.defaultValue = defaultValue;
        }
    }
}
