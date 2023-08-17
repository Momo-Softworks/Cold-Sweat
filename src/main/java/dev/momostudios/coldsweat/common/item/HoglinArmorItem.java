package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.client.renderer.model.armor.ArmorModels;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HoglinArmorItem extends ArmorItem
{
    public HoglinArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties)
    {   super(material, slot, properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack stack, EquipmentSlotType armorSlot, A playerModel)
    {
        switch (armorSlot)
        {
            case HEAD  : return (A) ArmorModels.HOGLIN_HEADPIECE_MODEL.withModelBase((playerModel));
            case CHEST : return (A) ArmorModels.HOGLIN_TUNIC_MODEL.withModelBase((playerModel));
            case LEGS  : return (A) ArmorModels.HOGLIN_TROUSERS_MODEL.withModelBase(playerModel);
            case FEET  : return (A) ArmorModels.HOGLIN_HOOVES_MODEL.withModelBase(playerModel);
            default: return null;
        }
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer)
    {   return stack.getItem() == ModItems.HOGLIN_HEADPIECE;
    }
}
