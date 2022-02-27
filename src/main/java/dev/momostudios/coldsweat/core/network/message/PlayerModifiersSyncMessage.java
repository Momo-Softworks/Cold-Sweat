package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import dev.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import dev.momostudios.coldsweat.util.NBTHelper;
import dev.momostudios.coldsweat.util.PlayerHelper;
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

    public PlayerModifiersSyncMessage() {
    }

    public PlayerModifiersSyncMessage(List<TempModifier> ambient, List<TempModifier> body, List<TempModifier> base, List<TempModifier> rate) {
        this.ambient = ambient;
        this.body = body;
        this.base = base;
        this.rate = rate;
    }

    public static void encode(PlayerModifiersSyncMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeNbt(writeToNBT(message, PlayerHelper.Types.AMBIENT));
        buffer.writeNbt(writeToNBT(message, PlayerHelper.Types.BODY));
        buffer.writeNbt(writeToNBT(message, PlayerHelper.Types.BASE));
        buffer.writeNbt(writeToNBT(message, PlayerHelper.Types.RATE));
    }

    public static PlayerModifiersSyncMessage decode(FriendlyByteBuf buffer)
    {
        return new PlayerModifiersSyncMessage(
                readFromNBT(buffer.readNbt()),
                readFromNBT(buffer.readNbt()),
                readFromNBT(buffer.readNbt()),
                readFromNBT(buffer.readNbt()));
    }

    private static CompoundTag writeToNBT(PlayerModifiersSyncMessage message, PlayerHelper.Types type)
    {
        CompoundTag nbt = new CompoundTag();
        List<TempModifier> referenceList =
                type == PlayerHelper.Types.AMBIENT ? message.ambient :
                type == PlayerHelper.Types.BODY ? message.body :
                type == PlayerHelper.Types.BASE ? message.base :
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
        return new DistExecutor.SafeRunnable()
        {
            @Override
            public void run()
            {
                LocalPlayer player = Minecraft.getInstance().player;

                if (player != null)
                {
                    player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
                    {
                        cap.clearModifiers(PlayerHelper.Types.AMBIENT);
                        cap.getModifiers(PlayerHelper.Types.AMBIENT).addAll(message.ambient);

                        cap.clearModifiers(PlayerHelper.Types.BODY);
                        cap.getModifiers(PlayerHelper.Types.BODY).addAll(message.body);

                        cap.clearModifiers(PlayerHelper.Types.BASE);
                        cap.getModifiers(PlayerHelper.Types.BASE).addAll(message.base);

                        cap.clearModifiers(PlayerHelper.Types.RATE);
                        cap.getModifiers(PlayerHelper.Types.RATE).addAll(message.rate);
                    });
                }
            }
        };
    }
}