package com.momosoftworks.coldsweat.config.util;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.Codec;
import com.momosoftworks.coldsweat.data.ops.NBTOps;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Codec-native replacement for the old {@code ValueHolder}. Holds a config value that loads lazily
 * from the {@code valueCreator} (which reads the backing .cfg fields). Synced holders carry a
 * {@link Codec} that handles server-&gt;client transmission via {@link NBTOps}, mirroring 1.16's
 * {@code DynamicHolder}.<br>
 * If added to {@link ConfigSettings#CONFIG_SETTINGS}, synced holders are sent to clients on join.
 */
public class DynamicHolder<T> implements Supplier<T>
{
    private T value;
    private final String name;
    private final Supplier<T> valueCreator;
    private final Codec<T> codec;
    private final Consumer<T> saver;
    private final boolean synced;

    private DynamicHolder(String name, Supplier<T> valueCreator, Codec<T> codec, Consumer<T> saver, boolean synced)
    {   this.name = name;
        this.valueCreator = valueCreator;
        this.codec = codec;
        this.saver = saver;
        this.synced = synced;
    }

    /**
     * A holder that is not synced over the network.
     */
    public static <T> DynamicHolder<T> simple(String name, Supplier<T> valueCreator)
    {   return new DynamicHolder<T>(name, valueCreator, null, null, false);
    }

    /**
     * A holder synced server-&gt;client via the given codec; {@code saver} writes the received value back to config.
     */
    public static <T> DynamicHolder<T> synced(String name, Supplier<T> valueCreator, Codec<T> codec, Consumer<T> saver)
    {   return new DynamicHolder<T>(name, valueCreator, codec, saver, true);
    }

    public String getName()
    {   return name;
    }

    public Codec<T> getCodec()
    {   return codec;
    }

    @Override
    public T get()
    {
        if (this.value == null)
        {   this.load();
        }
        return value;
    }

    public void set(T value)
    {   this.value = value;
    }

    @SuppressWarnings("unchecked")
    public void setUnsafe(Object value)
    {   this.value = (T) value;
    }

    public void load()
    {   this.value = valueCreator.get();
    }

    public boolean isSynced()
    {   return synced;
    }

    /**
     * Encodes this holder's value into a compound (wrapped under "value" since codecs may emit non-compound NBT).
     */
    public NBTTagCompound encode()
    {
        if (!synced) throw new IllegalStateException("Tried to encode non-synced DynamicHolder \"" + name + "\"");
        Optional<NBTBase> encoded = codec.encode(NBTOps.INSTANCE, this.get());
        if (!encoded.isPresent())
        {   throw new RuntimeException("Failed to encode config setting \"" + name + "\"");
        }
        NBTTagCompound wrapper = new NBTTagCompound();
        wrapper.setTag("value", encoded.get());
        return wrapper;
    }

    public void decode(NBTTagCompound tag)
    {
        if (!synced) throw new IllegalStateException("Tried to decode non-synced DynamicHolder \"" + name + "\"");
        NBTBase encoded = tag.getTag("value");
        Optional<T> decoded = codec.decode(NBTOps.INSTANCE, encoded);
        if (!decoded.isPresent())
        {   ColdSweat.LOGGER.error("Failed to decode synced config setting \"" + name + "\"");
            return;
        }
        this.value = decoded.get();
        if (this.saver != null)
        {   this.saver.accept(this.value);
        }
    }

    public void save()
    {
        if (this.saver != null)
        {   this.saver.accept(this.get());
        }
    }
}
