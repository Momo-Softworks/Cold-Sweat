package net.momostudios.coldsweat.core.network.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.core.util.registrylists.ModEffects;

import java.util.function.Supplier;

public class GracePeriodAskMessage
{
    int duration;

    public GracePeriodAskMessage(int duration)
    {
        this.duration = duration;
    }

    public static void encode(GracePeriodAskMessage message, PacketBuffer buffer) {
        buffer.writeInt(message.duration);
    }

    public static GracePeriodAskMessage decode(PacketBuffer buffer)
    {
        return new GracePeriodAskMessage(buffer.readInt());
    }

    public static void handle(GracePeriodAskMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (!context.getSender().getPersistentData().getBoolean("givenGracePeriod"))
            {
                context.getSender().getPersistentData().putBoolean("givenGracePeriod", true);
                context.getSender().addPotionEffect(new EffectInstance(ModEffects.GRACE, message.duration, 0, false, false));
            }
        });
    }
}