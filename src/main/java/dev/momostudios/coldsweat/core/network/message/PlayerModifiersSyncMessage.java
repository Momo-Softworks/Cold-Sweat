package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PlayerModifiersSyncMessage
{
    public List<TempModifier> world;
    public List<TempModifier> core;
    public List<TempModifier> base;
    public List<TempModifier> rate;
    public List<TempModifier> max;
    public List<TempModifier> min;

    public PlayerModifiersSyncMessage(List<TempModifier> world, List<TempModifier> core, List<TempModifier> base, List<TempModifier> rate,
                                      List<TempModifier> max, List<TempModifier> min)
    {
        this.world = world;
        this.core = core;
        this.base = base;
        this.rate = rate;
        this.max = max;
        this.min = min;
    }

    public static void encode(PlayerModifiersSyncMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeNbt(writeToNBT(message.world));
        buffer.writeNbt(writeToNBT(message.core));
        buffer.writeNbt(writeToNBT(message.base));
        buffer.writeNbt(writeToNBT(message.rate));
        buffer.writeNbt(writeToNBT(message.max));
        buffer.writeNbt(writeToNBT(message.min));
    }

    public static PlayerModifiersSyncMessage decode(FriendlyByteBuf buffer)
    {
        return new PlayerModifiersSyncMessage(
                readFromNBT(buffer.readNbt()),
                readFromNBT(buffer.readNbt()),
                readFromNBT(buffer.readNbt()),
                readFromNBT(buffer.readNbt()),
                readFromNBT(buffer.readNbt()),
                readFromNBT(buffer.readNbt()));
    }

    private static CompoundTag writeToNBT(List<TempModifier> referenceList)
    {
        CompoundTag nbt = new CompoundTag();

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
            TempModifier modifier = NBTHelper.tagToModifier(nbt.getCompound(key));

            if (modifier != null)
                modifiers.add(modifier);
        }
        return modifiers;
    }

    public static void handle(PlayerModifiersSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                LocalPlayer player = Minecraft.getInstance().player;

                if (player != null)
                {
                    player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
                    {
                        cap.clearModifiers(Temperature.Types.WORLD);
                        cap.getModifiers(Temperature.Types.WORLD).addAll(message.world);

                        cap.clearModifiers(Temperature.Types.CORE);
                        cap.getModifiers(Temperature.Types.CORE).addAll(message.core);

                        cap.clearModifiers(Temperature.Types.BASE);
                        cap.getModifiers(Temperature.Types.BASE).addAll(message.base);

                        cap.clearModifiers(Temperature.Types.RATE);
                        cap.getModifiers(Temperature.Types.RATE).addAll(message.rate);

                        cap.clearModifiers(Temperature.Types.MAX);
                        cap.getModifiers(Temperature.Types.MAX).addAll(message.max);

                        cap.clearModifiers(Temperature.Types.MIN);
                        cap.getModifiers(Temperature.Types.MIN).addAll(message.min);
                    });
                }
            });
        }

        context.setPacketHandled(true);
    }
}