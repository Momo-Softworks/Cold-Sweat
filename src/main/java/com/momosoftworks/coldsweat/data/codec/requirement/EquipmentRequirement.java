package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;

import java.util.Optional;

public class EquipmentRequirement
{
    private final ItemRequirement head;
    private final ItemRequirement chest;
    private final ItemRequirement legs;
    private final ItemRequirement feet;
    private final ItemRequirement mainHand;
    private final ItemRequirement offHand;

    public EquipmentRequirement(ItemRequirement head, ItemRequirement chest,
                                ItemRequirement legs, ItemRequirement feet,
                                ItemRequirement mainHand, ItemRequirement offHand)
    {
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.feet = feet;
        this.mainHand = mainHand;
        this.offHand = offHand;
    }
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

    public ItemRequirement head()
    {   return head;
    }
    public ItemRequirement chest()
    {   return chest;
    }
    public ItemRequirement legs()
    {   return legs;
    }
    public ItemRequirement feet()
    {   return feet;
    }
    public ItemRequirement mainHand()
    {   return mainHand;
    }
    public ItemRequirement offHand()
    {   return offHand;
    }

    public boolean test(Entity entity)
    {
        return entity instanceof LivingEntity
            && head.test(((LivingEntity) entity).getItemBySlot(EquipmentSlotType.HEAD), true)
            && chest.test(((LivingEntity) entity).getItemBySlot(EquipmentSlotType.CHEST), true)
            && legs.test(((LivingEntity) entity).getItemBySlot(EquipmentSlotType.LEGS), true)
            && feet.test(((LivingEntity) entity).getItemBySlot(EquipmentSlotType.FEET), true)
            && mainHand.test(((LivingEntity) entity).getMainHandItem(), true)
            && offHand.test(((LivingEntity) entity).getOffhandItem(), true);
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