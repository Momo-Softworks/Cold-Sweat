package com.momosoftworks.coldsweat.config.type;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemCarryTempData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CarriedItemTemperature implements NbtSerializable
{
    public final ItemRequirement item;
    public final List<Either<IntegerBounds, EquipmentSlotType>> slots;
    public final double temperature;
    public final Temperature.Trait trait;
    public final double maxEffect;
    public final EntityRequirement entityRequirement;

    public CarriedItemTemperature(ItemRequirement item, List<Either<IntegerBounds, EquipmentSlotType>> slots, double temperature,
                                  Temperature.Trait trait, double maxEffect, EntityRequirement entityRequirement)
    {
        this.item = item;
        this.slots = slots;
        this.temperature = temperature;
        this.trait = trait;
        this.maxEffect = maxEffect;
        this.entityRequirement = entityRequirement;
    }

    public static CarriedItemTemperature createFromData(ItemCarryTempData data)
    {
        return new CarriedItemTemperature(data.data, data.slots, data.temp,
                                          data.trait.orElse(Temperature.Trait.CORE),
                                          data.maxEffect.orElse(Double.MAX_VALUE),
                                          data.entityRequirement.orElse(EntityRequirement.NONE));
    }

    public boolean testEntity(LivingEntity entity)
    {   return entityRequirement.test(entity);
    }

    public boolean testSlot(ItemStack stack, @Nullable Integer slot, @Nullable EquipmentSlotType equipmentSlot)
    {
        if (!item.test(stack, true))
        {   return false;
        }
        if (slot == null && equipmentSlot == null)
        {   return false;
        }
        for (Either<IntegerBounds, EquipmentSlotType> either : slots)
        {
            if (slot != null && either.left().isPresent() && either.left().get().test(slot))
            {   return true;
            }
            else if (equipmentSlot != null && either.right().isPresent() && either.right().get().equals(equipmentSlot))
            {   return true;
            }
        }
        return false;
    }

    public String getSlotRangeName()
    {
        String[] strictType = {""};
        if (this.slots.size() == 1) this.slots.get(0).ifLeft(left ->
        {
            if (left.equals(IntegerBounds.NONE))
            {  strictType[0] = "inventory";
            }
            if (left.min == 36 && left.max == 44)
            {  strictType[0] = "hotbar";
            }
        });
        else if (this.slots.size() == 2
        && this.slots.get(0).right().map(right -> right == EquipmentSlotType.MAINHAND).orElse(false)
        && this.slots.get(1).right().map(right -> right == EquipmentSlotType.OFFHAND).orElse(false))
        {  strictType[0] = "hand";
        }

        return strictType[0];
    }

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.put("item", item.serialize());
        ListNBT slotsTag = new ListNBT();
        for (Either<IntegerBounds, EquipmentSlotType> either : slots)
        {
            CompoundNBT slotTag = new CompoundNBT();
            if (either.left().isPresent())
            {   slotTag.put("bounds", either.left().get().serialize());
            }
            else if (either.right().isPresent())
            {   slotTag.putString("slot", either.right().get().getName());
            }
            slotsTag.add(slotTag);
        }
        tag.put("slots", slotsTag);
        tag.putDouble("temperature", temperature);
        tag.putString("trait", trait.getSerializedName());
        tag.putDouble("maxEffect", maxEffect);
        tag.put("entity", entityRequirement.serialize());
        return tag;
    }

    public static CarriedItemTemperature deserialize(CompoundNBT tag)
    {
        ItemRequirement item = ItemRequirement.deserialize(tag.getCompound("item"));
        List<Either<IntegerBounds, EquipmentSlotType>> slots = new ArrayList<>();
        ListNBT slotsTag = tag.getList("slots", 10);
        for (int i = 0; i < slotsTag.size(); i++)
        {
            CompoundNBT slotTag = slotsTag.getCompound(i);
            if (slotTag.contains("bounds"))
            {   slots.add(Either.left(IntegerBounds.deserialize(slotTag.getCompound("bounds"))));
            }
            else if (slotTag.contains("slot"))
            {   slots.add(Either.right(EquipmentSlotType.byName(slotTag.getString("slot"))));
            }
        }
        double temperature = tag.getDouble("temperature");
        Temperature.Trait trait = Temperature.Trait.fromID(tag.getString("trait"));
        double maxEffect = tag.getDouble("maxEffect");
        EntityRequirement entityRequirement = EntityRequirement.deserialize(tag.getCompound("entity"));
        return new CarriedItemTemperature(item, slots, temperature, trait, maxEffect, entityRequirement);
    }
}
