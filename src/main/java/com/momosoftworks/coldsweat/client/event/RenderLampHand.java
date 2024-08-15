package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderLampHand
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHandRender(RenderHandEvent event)
    {
        if (event.getItemStack().getItem() == ModItems.SOULSPRING_LAMP)
        {
            event.setCanceled(true);

            PoseStack ms = event.getPoseStack();
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            boolean isRightHand = EntityHelper.getArmFromHand(event.getHand(), player) == HumanoidArm.RIGHT;
            PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
            ItemInHandRenderer handRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;

            ms.pushPose();

            // Idle position on the screen
            ms.translate(0, 0.2, -0.3);

            // Swing animation
            ms.pushPose();
            float swingProgress = event.getSwingProgress();
            float equipProgress = event.getEquipProgress();
            float handX = isRightHand ? 1.0F : -1.0F;
            // Position hand on screen
            ms.translate(isRightHand ? 0 : -0.698,
                         0.6,
                         isRightHand ? 0.7 : 0.76);
            float sqrtSwing = Mth.sqrt(swingProgress);
            float handSwingX = -0.3F * Mth.sin(sqrtSwing * (float)Math.PI);
            float handSwingY = 0.4F * Mth.sin(sqrtSwing * ((float)Math.PI * 2F));
            float handSwingZ = -0.4F * Mth.sin(swingProgress * (float)Math.PI);
            float swingSize = isRightHand
                              ? (equipProgress != 0 && equipProgress < 1 ? 2 : 4)
                              : (equipProgress != 0 && equipProgress < 1 ? 1.5f : 7);
            ms.translate(handX * (handSwingX + 0.64000005F), handSwingY/swingSize + -0.6F + equipProgress * -0.6F, handSwingZ/(swingSize*6) + -0.71999997F);
            ms.mulPose(Vector3f.YP.rotationDegrees(handX * 45.0F));
            float handFlailZ = Mth.sin(swingProgress * swingProgress * (float)Math.PI);
            float handFlailX = Mth.sin(sqrtSwing * (float)Math.PI);
            ms.mulPose(Vector3f.YP.rotationDegrees(handX * handFlailX * (isRightHand ? 1f : 20.0F)));
            ms.mulPose(Vector3f.XP.rotationDegrees(handX * handFlailX * (isRightHand ? -10.0F : 1f)));
            ms.mulPose(Vector3f.ZP.rotationDegrees(handX * handFlailZ * 20f));

            ms.mulPose(Vector3f.XP.rotationDegrees(90));
            if (isRightHand)
            {   ms.mulPose(Vector3f.ZP.rotationDegrees(-130));
            }
            else
            {   ms.mulPose(Vector3f.ZP.rotationDegrees(-230));
            }

            // Scale and translate hand so it's smaller on-screen
            ms.scale(0.5f, 0.5f, 0.5f);
            if (isRightHand)
            {   ms.translate(0.5, -0.1, 0.5);
            }
            else
            {   ms.translate(-1.2, -0.1, 0.5);
            }

            // Render the item/hand
            renderHand(ms, event.getMultiBufferSource(), event.getPackedLight(), player, isRightHand, event.getHand(), handRenderer, playerRenderer, event.getItemStack());
            ms.popPose();
            ms.popPose();
        }
    }

    public static void transformArm(LivingEntity entity, ModelPart arm, HumanoidArm side)
    {
        if (entity instanceof Player player && EntityHelper.holdingLamp(player, side))
        {
            if (side == HumanoidArm.LEFT)
            {
                arm.zRot += (float) (-0.5*Math.PI);
                arm.yRot = arm.xRot + (float) (0.5*Math.PI);
                arm.xRot = (float) -Math.PI/2;
                arm.x += 1;
                if (player.isCrouching())
                {   arm.xRot -= 0.4f;
                }
            }
            else
            {
                arm.zRot += 0.5*Math.PI;
                arm.yRot = -arm.xRot - 0.5f*(float)Math.PI;
                arm.xRot = (float) -Math.PI/2;
                arm.x -= 1;
                if (player.isCrouching())
                {   arm.xRot -= 0.4f;
                }
                // Better swinging animation
                if (player.swinging && side == player.getMainArm())
                {
                    float partialTick = Minecraft.getInstance().getFrameTime();
                    float attackAnim = player.getAttackAnim(partialTick);
                    float pitchFactor = 1.5f * CSMath.blend(0.4f, 2f, player.getViewXRot(partialTick), -90, 90);
                    if (attackAnim < 0.125)
                    {   arm.xRot += Math.max(0, Math.sin(attackAnim * Math.PI * 4) / pitchFactor);
                    }
                    else
                    {   arm.xRot += CSMath.blend(1, 0, attackAnim, 0.125, 1) / pitchFactor;
                    }
                }
            }
        }
    }

    private static void renderHand(PoseStack ms, MultiBufferSource bufferSource, int light, LocalPlayer player, boolean isRightHand,
                                   InteractionHand hand, ItemInHandRenderer handRenderer, PlayerRenderer playerRenderer, ItemStack itemStack)
    {
        boolean isSelected = player.getItemInHand(hand).is(ModItems.SOULSPRING_LAMP);
        // Render arm
        ms.pushPose();
        ms.pushPose();
        ms.scale(1, 1.2f, 1);
        ms.mulPose(Vector3f.XP.rotationDegrees(-25));
        ms.translate(0, -0.2, 0.25);
        // The hand moves a little bit when being put away. I don't know why
        if (!isSelected)
        {   ms.translate(0, isRightHand ? -0.012 : 0.015, 0);
            ms.mulPose(Vector3f.ZP.rotationDegrees(2.4f * (isRightHand ? -1 : 1)));
            ms.mulPose(Vector3f.XP.rotationDegrees(2.3f));
        }
        // Render arm for the correct side
        if (isRightHand)
        {
            if (isSelected)
            {
                ms.translate(-0.365, -0.2, -0.075);
                ms.mulPose(Vector3f.YP.rotationDegrees(-90));
                ms.mulPose(Vector3f.ZP.rotationDegrees(-90));
            }
            else
            {   ms.translate(-0.3925, 0.06, 0.38);
                ms.mulPose(Vector3f.YP.rotationDegrees(-90));
            }
            playerRenderer.renderRightHand(ms, bufferSource, light, player);
        }
        else
        {
            if (isSelected)
            {
                ms.translate(-0.335, -0.2, -0.075);
                ms.mulPose(Vector3f.YP.rotationDegrees(90));
                ms.mulPose(Vector3f.ZP.rotationDegrees(90));
            }
            else
            {   ms.translate(-0.325, 0.06, 0.38);
                ms.mulPose(Vector3f.YP.rotationDegrees(87));
            }
            playerRenderer.renderLeftHand(ms, bufferSource, light, player);
        }
        ms.popPose();

        // Render lamp item
        ms.pushPose();
        ms.mulPose(Vector3f.XP.rotationDegrees(-90));
        ms.translate(-0.35, 0.1, 0.625);
        ms.scale(1, 1, 0.8f);
        if (isRightHand)
        {
            ms.mulPose(Vector3f.ZP.rotationDegrees(90));
            ms.translate(-0.1, 0.155, 0);
            handRenderer.renderItem(player, itemStack, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, false, ms, bufferSource, light);
        }
        else
        {
            ms.mulPose(Vector3f.ZP.rotationDegrees(90));
            ms.translate(-0.1, 0.155, 0);
            handRenderer.renderItem(player, itemStack, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, false, ms, bufferSource, light);
        }
        ms.popPose();
        ms.popPose();
    }
}
