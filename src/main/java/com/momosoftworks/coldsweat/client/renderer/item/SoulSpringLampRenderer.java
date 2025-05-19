package com.momosoftworks.coldsweat.client.renderer.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SoulSpringLampRenderer extends BlockEntityWithoutLevelRenderer
{
    public static final ResourceLocation TEXTURE_FRAME = new ResourceLocation(ColdSweat.MOD_ID, "textures/item/soulspring_lamp/render/soulspring_lamp_frame.png");
    public static final ResourceLocation TEXTURE_0 = new ResourceLocation(ColdSweat.MOD_ID, "textures/item/soulspring_lamp/render/soulspring_lamp_0.png");
    public static final ResourceLocation TEXTURE_1 = new ResourceLocation(ColdSweat.MOD_ID, "textures/item/soulspring_lamp/render/soulspring_lamp_1.png");
    public static final ResourceLocation TEXTURE_2 = new ResourceLocation(ColdSweat.MOD_ID, "textures/item/soulspring_lamp/render/soulspring_lamp_2.png");
    public static final ResourceLocation TEXTURE_3 = new ResourceLocation(ColdSweat.MOD_ID, "textures/item/soulspring_lamp/render/soulspring_lamp_3.png");

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ColdSweat.MOD_ID, "soulspring_lamp"), "main");

    private static final int FULL_BRIGHT = 15728880;
    private static float TIME = 0;

    private final ModelPart base;
    private final ModelPart heart;

    public SoulSpringLampRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSel)
    {
        super(dispatcher, modelSel);
        ModelPart root = modelSel.bakeLayer(LAYER_LOCATION);
        this.base = root.getChild("base");
        this.heart = root.getChild("heart");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 11).addBox(-12.0F, -17.0F, 4.0F, 8.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(22, 21).addBox(-13.0F, -6.0F, 3.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(58, 21).addBox(-9.5F, -5.0F, 8.0F, 3.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(52, 18).addBox(-8.0F, -5.0F, 6.5F, 0.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(24, 0).addBox(-13.0F, -19.0F, 3.0F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(40, 13).addBox(-11.0F, -21.0F, 5.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, -7).addBox(-8.0F, -28.0F, 4.5F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition heart = partdefinition.addOrReplaceChild("heart", CubeListBuilder.create().texOffs(14, 0).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType displayContext, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        if (stack.is(ModItems.SOULSPRING_LAMP))
        {
            Minecraft mc = Minecraft.getInstance();
            boolean isFirstPerson = mc.options.getCameraType().isFirstPerson();
            double fuel = SoulspringLampItem.getFuel(stack);

            ResourceLocation texture = getTexture(stack);

            if (fuel > 0)
            {
                heart.y = -14.0F + (float) Math.sin(TIME / 8) * 1.2f;
                heart.xRot = CSMath.toRadians((TIME * 2) % 360);
                heart.yRot = CSMath.toRadians((TIME * 2 + 10) % 360);
                heart.zRot = CSMath.toRadians((TIME * 0.5 + 5) % 360);
            }
            else
            {
                heart.y = -14.0F;
                heart.yRot = 0;
                heart.xRot = 0;
                heart.zRot = 0;
            }

            float emission = SoulspringLampItem.isLit(stack) ? (float) CSMath.blend(0, 1, fuel, 0, 64) : 0;

            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            poseStack.pushPose();

            // Translate lamp correctly
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180));

            // Render frame
            VertexConsumer frameVertexes = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE_FRAME));
            this.base.render(poseStack, frameVertexes, light, overlay);

            // Render heart
            poseStack.pushPose();
            double heartYOffset = isFirstPerson ? 1.65 : 1.55;
            float heartScale = isFirstPerson ? 0.925f : 0.9f;
            poseStack.translate(0, heartYOffset, 0);
            poseStack.scale(heartScale, heartScale, heartScale);
            // Render
            VertexConsumer heartVtx = buffer.getBuffer(RenderType.entityTranslucent(texture));
            this.heart.render(poseStack, heartVtx, light, overlay, 1, 1, 1, 1-emission);
            // Emission
            this.heart.render(poseStack, heartVtx, FULL_BRIGHT, overlay, 1, 1, 1, emission);
            poseStack.popPose();

            // Render glass
            VertexConsumer glassVtx = buffer.getBuffer(RenderType.entityTranslucent(texture));
            this.base.render(poseStack, glassVtx, light, overlay, 1, 1, 1, 1-emission);
            // Emission
            this.base.render(poseStack, glassVtx, FULL_BRIGHT, overlay, 1, 1, 1, emission);
            poseStack.popPose();
        }
        else
        {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            super.renderByItem(stack, displayContext, poseStack, buffer, light, overlay);
        }
    }

    private static ResourceLocation getTexture(ItemStack lamp)
    {
        double fuel = SoulspringLampItem.getFuel(lamp);
        int state;
        CompoundTag tag = lamp.getOrCreateTag();
        if (tag.getBoolean("Lit"))
        {
            state = fuel > 43 ? 3 :
                    fuel > 22 ? 2 : 1;
        }
        else state = 0;

        return switch (state)
        {
            case 1 -> TEXTURE_1;
            case 2 -> TEXTURE_2;
            case 3 -> TEXTURE_3;
            default -> TEXTURE_0;
        };
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
