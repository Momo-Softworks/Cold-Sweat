package com.momosoftworks.coldsweat.api.insulation.slot;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import java.util.*;

public abstract class ScalingFormula
{
    public static Codec<ScalingFormula> getCodec()
    {
        return Codec.either(Static.CODEC, Dynamic.CODEC)
                    .xmap(either -> either.map(left -> left, right -> right),
                          scaling -> scaling instanceof Static ? Either.left((Static) scaling) : Either.right((Dynamic) scaling));
    }

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
        public static final Codec<Static> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("head").forGetter(s -> s.slots.getOrDefault(EquipmentSlotType.HEAD, 0)),
                Codec.INT.fieldOf("body").forGetter(s -> s.slots.getOrDefault(EquipmentSlotType.CHEST, 0)),
                Codec.INT.fieldOf("legs").forGetter(s -> s.slots.getOrDefault(EquipmentSlotType.LEGS, 0)),
                Codec.INT.fieldOf("feet").forGetter(s -> s.slots.getOrDefault(EquipmentSlotType.FEET, 0))
        ).apply(instance, Static::new));

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
            return Arrays.asList(slots.get(EquipmentSlotType.HEAD),
                                 slots.get(EquipmentSlotType.CHEST),
                                 slots.get(EquipmentSlotType.LEGS),
                                 slots.get(EquipmentSlotType.FEET));
        }
    }

    public static class Dynamic extends ScalingFormula
    {
        public static final Codec<Dynamic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Type.CODEC.fieldOf("scaling").forGetter(Dynamic::getType),
                Codec.DOUBLE.fieldOf("factor").forGetter(s -> s.factor),
                Codec.DOUBLE.fieldOf("max").forGetter(s -> s.max)
        ).apply(instance, Dynamic::new));

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

        public static final Codec<Type> CODEC = ExtraCodecs.enumIgnoreCase(values());

        @Override
        public String getSerializedName()
        {   return name;
        }

        public static Type byName(String name)
        {   return EnumHelper.byName(values(), name);
        }
    }
}
