package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public record EquipmentRequirement(Optional<ItemRequirement> head, Optional<ItemRequirement> chest,
                                   Optional<ItemRequirement> legs, Optional<ItemRequirement> feet,
                                   Optional<ItemRequirement> mainHand, Optional<ItemRequirement> offHand)
{
    public static final Codec<EquipmentRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemRequirement.CODEC.optionalFieldOf("head").forGetter(requirement -> requirement.head),
            ItemRequirement.CODEC.optionalFieldOf("chest").forGetter(requirement -> requirement.chest),
            ItemRequirement.CODEC.optionalFieldOf("legs").forGetter(requirement -> requirement.legs),
            ItemRequirement.CODEC.optionalFieldOf("feet").forGetter(requirement -> requirement.feet),
            ItemRequirement.CODEC.optionalFieldOf("main_hand").forGetter(requirement -> requirement.mainHand),
            ItemRequirement.CODEC.optionalFieldOf("off_hand").forGetter(requirement -> requirement.offHand)
    ).apply(instance, EquipmentRequirement::new));

    public boolean test(Entity entity)
    {
        return head.isEmpty() && chest.isEmpty() && legs.isEmpty() && feet.isEmpty() && mainHand.isEmpty() && offHand.isEmpty()
            || entity instanceof LivingEntity living
            && (head.isEmpty() || head.get().test(living.getItemBySlot(EquipmentSlot.HEAD)))
            && (chest.isEmpty() || chest.get().test(living.getItemBySlot(EquipmentSlot.CHEST)))
            && (legs.isEmpty() || legs.get().test(living.getItemBySlot(EquipmentSlot.LEGS)))
            && (feet.isEmpty() || feet.get().test(living.getItemBySlot(EquipmentSlot.FEET)))
            && (mainHand.isEmpty() || mainHand.get().test(living.getMainHandItem()))
            && (offHand.isEmpty() || offHand.get().test(living.getOffhandItem()));
    }

    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        head.ifPresent(requirement -> tag.put("head", requirement.serialize()));
        chest.ifPresent(requirement -> tag.put("chest", requirement.serialize()));
        legs.ifPresent(requirement -> tag.put("legs", requirement.serialize()));
        feet.ifPresent(requirement -> tag.put("feet", requirement.serialize()));
        mainHand.ifPresent(requirement -> tag.put("main_hand", requirement.serialize()));
        offHand.ifPresent(requirement -> tag.put("off_hand", requirement.serialize()));
        return tag;
    }

    public static EquipmentRequirement deserialize(CompoundTag tag)
    {
        return new EquipmentRequirement(
            tag.contains("head") ? Optional.of(ItemRequirement.deserialize(tag.getCompound("head"))) : Optional.empty(),
            tag.contains("chest") ? Optional.of(ItemRequirement.deserialize(tag.getCompound("chest"))) : Optional.empty(),
            tag.contains("legs") ? Optional.of(ItemRequirement.deserialize(tag.getCompound("legs"))) : Optional.empty(),
            tag.contains("feet") ? Optional.of(ItemRequirement.deserialize(tag.getCompound("feet"))) : Optional.empty(),
            tag.contains("main_hand") ? Optional.of(ItemRequirement.deserialize(tag.getCompound("main_hand"))) : Optional.empty(),
            tag.contains("off_hand") ? Optional.of(ItemRequirement.deserialize(tag.getCompound("off_hand"))) : Optional.empty()
        );
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {   return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {   return false;
        }

        EquipmentRequirement that = (EquipmentRequirement) obj;

        return head.equals(that.head) && chest.equals(that.chest) && legs.equals(that.legs) && feet.equals(that.feet) && mainHand.equals(that.mainHand) && offHand.equals(that.offHand);
    }

    @Override
    public String toString()
    {
        return "Equipment{" +
                "head=" + head +
                ", chest=" + chest +
                ", legs=" + legs +
                ", feet=" + feet +
                ", mainHand=" + mainHand +
                ", offHand=" + offHand +
                '}';
    }
}