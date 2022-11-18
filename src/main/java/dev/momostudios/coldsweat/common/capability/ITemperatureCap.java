package dev.momostudios.coldsweat.common.capability;

import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface ITemperatureCap
{
    double getTemp(Temperature.Type type);
    void setTemp(Temperature.Type type, double value);
    List<TempModifier> getModifiers(Temperature.Type type);
    boolean hasModifier(Temperature.Type type, Class<? extends TempModifier> mod);
    void clearModifiers(Temperature.Type type);
    void copy(ITemperatureCap cap);
    void tick(Player player);
    void tickDummy(Player player);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag tag);
}