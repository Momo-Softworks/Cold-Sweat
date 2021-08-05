package net.momostudios.coldsweat.nbt;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.INBTType;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ObjectNBT implements INBT {

    public Object object;
    public static final INBTType<ObjectNBT> TYPE = new INBTType<ObjectNBT>()
    {
        public ObjectNBT readNBT(DataInput input, int depth, NBTSizeTracker accounter) throws IOException
        {
            accounter.read(256L);
            return ObjectNBT.valueOf(input.readLong());
        }

        public String getName() {
            return "OBJECT";
        }

        public String getTagName() {
            return "TAG_Object";
        }

        public boolean isPrimitive() {
            return false;
        }
    };

    public ObjectNBT(Object object)
    {
        this.object = object;
    }

    public static ObjectNBT valueOf(Object value)
    {
        return new ObjectNBT(value);
    }

    @Override
    public void write(DataOutput output) throws IOException {

    }

    @Override
    public byte getId() {
        return 9;
    }

    @Override
    public INBTType<?> getType() {
        return TYPE;
    }

    @Override
    public INBT copy() {
        return new ObjectNBT(object);
    }

    @Override
    public ITextComponent toFormattedComponent(String indentation, int indentDepth) {
        return new StringTextComponent(indentation + indentDepth);
    }
}