package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import dev.momostudios.coldsweat.client.event.ClientSoundHandler;

import java.util.UUID;
import java.util.function.Supplier;

public class PlaySoundMessage
{
    int soundID;
    float volume;
    float pitch;
    UUID entityID;

    public PlaySoundMessage(int soundID, float volume, float pitch, UUID entityID)
    {
        this.soundID = soundID;
        this.volume = volume;
        this.pitch = pitch;
        this.entityID = entityID;
    }

    public static void encode(PlaySoundMessage message, PacketBuffer buffer) {
        buffer.writeInt(message.soundID);
        buffer.writeFloat(message.volume);
        buffer.writeFloat(message.pitch);
        buffer.writeUniqueId(message.entityID);
    }

    public static PlaySoundMessage decode(PacketBuffer buffer)
    {
        return new PlaySoundMessage(buffer.readInt(), buffer.readFloat(), buffer.readFloat(), buffer.readUniqueId());
    }

    public static void handle(PlaySoundMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isClient())
            {
                switch (message.soundID)
                {
                    case 0:
                        ClientSoundHandler.playDamageSound = 0;
                        break;
                    case 1:
                        ClientSoundHandler.playDamageSound = 1;
                        break;
                    case 2:
                        ClientSoundHandler.playDamageSound = 2;
                        break;
                }

                ClientSoundHandler.volume = message.volume;
                ClientSoundHandler.pitch = message.pitch;
                ClientSoundHandler.entity = Minecraft.getInstance().world.getPlayerByUuid(message.entityID);
            }
        });
        context.setPacketHandled(true);
    }
}
