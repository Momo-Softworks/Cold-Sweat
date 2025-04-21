package com.momosoftworks.coldsweat.common.item;


import com.momosoftworks.coldsweat.client.event.RegisterModels;
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
        {   return Client.getPlayerArmorModel();
        }
        else return Client.getRealArmorModel(entityLiving, stack, armorSlot);
    }

    public static final class Client
    {
        /**
         * Always returns empty, because the armor model is processed in {@link com.momosoftworks.coldsweat.client.renderer.layer.ChameleonArmorLayer}
         */
        public static <A extends BipedModel<?>> A getPlayerArmorModel()
        {   return (A) RegisterModels.EMPTY_ARMOR_MODEL;
        }

        public static <A extends BipedModel<?>> A getRealArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot)
        {
            switch (armorSlot)
            {
                case HEAD  : return (A) RegisterModels.CHAMELEON_HELMET_MODEL;
                case CHEST : return (A) RegisterModels.CHAMELEON_CHESTPLATE_MODEL;
                case LEGS  : return (A) RegisterModels.CHAMELEON_LEGGINGS_MODEL;
                case FEET  : return (A) RegisterModels.CHAMELEON_BOOTS_MODEL;
                default    : return (A) RegisterModels.EMPTY_ARMOR_MODEL;
            }
        }
    }
}
