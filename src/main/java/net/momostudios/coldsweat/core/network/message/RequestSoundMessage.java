package net.momostudios.coldsweat.core.network.message;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.client.event.PlayerDamageSoundClient;

import java.util.function.Supplier;

public class RequestSoundMessage
{
    int soundID;

    public RequestSoundMessage(int soundID) {
        this.soundID = soundID;
    }

    public static void encode(RequestSoundMessage message, PacketBuffer buffer) {
        buffer.writeInt(message.soundID);
    }

    public static RequestSoundMessage decode(PacketBuffer buffer)
    {
        return new RequestSoundMessage(buffer.readInt());
    }

    public static void handle(RequestSoundMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isClient())
            {
                switch (message.soundID)
                {
                    case 0:
                        PlayerDamageSoundClient.playDamageSound = 0;
                        break;
                    case 1:
                        PlayerDamageSoundClient.playDamageSound = 1;
                        break;
                    case 2:
                        PlayerDamageSoundClient.playDamageSound = 2;
                        break;
                }
            }
        });
    }
}
