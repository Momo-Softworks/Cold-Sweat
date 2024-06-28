package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.spec.MainSettingsConfig;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SyncConfigSettingsMessage implements CustomPacketPayload
{
    public static final UUID EMPTY_UUID = new UUID(0, 0);
    public static final CustomPacketPayload.Type<SyncConfigSettingsMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "sync_config_settings"));
    public static final StreamCodec<FriendlyByteBuf, SyncConfigSettingsMessage> CODEC = CustomPacketPayload.codec(SyncConfigSettingsMessage::encode, SyncConfigSettingsMessage::decode);

    Map<String, CompoundTag> configValues;
    UUID menuOpener;

    public SyncConfigSettingsMessage()
    {   this(EMPTY_UUID);
    }

    public SyncConfigSettingsMessage(UUID menuOpener)
    {   this(ConfigSettings.encode(), menuOpener);
    }

    private SyncConfigSettingsMessage(Map<String, CompoundTag> values, UUID menuOpener)
    {   this.configValues = values;
        this.menuOpener = menuOpener;
    }

    public static void encode(SyncConfigSettingsMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeUUID(message.menuOpener);
        buffer.writeInt(message.configValues.size());

        for (Map.Entry<String, CompoundTag> entry : message.configValues.entrySet())
        {   buffer.writeUtf(entry.getKey());
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

    public static void handle(SyncConfigSettingsMessage message, IPayloadContext context, boolean isServer)
    {
        context.enqueueWork(() ->
        {
            message.configValues.forEach(ConfigSettings::decode);

            if (isServer)
            {
                if (context.player().hasPermissions(2))
                {   ConfigSettings.saveValues();
                    MainSettingsConfig.getInstance().save();
                }
                PacketDistributor.sendToAllPlayers(new SyncConfigSettingsMessage(EMPTY_UUID));
            }
            else
            {
                if (message.menuOpener.equals(ClientOnlyHelper.getClientPlayer().getUUID()))
                {   ClientOnlyHelper.openConfigScreen();
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}