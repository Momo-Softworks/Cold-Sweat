package com.momosoftworks.coldsweat.common.item;

import com.jozufozu.flywheel.core.model.ModelPart;
import com.momosoftworks.coldsweat.client.renderer.model.armor.ArmorModels;
import com.momosoftworks.coldsweat.client.renderer.model.armor.LlamaParkaModel;
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

public class FurArmorItem extends ArmorItem
{
    public FurArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties)
    {   super(material, slot, properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack stack, EquipmentSlotType armorSlot, A playerModel)
    {
        switch (armorSlot)
        {
            case HEAD : return (A) ArmorModels.LLAMA_CAP_MODEL.withModelBase(playerModel);
            case CHEST :
            {
                LlamaParkaModel<?> model = ArmorModels.LLAMA_PARKA_MODEL.withModelBase(playerModel);
                ModelRenderer fluff = model.fluff;
                float headPitch = entityLiving.getViewXRot(Minecraft.getInstance().getFrameTime());

                fluff.xRot = CSMath.toRadians(CSMath.clamp(headPitch, 0, 60f)) / 2;
                fluff.x = fluff.zRot * 2;

                return ((A) model);
            }
            case LEGS : return (A) ArmorModels.LLAMA_PANTS_MODEL.withModelBase(playerModel);
            case FEET : return (A) ArmorModels.LLAMA_BOOTS_MODEL.withModelBase(playerModel);
            default : return null;
        }
    }
}
