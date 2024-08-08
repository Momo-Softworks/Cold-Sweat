package com.momosoftworks.coldsweat.api.insulation.slot;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class ScalingFormula
{
    Type scaling;

    protected ScalingFormula(Type scaling)
    {   this.scaling = scaling;
    }

    public abstract int getSlots(EquipmentSlot slot, ItemStack stack);
    public abstract List<? extends Number> getValues();

    public Type getType()
    {   return scaling;
    }

    public static class Static extends ScalingFormula
    {
        Map<EquipmentSlot, Integer> slots = new EnumMap<>(EquipmentSlot.class);

        public Static(int head, int body, int legs, int feet)
        {
            super(Type.STATIC);
            slots.put(EquipmentSlot.HEAD, head);
            slots.put(EquipmentSlot.CHEST, body);
            slots.put(EquipmentSlot.LEGS, legs);
            slots.put(EquipmentSlot.FEET, feet);
        }

        @Override
        public int getSlots(EquipmentSlot slot, ItemStack stack)
        {   return slots.getOrDefault(slot, 0);
        }

        @Override
        public List<? extends Number> getValues()
        {
            ArrayList<Integer> values = new ArrayList<>();
            values.add(0, slots.get(EquipmentSlot.HEAD));
            values.add(1, slots.get(EquipmentSlot.CHEST));
            values.add(2, slots.get(EquipmentSlot.LEGS));
            values.add(3, slots.get(EquipmentSlot.FEET));
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
        public int getSlots(EquipmentSlot slot, ItemStack stack)
        {
            double protection = stack.getAttributeModifiers(slot).get(Attributes.ARMOR).stream().findFirst().map(mod -> mod.getAmount()).orElse(0.0);
            return switch (scaling)
            {
                case LINEAR      -> (int) CSMath.clamp(Math.floor(protection * factor), 0, max);
                case EXPONENTIAL -> (int) CSMath.clamp(Math.floor(Math.pow(protection, factor)), 0, max);
                case LOGARITHMIC -> (int) CSMath.clamp(Math.floor(Math.sqrt(protection) * factor), 0, max);
                default -> 0;
            };
        }

        @Override
        public List<? extends Number> getValues()
        {   return List.of(factor, max);
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
