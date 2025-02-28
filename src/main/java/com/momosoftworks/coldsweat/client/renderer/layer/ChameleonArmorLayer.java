package com.momosoftworks.coldsweat.client.renderer.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.client.renderer.model.armor.*;
import com.momosoftworks.coldsweat.common.item.ChameleonArmorItem;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ChameleonArmorLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A>
{
    public static final ResourceLocation GREEN_LAYER_1_LOCATION = new ResourceLocation(ColdSweat.MOD_ID, "textures/models/armor/chameleon_layer_1.png");
    public static final ResourceLocation GREEN_LAYER_2_LOCATION = new ResourceLocation(ColdSweat.MOD_ID, "textures/models/armor/chameleon_layer_2.png");
    public static final ResourceLocation RED_LAYER_1_LOCATION = new ResourceLocation(ColdSweat.MOD_ID, "textures/models/armor/chameleon_layer_1_red.png");
    public static final ResourceLocation RED_LAYER_2_LOCATION = new ResourceLocation(ColdSweat.MOD_ID, "textures/models/armor/chameleon_layer_2_red.png");
    public static final ResourceLocation BLUE_LAYER_1_LOCATION = new ResourceLocation(ColdSweat.MOD_ID, "textures/models/armor/chameleon_layer_1_blue.png");
    public static final ResourceLocation BLUE_LAYER_2_LOCATION = new ResourceLocation(ColdSweat.MOD_ID, "textures/models/armor/chameleon_layer_2_blue.png");

    public ChameleonArmorLayer(IEntityRenderer<T, M> renderer)
    {   super(renderer, null, null);
    }

    @Override
    public void render(MatrixStack poseStack, IRenderTypeBuffer buffer, int light, T entity,
                       float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks,
                       float pNetHeadYaw, float pHeadPitch)
    {
        ArmorModels.CHAMELEON_HELMET_MODEL = new ChameleonHelmetModel<>();
        ArmorModels.CHAMELEON_CHESTPLATE_MODEL = new ChameleonChestplateModel<>();
        ArmorModels.CHAMELEON_LEGGINGS_MODEL = new ChameleonLeggingsModel<>();
        ArmorModels.CHAMELEON_BOOTS_MODEL = new ChameleonBootsModel<>();
        this.renderArmorPiece(poseStack, buffer, entity, EquipmentSlotType.CHEST, light);
        this.renderArmorPiece(poseStack, buffer, entity, EquipmentSlotType.LEGS, light);
        this.renderArmorPiece(poseStack, buffer, entity, EquipmentSlotType.FEET, light);
        this.renderArmorPiece(poseStack, buffer, entity, EquipmentSlotType.HEAD, light);
    }

    protected void renderArmorPiece(MatrixStack poseStack, IRenderTypeBuffer buffer, T livingEntity, EquipmentSlotType slot, int light)
    {
        ItemStack itemstack = livingEntity.getItemBySlot(slot);
        Item item = itemstack.getItem();
        if (item instanceof ChameleonArmorItem)
        {
            ChameleonArmorItem armorItem = (ChameleonArmorItem) item;

            if (MobEntity.getEquipmentSlotForItem(itemstack) == slot)
            {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                // Get model data
                boolean isInnerModel = this.usesInnerModel(slot);
                BipedModel<T> model = ChameleonArmorItem.Client.getRealArmorModel(livingEntity, itemstack, slot);
                // Set transforms & visibility properties
                this.getParentModel().copyPropertiesTo(model);
                this.setPartVisibilities(model, slot);
                this.renderModel(poseStack, buffer, light, armorItem, model, isInnerModel, 1.0f, 1.0f, 1.0f, 1.0f, Color.GREEN.getLayer(slot));
                // Render overlay texture (red/blue)
                double adaptiveFactor = AdaptiveInsulation.getFactorFromNBT(itemstack);
                ResourceLocation overlay = adaptiveFactor < 0 ? Color.BLUE.getLayer(slot) : Color.RED.getLayer(slot);
                float alpha = (float) Math.abs(adaptiveFactor);
                this.renderModel(poseStack, buffer, light, armorItem, model, isInnerModel, 1.0f, 1.0f, 1.0f, alpha, overlay);
                // Render enchantment glint
                if (itemstack.hasFoil())
                {   this.renderGlint(poseStack, buffer, light, model);
                }
                RenderSystem.disableBlend();
            }
        }
    }

    protected boolean usesInnerModel(EquipmentSlotType slot)
    {   return slot == EquipmentSlotType.LEGS;
    }

    protected void renderModel(MatrixStack poseStack, IRenderTypeBuffer buffer, int light, ArmorItem armorItem, Model model,
                               boolean showGlint, float red, float green, float blue, float alpha, ResourceLocation armorResource)
    {
        IVertexBuilder vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(armorResource));
        model.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
    }

    protected void renderGlint(MatrixStack poseStack, IRenderTypeBuffer buffer, int packedLight, Model model)
    {
        model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorEntityGlint()), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected void setPartVisibilities(BipedModel<T> pModel, EquipmentSlotType pSlot)
    {
        pModel.setAllVisible(false);
        switch (pSlot)
        {
            case HEAD:
                pModel.head.visible = true;
                pModel.hat.visible = true;
                break;
            case CHEST:
                pModel.body.visible = true;
                pModel.rightArm.visible = true;
                pModel.leftArm.visible = true;
                break;
            case LEGS:
                pModel.body.visible = true;
                pModel.rightLeg.visible = true;
                pModel.leftLeg.visible = true;
                break;
            case FEET:
                pModel.rightLeg.visible = true;
                pModel.leftLeg.visible = true;
                break;
        }
    }

    public enum Color
    {
        GREEN(GREEN_LAYER_1_LOCATION, GREEN_LAYER_2_LOCATION),
        RED(RED_LAYER_1_LOCATION, RED_LAYER_2_LOCATION),
        BLUE(BLUE_LAYER_1_LOCATION, BLUE_LAYER_2_LOCATION);

        private final ResourceLocation layer1;
        private final ResourceLocation layer2;

        Color(ResourceLocation layer1, ResourceLocation layer2)
        {
            this.layer1 = layer1;
            this.layer2 = layer2;
        }

        public ResourceLocation getLayer1()
        {   return layer1;
        }

        public ResourceLocation getLayer2()
        {   return layer2;
        }

        public ResourceLocation getLayer(EquipmentSlotType slot)
        {   return slot == EquipmentSlotType.LEGS ? layer2 : layer1;
        }
    }
}
