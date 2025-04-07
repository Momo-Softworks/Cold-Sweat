package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public record EquipmentRequirement(ItemRequirement head, ItemRequirement chest,
                                   ItemRequirement legs, ItemRequirement feet,
                                   ItemRequirement mainHand, ItemRequirement offHand)
{
    public static final Codec<EquipmentRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemRequirement.CODEC.optionalFieldOf("head", ItemRequirement.NONE).forGetter(requirement -> requirement.head),
            ItemRequirement.CODEC.optionalFieldOf("chest", ItemRequirement.NONE).forGetter(requirement -> requirement.chest),
            ItemRequirement.CODEC.optionalFieldOf("legs", ItemRequirement.NONE).forGetter(requirement -> requirement.legs),
            ItemRequirement.CODEC.optionalFieldOf("feet", ItemRequirement.NONE).forGetter(requirement -> requirement.feet),
            ItemRequirement.CODEC.optionalFieldOf("mainhand", ItemRequirement.NONE).forGetter(requirement -> requirement.mainHand),
            ItemRequirement.CODEC.optionalFieldOf("offhand", ItemRequirement.NONE).forGetter(requirement -> requirement.offHand)
    ).apply(instance, EquipmentRequirement::new));

    public static final EquipmentRequirement NONE = new EquipmentRequirement(ItemRequirement.NONE, ItemRequirement.NONE,
                                                                             ItemRequirement.NONE, ItemRequirement.NONE,
                                                                             ItemRequirement.NONE, ItemRequirement.NONE);

    public boolean test(Entity entity)
    {
        return this.equals(NONE)
            || (entity instanceof LivingEntity living
            && testSlot(EquipmentSlot.HEAD, living)
            && testSlot(EquipmentSlot.CHEST, living)
            && testSlot(EquipmentSlot.LEGS, living)
            && testSlot(EquipmentSlot.FEET, living)
            && testSlot(EquipmentSlot.MAINHAND, living)
            && testSlot(EquipmentSlot.OFFHAND, living));
    }

    private boolean testSlot(EquipmentSlot slot, LivingEntity entity)
    {
        ItemRequirement requirement = switch (slot)
        {
            case HEAD -> head;
            case CHEST -> chest;
            case LEGS -> legs;
            case FEET -> feet;
            case MAINHAND -> mainHand;
            case OFFHAND -> offHand;
            default -> ItemRequirement.NONE;
        };
        return requirement.test(entity.getItemBySlot(slot), true);
    }

    @Override
    public String toString()
    {   return CODEC.encodeStart(JsonOps.INSTANCE, this).result().map(Object::toString).orElse("serialize_failed");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        EquipmentRequirement that = (EquipmentRequirement) obj;
        return head.equals(that.head)
            && chest.equals(that.chest)
            && legs.equals(that.legs)
            && feet.equals(that.feet)
            && mainHand.equals(that.mainHand)
            && offHand.equals(that.offHand);
    }
}