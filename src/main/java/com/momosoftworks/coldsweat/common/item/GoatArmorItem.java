package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.client.event.RegisterModels;
import com.momosoftworks.coldsweat.client.renderer.model.armor.GoatChestplateModel;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GoatArmorItem extends ArmorItem
{
    public GoatArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties)
    {   super(material, slot, properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack stack, EquipmentSlotType armorSlot, A playerModel)
    {
        switch (armorSlot)
        {
            case HEAD : return (A) RegisterModels.GOAT_HELMET_MODEL.withModelBase(playerModel);
            case CHEST :
            {
                GoatChestplateModel<?> model = RegisterModels.GOAT_CHESTPLATE_MODEL.withModelBase(playerModel);
                ModelRenderer fluff = model.fluff;
                float headPitch = entityLiving.getViewXRot(Minecraft.getInstance().getFrameTime());

                fluff.xRot = CSMath.toRadians(CSMath.clamp(headPitch, 0, 60f)) / 2;
                fluff.x = fluff.zRot * 2;

                return ((A) model);
            }
            case LEGS : return (A) RegisterModels.GOAT_LEGGINGS_MODEL.withModelBase(playerModel);
            case FEET : return (A) RegisterModels.GOAT_BOOTS_MODEL.withModelBase(playerModel);
            default : return null;
        }
    }
}
