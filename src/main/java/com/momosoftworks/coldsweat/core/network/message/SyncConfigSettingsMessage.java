package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.config.spec.MainSettingsConfig;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncConfigSettingsMessage
{
    public static final UUID EMPTY_UUID = new UUID(0, 0);

    Map<String, CompoundTag> configValues;
    UUID menuOpener;

    public SyncConfigSettingsMessage(RegistryAccess registryAccess)
    {   this(EMPTY_UUID, registryAccess);
    }

    public SyncConfigSettingsMessage(UUID menuOpener, RegistryAccess registryAccess)
    {   this(ConfigSettings.encode(registryAccess), menuOpener);
    }

    private SyncConfigSettingsMessage(Map<String, CompoundTag> values, UUID menuOpener)
    {
        this.configValues = values;
        this.menuOpener = menuOpener;
    }

    public static void encode(SyncConfigSettingsMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeUUID(message.menuOpener);
        buffer.writeInt(message.configValues.size());
        for (Map.Entry<String, CompoundTag> entry : message.configValues.entrySet())
        {
            buffer.writeUtf(entry.getKey());
            buffer.writeNbt(entry.getValue());
        }
    }

    public static SyncConfigSettingsMessage decode(FriendlyByteBuf buffer)
    {   UUID menuOpener = buffer.readUUID();
        int size = buffer.readInt();
        Map<String, CompoundTag> values = new HashMap<>();

        for (int i = 0; i < size; i++)
        {   values.put(buffer.readUtf(), buffer.readNbt());
        }

        return new SyncConfigSettingsMessage(values, menuOpener);
    }

    public static void handle(SyncConfigSettingsMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            RegistryAccess registryAccess = RegistryHelper.getRegistryAccess();
            message.configValues.forEach((name, values) -> ConfigSettings.decode(name, values, registryAccess));

            if (context.getDirection().getReceptionSide().isServer())
            {
                if (context.getSender() != null && context.getSender().hasPermissions(2))
                {   ConfigSettings.saveValues(registryAccess);
                    MainSettingsConfig.getInstance().save();
                }

                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncConfigSettingsMessage(EMPTY_UUID, registryAccess));
            }
            else if (context.getDirection().getReceptionSide().isClient())
            {
                if (message.menuOpener.equals(ClientOnlyHelper.getClientPlayer().getUUID()))
                {   ClientOnlyHelper.openConfigScreen();
                }
            }
        });
        context.setPacketHandled(true);
    }
}
