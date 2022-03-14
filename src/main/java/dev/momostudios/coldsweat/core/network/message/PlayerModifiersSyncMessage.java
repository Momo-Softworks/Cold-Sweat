package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.common.capability.ModCapabilities;
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
    public List<TempModifier> world;
    public List<TempModifier> core;
    public List<TempModifier> base;
    public List<TempModifier> rate;
    public List<TempModifier> hottest;
    public List<TempModifier> coldest;

    public PlayerModifiersSyncMessage(List<TempModifier> world, List<TempModifier> core, List<TempModifier> base, List<TempModifier> rate, List<TempModifier> hottest, List<TempModifier> coldest)
    {
        this.world = world;
        this.core = core;
        this.base = base;
        this.rate = rate;
        this.hottest = hottest;
        this.coldest = coldest;
    }

    public static void encode(PlayerModifiersSyncMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeNbt(writeToNBT(message, Temperature.Types.WORLD));
        buffer.writeNbt(writeToNBT(message, Temperature.Types.CORE));
        buffer.writeNbt(writeToNBT(message, Temperature.Types.BASE));
        buffer.writeNbt(writeToNBT(message, Temperature.Types.RATE));
        buffer.writeNbt(writeToNBT(message, Temperature.Types.HOTTEST));
        buffer.writeNbt(writeToNBT(message, Temperature.Types.COLDEST));
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

    private static CompoundTag writeToNBT(PlayerModifiersSyncMessage message, Temperature.Types type)
    {
        CompoundTag nbt = new CompoundTag();
        List<TempModifier> referenceList =
                type == Temperature.Types.WORLD   ? message.world :
                type == Temperature.Types.CORE    ? message.core :
                type == Temperature.Types.BASE    ? message.base :
                type == Temperature.Types.RATE    ? message.rate :
                type == Temperature.Types.HOTTEST ? message.hottest :
                type == Temperature.Types.COLDEST ? message.coldest :
                new ArrayList<>(0);

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
        context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> syncTemperature(message)));

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

                        cap.clearModifiers(Temperature.Types.HOTTEST);
                        cap.getModifiers(Temperature.Types.HOTTEST).addAll(message.hottest);

                        cap.clearModifiers(Temperature.Types.COLDEST);
                        cap.getModifiers(Temperature.Types.COLDEST).addAll(message.coldest);
                    });
                }
            }
        };
    }
}