package net.momostudios.coldsweat.temperature.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.momostudios.coldsweat.temperature.Temperature;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TemperatureCapability
{
    @CapabilityInject(Data.class)
    public static Capability<Data> CAPABILITY_TEMPERATURE;

    //Convert to/from NBT
    public static class Storage implements Capability.IStorage<Data>
    {
        @Override
        public INBT writeNBT(Capability<Data> capability, Data instance, Direction side)
        {
            CompoundNBT compound = new CompoundNBT();
            compound.putDouble("body_temperature", instance.getCoreTemperature().get());
            compound.putDouble("ambient_temperature", instance.getAmbientTemperature().get());
            return compound;
        }

        @Override
        public void readNBT(Capability<Data> capability, Data instance, Direction side, INBT nbt)
        {
            instance.setCoreTemperature(new Temperature(((CompoundNBT) nbt).getDouble("body_temperature")));
            instance.setAmbientTemperature(new Temperature(((CompoundNBT) nbt).getDouble("ambient_temperature")));
        }
    }

    public static class Data
    {
        /** These are the initial values for the player's body temperature and relative world temperature */
        private Temperature temperature = new Temperature(0);
        private Temperature ambientTemp = new Temperature(0.8);

        /** Sets the player's body temperature to the new value*/
        public void setCoreTemperature(Temperature tempIn)
        {
            this.temperature = tempIn;
        }
        /** Sets the world temperature relative to the player to the new value */
        public void setAmbientTemperature(Temperature tempIn)
        {
            this.ambientTemp = tempIn;
        }


        /** Adds the value to the player's body temperature*/
        public void addToCoreTemperature(double amount)
        {
            this.temperature.add(amount);
        }
        /** Adds the value to the world temperature relative to the player */
        public void addToAmbientTemperature(double amount)
        {
            this.ambientTemp.add(amount);
        }


        /** Returns a {@code Temperature} representing the player's body temperature */
        public Temperature getCoreTemperature()
        {
            return temperature;
        }
        /** Returns a {@code Temperature} representing the world temperature relative to the player */
        public Temperature getAmbientTemperature()
        {
            return ambientTemp;
        }
    }

    public static class Provider implements ICapabilitySerializable<INBT> {
        private Data tempIn = new Data();
        private Capability.IStorage<Data> storage = new Storage();

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
        {
            if (cap != null && cap.equals(CAPABILITY_TEMPERATURE))
                return LazyOptional.of(() -> tempIn).cast();
            else return LazyOptional.empty();
        }

        @Override
        public INBT serializeNBT() {
            return storage.writeNBT(CAPABILITY_TEMPERATURE, tempIn, null);
        }

        @Override
        public void deserializeNBT(INBT nbt) {
            storage.readNBT(CAPABILITY_TEMPERATURE, tempIn, null, nbt);
        }
    }
}
