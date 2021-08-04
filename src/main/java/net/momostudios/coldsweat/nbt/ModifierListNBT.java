package net.momostudios.coldsweat.nbt;

import net.minecraft.nbt.*;
import net.minecraft.util.text.ITextComponent;
import net.momostudios.coldsweat.temperature.modifier.TempModifier;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModifierListNBT extends CollectionNBT
{
    List<ObjectNBT> modifierCollection = new ArrayList<>();

    @Override
    public INBT set(int index, INBT object)
    {
        if (object instanceof ObjectNBT)
            modifierCollection.set(index, (ObjectNBT) object);

        return null;
    }

    @Override
    public void add(int index, INBT object) {}

    public void add(ObjectNBT object)
    {
        this.modifierCollection.add(object);
    }

    @Override
    public Object get(int index)
    {
        return modifierCollection.get(index);
    }

    public List<ObjectNBT> getModifiers()
    {
        return this.modifierCollection;
    }

    @Override
    public INBT remove(int index) { return null; }

    public void remove(ObjectNBT object)
    {
        this.modifierCollection.removeIf(modIter -> modIter.getClass().equals(object.getClass()));
    }

    @Override
    public boolean setNBTByIndex(int index, INBT nbt) {
        return false;
    }

    @Override
    public boolean addNBTByIndex(int index, INBT nbt) {
        return false;
    }

    @Override
    public byte getTagType() {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void write(DataOutput output) throws IOException {

    }

    @Override
    public byte getId() {
        return 0;
    }

    @Override
    public INBTType<?> getType() {
        return null;
    }

    @Override
    public INBT copy() {
        return null;
    }

    @Override
    public ITextComponent toFormattedComponent(String indentation, int indentDepth) {
        return null;
    }
}
