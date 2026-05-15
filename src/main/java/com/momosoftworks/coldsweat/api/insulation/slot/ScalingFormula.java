package com.momosoftworks.coldsweat.api.insulation.slot;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

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

    public abstract int getSlots(EquipmentSlot slot, ItemStack stack);
    public abstract List<? extends Number> getValues();

    public Type getType()
    {   return scaling;
    }

    public static class Static extends ScalingFormula
    {
        public static final Codec<Static> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("head").forGetter(s -> s.slots.getOrDefault(EquipmentSlot.HEAD, 0)),
                Codec.INT.fieldOf("body").forGetter(s -> s.slots.getOrDefault(EquipmentSlot.CHEST, 0)),
                Codec.INT.fieldOf("legs").forGetter(s -> s.slots.getOrDefault(EquipmentSlot.LEGS, 0)),
                Codec.INT.fieldOf("feet").forGetter(s -> s.slots.getOrDefault(EquipmentSlot.FEET, 0))
        ).apply(instance, Static::new));

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
            return List.of(slots.get(EquipmentSlot.HEAD),
                           slots.get(EquipmentSlot.CHEST),
                           slots.get(EquipmentSlot.LEGS),
                           slots.get(EquipmentSlot.FEET));
        }
    }

    public static class Dynamic extends ScalingFormula
    {
        public static final Codec<Dynamic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Type.CODEC.fieldOf("scaling").forGetter(Dynamic::getType),
                ExtraCodecs.DOUBLE.fieldOf("factor").forGetter(s -> s.factor),
                ExtraCodecs.DOUBLE.fieldOf("max").forGetter(s -> s.max)
        ).apply(instance, Dynamic::new));

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
            double protection = stack.getAttributeModifiers().modifiers().stream().filter(entry -> entry.attribute() == Attributes.ARMOR).findFirst().map(entry -> entry.modifier().amount()).orElse(0.0);
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
