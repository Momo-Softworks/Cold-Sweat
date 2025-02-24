package com.momosoftworks.coldsweat.common.item;


import com.momosoftworks.coldsweat.client.renderer.model.armor.ArmorModels;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;

public class ChameleonArmorItem extends ArmorItem
{
    public ChameleonArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties)
    {   super(material, slot, properties);
    }

    @Override
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack stack, EquipmentSlotType armorSlot, A playerModel)
    {
        if (entityLiving instanceof PlayerEntity)
        {   return (A) ArmorModels.EMPTY_ARMOR_MODEL;
        }
        else return getRealArmorModel(entityLiving, stack, armorSlot);
    }

    public <A extends BipedModel<?>> A getRealArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot)
    {
        switch (armorSlot)
        {
            case HEAD  : return (A) ArmorModels.CHAMELEON_HELMET_MODEL;
            case CHEST : return (A) ArmorModels.CHAMELEON_CHESTPLATE_MODEL;
            case LEGS  : return (A) ArmorModels.CHAMELEON_LEGGINGS_MODEL;
            case FEET  : return (A) ArmorModels.CHAMELEON_BOOTS_MODEL;
            default    : return (A) ArmorModels.EMPTY_ARMOR_MODEL;
        }
    }
}
