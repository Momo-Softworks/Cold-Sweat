package com.momosoftworks.coldsweat.common.entity.data;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Holds preferences that are synced from the client to the server.<br>
 * Preferences MUST be NBT serializable primitives or enums.
 */
public enum Preference
{
    UNITS("Units", FriendlyByteBuf::writeEnum, buf -> buf.readEnum(Temperature.Units.class),
        () -> ConfigSettings.CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F),
    WATERSKIN_PRIMARY("WaterskinPrimary", FriendlyByteBuf::writeEnum, buf -> buf.readEnum(WaterskinAction.class),
        ConfigSettings.WATERSKIN_USE_PRIMARY),
    WATERSKIN_SECONDARY("WaterskinSecondary", FriendlyByteBuf::writeEnum, buf -> buf.readEnum(WaterskinAction.class),
        ConfigSettings.WATERSKIN_USE_SECONDARY);

    private final String key;
    private final Writer<?> writer;
    private final Reader<?> reader;
    private final Supplier<?> getter;

    <T> Preference(String key, Writer<T> writer, Reader<T> reader, Supplier<T> getter)
    {
        this.key = key;
        this.writer = writer;
        this.reader = reader;
        this.getter = getter;
    }

    public String key()
    {   return this.key;
    }
    public Writer writer()
    {   return this.writer;
    }
    public Reader reader()
    {   return this.reader;
    }
    public Supplier getter()
    {   return this.getter;
    }

    @Nullable
    public static <T> T get(Player player, Preference preference)
    {
        CompoundTag preferenceNBT = player.getPersistentData().getCompound("ColdSweatPreferences");
        if (!preferenceNBT.contains(preference.key()))
        {   return null;
        }
        return (T) NBTHelper.deserialize(preferenceNBT.get(preference.key()));
    }

    public static <T> T getOrDefault(Player player, Preference preference, T defaultValue)
    {   return CSMath.orElse(get(player, preference), defaultValue);
    }

    @FunctionalInterface
    public interface Writer<T> extends StreamEncoder<FriendlyByteBuf, T>
    {}

    @FunctionalInterface
    public interface Reader<T> extends StreamDecoder<FriendlyByteBuf, T>
    {}

    public enum WaterskinAction implements StringRepresentable
    {
        DRINK("drink"),
        POUR("pour"),
        NONE("none");

        private final String name;

        WaterskinAction(String name)
        {   this.name = name;
        }

        @Override
        public String getSerializedName()
        {   return this.name;
        }

        public static WaterskinAction byName(String name)
        {   for (WaterskinAction behavior : values())
            {   if (behavior.name.equals(name))
                {   return behavior;
                }
            }
            return NONE;
        }
    }
}
