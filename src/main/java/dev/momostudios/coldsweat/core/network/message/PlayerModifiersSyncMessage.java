package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.capability.PlayerTempCap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerModifiersSyncMessage
{
    CompoundTag modifiers;

    public PlayerModifiersSyncMessage(CompoundTag modifiers)
    {
        this.modifiers = modifiers;
    }

    public static void encode(PlayerModifiersSyncMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeNbt(message.modifiers);
    }

    public static PlayerModifiersSyncMessage decode(FriendlyByteBuf buffer)
    {
        return new PlayerModifiersSyncMessage(buffer.readNbt());
    }

    public static void handle(PlayerModifiersSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient())
        context.enqueueWork(() ->
        {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null)
            {
                player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
                {
                    if (cap instanceof PlayerTempCap playerCap)
                    {
                        playerCap.deserializeModifiers(message.modifiers);
                    }
                });
            }
        });

        context.setPacketHandled(true);
    }
}