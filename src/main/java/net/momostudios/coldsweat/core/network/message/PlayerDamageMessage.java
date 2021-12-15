package net.momostudios.coldsweat.core.network.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.client.event.PlayerDamageSoundClient;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class PlayerDamageMessage
{
    public PlayerDamageMessage() {
    }

    public static void encode(PlayerDamageMessage message, PacketBuffer buffer) {
    }

    public static PlayerDamageMessage decode(PacketBuffer buffer)
    {
        return new PlayerDamageMessage();
    }

    public static void handle(PlayerDamageMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isClient())
            {
                PlayerDamageSoundClient.playDamageSound = true;
            }
        });
    }
}
