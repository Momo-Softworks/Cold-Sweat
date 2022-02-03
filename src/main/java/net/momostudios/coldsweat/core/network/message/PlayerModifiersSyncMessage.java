package net.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.common.world.TempModifierEntries;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.util.NBTHelper;
import net.momostudios.coldsweat.util.PlayerTemp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PlayerModifiersSyncMessage
{
    public List<TempModifier> ambient;
    public List<TempModifier> body;
    public List<TempModifier> base;
    public List<TempModifier> rate;

    public PlayerModifiersSyncMessage() {
    }

    public PlayerModifiersSyncMessage(List<TempModifier> ambient, List<TempModifier> body, List<TempModifier> base, List<TempModifier> rate) {
        this.ambient = ambient;
        this.body = body;
        this.base = base;
        this.rate = rate;
    }

    public static void encode(PlayerModifiersSyncMessage message, PacketBuffer buffer)
    {
        buffer.writeCompoundTag(writeToNBT(message, PlayerTemp.Types.AMBIENT));
        buffer.writeCompoundTag(writeToNBT(message, PlayerTemp.Types.BODY));
        buffer.writeCompoundTag(writeToNBT(message, PlayerTemp.Types.BASE));
        buffer.writeCompoundTag(writeToNBT(message, PlayerTemp.Types.RATE));
    }

    public static PlayerModifiersSyncMessage decode(PacketBuffer buffer)
    {
        return new PlayerModifiersSyncMessage(
                readFromNBT(buffer.readCompoundTag()),
                readFromNBT(buffer.readCompoundTag()),
                readFromNBT(buffer.readCompoundTag()),
                readFromNBT(buffer.readCompoundTag()));
    }

    private static CompoundNBT writeToNBT(PlayerModifiersSyncMessage message, PlayerTemp.Types type)
    {
        CompoundNBT nbt = new CompoundNBT();
        List<TempModifier> referenceList =
                type == PlayerTemp.Types.AMBIENT ? message.ambient :
                type == PlayerTemp.Types.BODY ? message.body :
                type == PlayerTemp.Types.BASE ? message.base :
                message.rate;

        // Iterate modifiers and write to NBT
        for (int i = 0; i < referenceList.size(); i++)
        {
            TempModifier modifier = referenceList.get(i);

            if (modifier != null && modifier.getID() != null)
            {
                nbt.put(String.valueOf(i), NBTHelper.modifierToNBT(modifier));
            }
        }

        return nbt;
    }

    private static List<TempModifier> readFromNBT(CompoundNBT nbt)
    {
        List<TempModifier> modifiers = new ArrayList<>();
        for (String key : nbt.keySet())
        {
            TempModifier modifier = NBTHelper.NBTToModifier(nbt.getCompound(key));

            if (modifier != null)
                modifiers.add(modifier);
        }
        return modifiers;
    }

    public static void handle(PlayerModifiersSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            ClientPlayerEntity player = Minecraft.getInstance().player;

            if (player != null)
            player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
            {
                cap.clearModifiers(PlayerTemp.Types.AMBIENT);
                cap.getModifiers(PlayerTemp.Types.AMBIENT).addAll(message.ambient);

                cap.clearModifiers(PlayerTemp.Types.BODY);
                cap.getModifiers(PlayerTemp.Types.BODY).addAll(message.body);

                cap.clearModifiers(PlayerTemp.Types.BASE);
                cap.getModifiers(PlayerTemp.Types.BASE).addAll(message.base);

                cap.clearModifiers(PlayerTemp.Types.RATE);
                cap.getModifiers(PlayerTemp.Types.RATE).addAll(message.rate);
            });
        });
        context.setPacketHandled(true);
    }
}
