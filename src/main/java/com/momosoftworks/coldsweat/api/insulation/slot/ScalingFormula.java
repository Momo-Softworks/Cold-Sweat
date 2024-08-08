package com.momosoftworks.coldsweat.api.insulation.slot;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import java.util.*;

public abstract class ScalingFormula
{
    Type scaling;

    protected ScalingFormula(Type scaling)
    {   this.scaling = scaling;
    }

    public abstract int getSlots(EquipmentSlotType slot, ItemStack stack);
    public abstract List<? extends Number> getValues();

    public Type getType()
    {   return scaling;
    }

    public static class Static extends ScalingFormula
    {
        Map<EquipmentSlotType, Integer> slots = new EnumMap<>(EquipmentSlotType.class);

        public Static(int head, int body, int legs, int feet)
        {
            super(Type.STATIC);
            slots.put(EquipmentSlotType.HEAD, head);
            slots.put(EquipmentSlotType.CHEST, body);
            slots.put(EquipmentSlotType.LEGS, legs);
            slots.put(EquipmentSlotType.FEET, feet);
        }

        @Override
        public int getSlots(EquipmentSlotType slot, ItemStack stack)
        {   return slots.getOrDefault(slot, 0);
        }

        @Override
        public List<? extends Number> getValues()
        {
            ArrayList<Integer> values = new ArrayList<>();
            values.add(0, slots.get(EquipmentSlotType.HEAD));
            values.add(1, slots.get(EquipmentSlotType.CHEST));
            values.add(2, slots.get(EquipmentSlotType.LEGS));
            values.add(3, slots.get(EquipmentSlotType.FEET));
            return values;
        }
    }

    public static class Dynamic extends ScalingFormula
    {
        double factor;
        double max;

        public Dynamic(Type scaling, double factor, double max)
        {   super(scaling);
            this.factor = factor;
            this.max = max;
        }

        @Override
        public int getSlots(EquipmentSlotType slot, ItemStack stack)
        {
            double protection = stack.getAttributeModifiers(slot).get(Attributes.ARMOR).stream().findFirst().map(mod -> mod.getAmount()).orElse(0.0);
            switch (scaling)
            {
                case LINEAR      : return (int) CSMath.clamp(Math.floor(protection * factor), 0, max);
                case EXPONENTIAL : return (int) CSMath.clamp(Math.floor(Math.pow(protection, factor)), 0, max);
                case LOGARITHMIC : return (int) CSMath.clamp(Math.floor(Math.sqrt(protection) * factor), 0, max);
                default : return  0;
            }
        }

        @Override
        public List<? extends Number> getValues()
        {   return Arrays.asList(factor, max);
        }
    }

    public enum Type implements StringRepresentable
    {
        STATIC("static"),
        LINEAR("linear"),
        EXPONENTIAL("exponential"),
        LOGARITHMIC("logarithmic");

        final String name;

        Type(String name)
        {   this.name = name;
        }

        public static final Codec<Type> CODEC = Codec.STRING.xmap(Type::byName, Type::getSerializedName);

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static Type byName(String name)
        {   for (Type type : values())
            {   if (type.name.equals(name))
                {   return type;
                }
            }
            throw new IllegalArgumentException("Unknown insulation scaling: " + name);
        }
    }
}
