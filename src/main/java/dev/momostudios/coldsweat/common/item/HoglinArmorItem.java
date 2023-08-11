package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.client.event.RegisterModels;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;

public class HoglinArmorItem extends ArmorItem
{
    public HoglinArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties)
    {   super(material, slot, properties);
    }

    @Override
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack stack, EquipmentSlotType armorSlot, A playerModel)
    {
        switch (armorSlot)
        {
            case HEAD  : return (A) RegisterModels.HOGLIN_HEADPIECE_MODEL.withModelBase((playerModel));
            case CHEST : return (A) RegisterModels.HOGLIN_TUNIC_MODEL.withModelBase((playerModel));
            case LEGS  : return (A) RegisterModels.HOGLIN_TROUSERS_MODEL.withModelBase(playerModel);
            case FEET  : return (A) RegisterModels.HOGLIN_HOOVES_MODEL.withModelBase(playerModel);
            default: return null;
        }
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer)
    {   return stack.getItem() == ModItems.HOGLIN_HEADPIECE;
    }
}
