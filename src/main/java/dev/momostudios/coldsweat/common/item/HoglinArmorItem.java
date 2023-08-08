package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.client.event.RegisterModels;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;

public class HoglinArmorItem extends ArmorItem
{
    public HoglinArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties)
    {   super(material, slot, properties);
    }

    /*@Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer)
    {
        consumer.accept(new IItemRenderProperties()
        {
            @Override
            public HumanoidModel<?> getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> playerModel)
            {
                RegisterModels.checkForInitModels();
                return switch (armorSlot)
                {
                    case HEAD -> RegisterModels.HOGLIN_HEADPIECE_MODEL;
                    case CHEST -> RegisterModels.HOGLIN_TUNIC_MODEL;
                    case LEGS -> RegisterModels.HOGLIN_TROUSERS_MODEL;
                    case FEET -> RegisterModels.HOGLIN_HOOVES_MODEL;
                    default -> null;
                };
            }
        });
    }*/

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer)
    {   return stack.getItem() == ModItems.HOGLIN_HEADPIECE;
    }
}
