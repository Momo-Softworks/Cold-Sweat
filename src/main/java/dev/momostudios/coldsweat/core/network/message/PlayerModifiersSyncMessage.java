package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.common.capability.CSCapabilities;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import dev.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PlayerModifiersSyncMessage
{
    public List<TempModifier> ambient;
    public List<TempModifier> body;
    public List<TempModifier> base;
    public List<TempModifier> rate;

    public PlayerModifiersSyncMessage(List<TempModifier> ambient, List<TempModifier> body, List<TempModifier> base, List<TempModifier> rate) {
        this.ambient = ambient;
        this.body = body;
        this.base = base;
        this.rate = rate;
    }

    public static void encode(PlayerModifiersSyncMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeNbt(writeToNBT(message, Temperature.Types.AMBIENT));
        buffer.writeNbt(writeToNBT(message, Temperature.Types.BODY));
        buffer.writeNbt(writeToNBT(message, Temperature.Types.BASE));
        buffer.writeNbt(writeToNBT(message, Temperature.Types.RATE));
    }

    public static PlayerModifiersSyncMessage decode(FriendlyByteBuf buffer)
    {
        return new PlayerModifiersSyncMessage(
                readFromNBT(buffer.readNbt()),
                readFromNBT(buffer.readNbt()),
                readFromNBT(buffer.readNbt()),
                readFromNBT(buffer.readNbt()));
    }

    private static CompoundTag writeToNBT(PlayerModifiersSyncMessage message, Temperature.Types type)
    {
        CompoundTag nbt = new CompoundTag();
        List<TempModifier> referenceList =
                type == Temperature.Types.AMBIENT ? message.ambient :
                type == Temperature.Types.BODY ? message.body :
                type == Temperature.Types.BASE ? message.base :
                message.rate;

        // Iterate modifiers and write to NBT
        for (int i = 0; i < referenceList.size(); i++)
        {
            TempModifier modifier = referenceList.get(i);

            if (modifier != null && modifier.getID() != null)
            {
                nbt.put(String.valueOf(i), NBTHelper.modifierToTag(modifier));
            }
        }

        return nbt;
    }

    private static List<TempModifier> readFromNBT(CompoundTag nbt)
    {
        List<TempModifier> modifiers = new ArrayList<>();
        for (String key : nbt.getAllKeys())
        {
            TempModifier modifier = NBTHelper.TagToModifier(nbt.getCompound(key));

            if (modifier != null)
                modifiers.add(modifier);
        }
        return modifiers;
    }

    public static void handle(PlayerModifiersSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () ->syncTemperature(message)));

        context.setPacketHandled(true);
    }

    public static DistExecutor.SafeRunnable syncTemperature(PlayerModifiersSyncMessage message)
    {
        return () ->
        {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null)
            {
                player.getCapability(CSCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
                {
                    cap.clearModifiers(Temperature.Types.AMBIENT);
                    cap.getModifiers(Temperature.Types.AMBIENT).addAll(message.ambient);

                    cap.clearModifiers(Temperature.Types.BODY);
                    cap.getModifiers(Temperature.Types.BODY).addAll(message.body);

                    cap.clearModifiers(Temperature.Types.BASE);
                    cap.getModifiers(Temperature.Types.BASE).addAll(message.base);

                    cap.clearModifiers(Temperature.Types.RATE);
                    cap.getModifiers(Temperature.Types.RATE).addAll(message.rate);
                });
            }
        };
    }
}