package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import dev.momostudios.coldsweat.client.event.ClientSoundHandler;
import net.minecraftforge.network.NetworkEvent;

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

    public static void encode(PlaySoundMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.soundID);
        buffer.writeFloat(message.volume);
        buffer.writeFloat(message.pitch);
        buffer.writeUUID(message.entityID);
    }

    public static PlaySoundMessage decode(FriendlyByteBuf buffer)
    {
        return new PlaySoundMessage(buffer.readInt(), buffer.readFloat(), buffer.readFloat(), buffer.readUUID());
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
                ClientSoundHandler.entity = Minecraft.getInstance().level.getPlayerByUUID(message.entityID);
            }
        });
        context.setPacketHandled(true);
    }
}
