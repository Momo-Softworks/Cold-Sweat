package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.client.event.RegisterModels;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;

public class HoglinArmorItem extends ArmorItem
{
    public HoglinArmorItem(ArmorMaterial material, EquipmentSlot slot, Properties properties)
    {   super(material, slot, properties);
    }

    @Override
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
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer)
    {   return stack.is(ModItems.HOGLIN_HEADPIECE);
    }
}
