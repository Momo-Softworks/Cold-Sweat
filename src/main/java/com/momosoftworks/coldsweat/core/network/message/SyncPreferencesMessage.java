package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.entity.data.Preference;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SyncPreferencesMessage
{
    EnumMap<Preference, Object> preferences = new EnumMap<>(Preference.class);

    public SyncPreferencesMessage()
    {}

    public static SyncPreferencesMessage create()
    {
        SyncPreferencesMessage message = new SyncPreferencesMessage();
        for (Preference preference : Preference.values())
        {   message.preferences.put(preference, preference.getter().get());
        }
        return message;
    }

    public static void encode(SyncPreferencesMessage message, PacketBuffer buffer)
    {
        if (EffectiveSide.get().isClient())
        {   savePreferences(ClientOnlyHelper.getClientPlayer(), message.preferences);
        }
        AtomicReference<Preference> pref = new AtomicReference<>();
        buffer.writeInt(message.preferences.size());
        message.preferences.forEach((key, value) ->
        {   pref.set(key);
            buffer.writeEnum(key);
            pref.get().writer().accept(buffer, value);
        });
    }

    public static SyncPreferencesMessage decode(PacketBuffer buffer)
    {
        SyncPreferencesMessage message = new SyncPreferencesMessage();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++)
        {
            Preference preference = buffer.readEnum(Preference.class);
            Object value = preference.reader().apply(buffer);
            message.preferences.put(preference, value);
        }
        return message;
    }

    public static void handle(SyncPreferencesMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isServer())
            {
                if (context.getSender() != null)
                {
                    PlayerEntity player = context.getSender();
                    // Save the preferences to the player's persistent data
                    savePreferences(player, message.preferences);
                }
            }
        });
        context.setPacketHandled(true);
    }

    private static void savePreferences(PlayerEntity player, EnumMap<Preference, Object> preferences)
    {
        CompoundNBT nbt = player.getPersistentData();
        CompoundNBT preferencesTag = new CompoundNBT();
        for (Preference preference : Preference.values())
        {   preferencesTag.put(preference.key(), NBTHelper.serialize(preferences.get(preference)));
        }
        nbt.put("ColdSweatPreferences", preferencesTag);
    }
}
