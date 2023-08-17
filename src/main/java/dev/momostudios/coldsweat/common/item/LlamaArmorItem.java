package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.client.renderer.model.armor.ArmorModels;
import dev.momostudios.coldsweat.client.renderer.model.armor.LlamaParkaModel;
import dev.momostudios.coldsweat.util.math.CSMath;
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

public class LlamaArmorItem extends ArmorItem
{
    public LlamaArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties)
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
                float headYaw = CSMath.blend(entityLiving.yRotO, entityLiving.yRot, Minecraft.getInstance().getFrameTime(), 0, 1);
                float bodyYaw = entityLiving.yBodyRot;
                float netHeadYaw = CSMath.clamp(headYaw - bodyYaw, -30, 30);

                fluff.xRot = CSMath.toRadians(CSMath.clamp(headPitch, 0, 60f)) / 2;
                fluff.zRot = -CSMath.toRadians(netHeadYaw) * fluff.xRot / 1;
                fluff.x = fluff.zRot * 2;
                return (A) model;
            }
            case LEGS : return (A) ArmorModels.LLAMA_PANTS_MODEL.withModelBase(playerModel);
            case FEET : return (A) ArmorModels.LLAMA_BOOTS_MODEL.withModelBase(playerModel);
            default : return null;
        }
    }
}
