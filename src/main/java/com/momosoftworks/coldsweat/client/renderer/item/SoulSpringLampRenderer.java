package com.momosoftworks.coldsweat.client.renderer.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.Callable;

public class SoulSpringLampRenderer extends ItemStackTileEntityRenderer implements Callable<ItemStackTileEntityRenderer>
{
    public static final ResourceLocation TEXTURE_FRAME = new ResourceLocation(ColdSweat.MOD_ID, "textures/item/soulspring_lamp/render/soulspring_lamp_frame.png");
    public static final ResourceLocation TEXTURE_0 = new ResourceLocation(ColdSweat.MOD_ID, "textures/item/soulspring_lamp/render/soulspring_lamp_0.png");
    public static final ResourceLocation TEXTURE_1 = new ResourceLocation(ColdSweat.MOD_ID, "textures/item/soulspring_lamp/render/soulspring_lamp_1.png");
    public static final ResourceLocation TEXTURE_2 = new ResourceLocation(ColdSweat.MOD_ID, "textures/item/soulspring_lamp/render/soulspring_lamp_2.png");
    public static final ResourceLocation TEXTURE_3 = new ResourceLocation(ColdSweat.MOD_ID, "textures/item/soulspring_lamp/render/soulspring_lamp_3.png");

    private static final int FULL_BRIGHT = 15728880;
    private static float TIME = 0;

    private SoulspringLampModel model;

    public SoulSpringLampRenderer()
    {   this.model = new SoulspringLampModel();
    }

    public static class SoulspringLampModel extends Model
    {
        private final ModelRenderer base;
        private final ModelRenderer heart;

        public SoulspringLampModel()
        {
            super(RenderType::entityTranslucent);
            texWidth = 64;
            texHeight = 32;

            base = new ModelRenderer(this);
            base.setPos(8.0F, 24.0F, -8.0F);
            base.texOffs(0, 11).addBox(-12.0F, -17.0F, 4.0F, 8.0F, 11.0F, 8.0F, 0.0F, false);
            base.texOffs(22, 21).addBox(-13.0F, -6.0F, 3.0F, 10.0F, 1.0F, 10.0F, 0.0F, false);
            base.texOffs(58, 21).addBox(-9.5F, -5.0F, 8.0F, 3.0F, 8.0F, 0.0F, 0.0F, false);
            base.texOffs(52, 18).addBox(-8.0F, -5.0F, 6.5F, 0.0F, 8.0F, 3.0F, 0.0F, false);
            base.texOffs(24, 0).addBox(-13.0F, -19.0F, 3.0F, 10.0F, 3.0F, 10.0F, 0.0F, false);
            base.texOffs(40, 13).addBox(-11.0F, -21.0F, 5.0F, 6.0F, 2.0F, 6.0F, 0.0F, false);
            base.texOffs(0, -7).addBox(-8.0F, -28.0F, 4.5F, 0.0F, 7.0F, 7.0F, 0.0F, false);

            heart = new ModelRenderer(this);
            heart.setPos(0.0F, 24.0F, 0.0F);
            heart.texOffs(14, 0).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, 0.0F, false);
        }

        @Override
        public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
        {}
    }

    @Override
    public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType displayContext, MatrixStack poseStack, IRenderTypeBuffer buffer, int light, int overlay)
    {
        if (stack.getItem() == ModItems.SOULSPRING_LAMP)
        {
            Minecraft mc = Minecraft.getInstance();
            boolean isFirstPerson = mc.options.getCameraType().isFirstPerson();
            double fuel = SoulspringLampItem.getFuel(stack);

            ResourceLocation texture = getTexture(stack);

            if (fuel > 0)
            {
                this.model.heart.y = -14.0F + (float) Math.sin(TIME / 8) * 1.2f;
                this.model.heart.xRot = CSMath.toRadians((TIME * 2) % 360);
                this.model.heart.yRot = CSMath.toRadians((TIME * 2 + 10) % 360);
                this.model.heart.zRot = CSMath.toRadians((TIME * 0.5 + 5) % 360);
            }
            else
            {
                this.model.heart.y = -14.0F;
                this.model.heart.yRot = 0;
                this.model.heart.xRot = 0;
                this.model.heart.zRot = 0;
            }

            float emission = SoulspringLampItem.isLit(stack) ? (float) CSMath.blend(0, 1, fuel, 0, 64) : 0;

            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            poseStack.pushPose();

            // Translate lamp correctly
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180));

            // Render frame
            IVertexBuilder frameVertexes = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE_FRAME));
            this.model.base.render(poseStack, frameVertexes, light, overlay);

            // Render heart
            poseStack.pushPose();
            double heartYOffset = isFirstPerson ? 1.65 : 1.55;
            float heartScale = isFirstPerson ? 0.925f : 0.9f;
            poseStack.translate(0, heartYOffset, 0);
            poseStack.scale(heartScale, heartScale, heartScale);
            // Render
            IVertexBuilder heartVtx = buffer.getBuffer(RenderType.entityTranslucent(texture));
            this.model.heart.render(poseStack, heartVtx, light, overlay, 1, 1, 1, 1-emission);
            // Emission
            this.model.heart.render(poseStack, heartVtx, FULL_BRIGHT, overlay, 1, 1, 1, emission);
            poseStack.popPose();

            // Render glass
            IVertexBuilder glassVtx = buffer.getBuffer(RenderType.entityTranslucent(texture));
            this.model.base.render(poseStack, glassVtx, light, overlay, 1, 1, 1, 1-emission);
            // Emission
            this.model.base.render(poseStack, glassVtx, FULL_BRIGHT, overlay, 1, 1, 1, emission);
            poseStack.popPose();
        }
        else
        {   super.renderByItem(stack, displayContext, poseStack, buffer, light, overlay);
        }
    }

    private static ResourceLocation getTexture(ItemStack lamp)
    {
        double fuel = SoulspringLampItem.getFuel(lamp);
        int state;
        CompoundNBT tag = lamp.getOrCreateTag();
        if (tag.getBoolean("Lit"))
        {
            state = fuel > 43 ? 3 :
                    fuel > 22 ? 2 : 1;
        }
        else state = 0;

        switch (state)
        {
            case 1 : return TEXTURE_1;
            case 2 : return TEXTURE_2;
            case 3 : return TEXTURE_3;
            default : return TEXTURE_0;
        }
    }

    @Override
    public ItemStackTileEntityRenderer call()
    {   return this;
    }

    @SubscribeEvent
    public static void tickTimer(TickEvent.RenderTickEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isPaused() && event.phase == TickEvent.Phase.START)
        {   TIME += mc.getDeltaFrameTime();
        }
    }
}
