package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.util.ClientOnlyHelper;
import dev.momostudios.coldsweat.config.ConfigSettings;
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
    static final UUID EMPTY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    Map<String, CompoundTag> configValues;
    UUID menuOpener;

    public SyncConfigSettingsMessage()
    {
        this(EMPTY_UUID);
    }

    public SyncConfigSettingsMessage(UUID menuOpener)
    {
        this(ConfigSettings.encode(), menuOpener);
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
    {
        UUID openMenu = buffer.readUUID();

        int size = buffer.readInt();
        Map<String, CompoundTag> values = new HashMap<>();
        for (int i = 0; i < size; i++)
        {   values.put(buffer.readUtf(), buffer.readNbt());
        }

        return new SyncConfigSettingsMessage(values, openMenu);
    }

    public static void handle(SyncConfigSettingsMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            message.configValues.forEach(ConfigSettings::decode);

            if (context.getDirection().getReceptionSide().isServer())
            {   ConfigSettings.saveValues();
                ColdSweatConfig.getInstance().save();

                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncConfigSettingsMessage(message.menuOpener));
            }
            else if (context.getDirection().getReceptionSide().isClient())
            {
                if (!message.menuOpener.equals(EMPTY_UUID))
                {   ClientOnlyHelper.openConfigScreen();
                }
            }
        });
        context.setPacketHandled(true);
    }
}
