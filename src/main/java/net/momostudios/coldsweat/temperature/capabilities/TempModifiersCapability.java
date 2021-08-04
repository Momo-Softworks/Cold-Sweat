package net.momostudios.coldsweat.temperature.capabilities;

import net.minecraft.nbt.*;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.momostudios.coldsweat.nbt.ObjectNBT;
import net.momostudios.coldsweat.temperature.modifier.BiomeTempModifier;
import net.momostudios.coldsweat.temperature.modifier.DepthTempModifier;
import net.momostudios.coldsweat.temperature.modifier.TempModifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TempModifiersCapability
{
    @CapabilityInject(Data.class)
    public static Capability<Data> CAPABILITY_TEMP_MODIFIERS;

    public static class Data
    {
        TempModifier[] test = {new BiomeTempModifier(), new DepthTempModifier()};
        private List<TempModifier> modifiers = Arrays.asList(test);

        public void clear()
        {
            modifiers.clear();
        }

        public void set(List<TempModifier> modifiersIn)
        {
            modifiers = modifiersIn;
        }

        //Add an instance of the given modifier if the LivingEntity doesn't already have it
        public void add(TempModifier modifier)
        {
            for (TempModifier modIter : modifiers)
            {
                if (modIter.getClass() == modIter.getClass())
                    return;
            }
            modifiers.add(modifier);
        }

        //Remove all instances of the given modifier from the LivingEntity
        public void remove(TempModifier modifier)
        {
            modifiers.removeIf(modIter -> modIter.getClass() == modIter.getClass());
        }

        public List<TempModifier> getModifiers()
        {
            return this.modifiers;
        }

        public boolean hasModifier(TempModifier modifier)
        {
            for (TempModifier modIter : modifiers)
            {
                if (modIter.getClass().equals(modifier.getClass()))
                    return true;
            }
            return false;
        }
    }

    //Convert to/from NBT
    public static class Storage implements Capability.IStorage<Data>
    {
        @Override
        public INBT writeNBT(Capability<Data> capability, Data instance, Direction side)
        {
            CompoundNBT compound = new CompoundNBT();
            ListNBT modList = new ListNBT();
            if (instance.getModifiers() != null)
            {
                for (TempModifier iter : instance.getModifiers()) {
                    modList.add(new ObjectNBT(iter));
                    System.out.println("Object is " + new ObjectNBT(iter) + "of type " + iter);
                }
                compound.put("tempModifiers", modList);
                //System.err.println("The CompoundNBT is " + compound.get("temperature_modifiers"));
                //System.err.println("The CompoundNBT should be " + modList);
            }
            return compound;
        }

        @Override
        public void readNBT(Capability<Data> capability, Data instance, Direction side, INBT nbt)
        {
            if (((CompoundNBT) nbt).get("tempModifiers") != null)
            {
                List<INBT> nbtList = ((ListNBT) ((CompoundNBT) nbt).get("tempModifiers")).subList(0, ((CompoundNBT) nbt).size());
                List<TempModifier> modifiers = new ArrayList<>();
                for (INBT iter : nbtList) {
                    if (iter instanceof ObjectNBT && ((ObjectNBT) iter).object.getClass().equals(TempModifier.class)) {
                        modifiers.add((TempModifier) ((ObjectNBT) iter).object);
                    }
                }
                instance.set(modifiers);
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<INBT>
    {
        private Data modifiers = new Data();
        private Capability.IStorage<Data> storage = new Storage();

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
        {
            if (cap != null && cap.equals(CAPABILITY_TEMP_MODIFIERS)) return LazyOptional.of(() -> modifiers).cast();
            else return LazyOptional.empty();
        }

        @Override
        public INBT serializeNBT()
        {
            return storage.writeNBT(CAPABILITY_TEMP_MODIFIERS, modifiers, null);
        }

        @Override
        public void deserializeNBT(INBT nbt)
        {
            storage.readNBT(CAPABILITY_TEMP_MODIFIERS, modifiers, null, nbt);
        }
    }
}
