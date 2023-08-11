package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.client.renderer.model.armor.ArmorModels;
import dev.momostudios.coldsweat.client.renderer.model.armor.GoatParkaModel;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;

public class GoatArmorItem extends ArmorItem
{
    public GoatArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties)
    {   super(material, slot, properties);
    }

    @Override
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack stack, EquipmentSlotType armorSlot, A playerModel)
    {
        switch (armorSlot)
        {
            case HEAD : return (A) ArmorModels.GOAT_CAP_MODEL.withModelBase(playerModel);
            case CHEST :
            {
                GoatParkaModel<?> model = ArmorModels.GOAT_PARKA_MODEL.withModelBase(playerModel);
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
            case LEGS : return (A) ArmorModels.GOAT_PANTS_MODEL.withModelBase(playerModel);
            case FEET : return (A) ArmorModels.GOAT_BOOTS_MODEL.withModelBase(playerModel);
            default : return null;
        }
    }
}
