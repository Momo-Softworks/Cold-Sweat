package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.Math;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderLampHand
{
    private static final float MAX_ARM_RAISE = CSMath.toRadians(70);

    // Matches the third-person arm pose when the lamp is fully raised
    private static final float FIRST_PERSON_ARM_Y_ROT = -0.0148f;
    private static final float FIRST_PERSON_ARM_Z_ROT = (float) (Math.PI / 2) + 0.05f;

    // Whether each hand has finished raising the lamp, indexed by Hand
    private static final boolean[] HAND_RAISED = new boolean[2];

    /* First-person */

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHandRender(RenderHandEvent event)
    {
        if (event.getItemStack().getItem() != ModItems.SOULSPRING_LAMP
        || !ConfigSettings.POSE_SOULSPRING_LAMP.get())
        {   return;
        }
        event.setCanceled(true);

        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;

        MatrixStack ms = event.getMatrixStack();
        boolean isRightHand = EntityHelper.getArmFromHand(event.getHand(), player) == HandSide.RIGHT;
        float handX = isRightHand ? 1 : -1;
        float swingProgress = event.getSwingProgress();
        float equipProgress = event.getEquipProgress();

        // Vanilla lowers the hand while the attack cooldown recovers
        int handIndex = event.getHand().ordinal();
        if (event.getItemStack() != player.getItemInHand(event.getHand()) || player.isHandsBusy())
        {   HAND_RAISED[handIndex] = false;
        }
        else if (equipProgress == 0)
        {   HAND_RAISED[handIndex] = true;
        }
        if (HAND_RAISED[handIndex])
        {   equipProgress = 0;
        }

        ms.pushPose();

        // Position hand on screen
        float sqrtSwing = MathHelper.sqrt(swingProgress);
        float handSwingX = -0.3F * MathHelper.sin(sqrtSwing * (float) Math.PI);
        float handSwingY = 0.1F * MathHelper.sin(sqrtSwing * (float) Math.PI * 2);
        float handSwingZ = -0.0167F * MathHelper.sin(swingProgress * (float) Math.PI);
        ms.translate(handX * (handSwingX + 0.64F), handSwingY + 0.2F - equipProgress * 0.6F, handSwingZ - 0.32F);
        ms.mulPose(Vector3f.YP.rotationDegrees(handX * 45));

        // Swing animation
        float handFlailX = MathHelper.sin(sqrtSwing * (float) Math.PI);
        float handFlailZ = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        ms.mulPose(Vector3f.YP.rotationDegrees(handX * handFlailX));
        ms.mulPose(Vector3f.XP.rotationDegrees(handFlailX * -10));
        ms.mulPose(Vector3f.ZP.rotationDegrees(handX * handFlailZ * 20));

        // Hold the lamp out in front, palm down
        ms.mulPose(Vector3f.XP.rotationDegrees(90));
        ms.mulPose(Vector3f.ZP.rotationDegrees(handX * -130));
        ms.scale(0.5f, 0.5f, 0.5f);
        ms.translate(handX * 0.5, -0.1, 0.5);

        // Render arm
        ms.pushPose();
        ms.scale(1, 1.2f, 1);
        ms.mulPose(Vector3f.XP.rotationDegrees(-25));
        ms.translate(handX * -0.365, -0.4, 0.175);
        ms.mulPose(Vector3f.YP.rotationDegrees(handX * -90));
        ms.mulPose(Vector3f.ZP.rotationDegrees(handX * -90));
        PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        renderArm(ms, event.getBuffers(), event.getLight(), player, isRightHand, playerRenderer);
        ms.popPose();

        // Render lamp item
        ms.pushPose();
        ms.mulPose(Vector3f.XP.rotationDegrees(-90));
        ms.translate(handX * -0.35, 0.1, 0.625);
        ms.scale(1, 1, 0.8f);
        ms.mulPose(Vector3f.ZP.rotationDegrees(90));
        ms.translate(-0.1, 0.125, 0);
        renderLamp(ms, event.getBuffers(), event.getLight(), player, isRightHand, event.getItemStack());
        ms.popPose();

        ms.popPose();
    }

    /**
     * Renders the player's arm with a fixed pose, instead of going through {@link PlayerRenderer#renderRightHand},
     * which runs {@code setupAnim()} and lets the third-person arm pose leak into the first-person arm.
     */
    private static void renderArm(MatrixStack ms, IRenderTypeBuffer bufferSource, int light, ClientPlayerEntity player, boolean isRightHand, PlayerRenderer playerRenderer)
    {
        HandSide side = isRightHand ? HandSide.RIGHT : HandSide.LEFT;
        // Allow other mods to render the arm (i.e. first-person armor)
        if (ForgeHooksClient.renderSpecificFirstPersonArm(ms, bufferSource, light, player, side)) return;

        PlayerModel<AbstractClientPlayerEntity> model = playerRenderer.getModel();
        ModelRenderer arm = isRightHand ? model.rightArm : model.leftArm;
        ModelRenderer sleeve = isRightHand ? model.rightSleeve : model.leftSleeve;
        float sideMultiplier = isRightHand ? 1 : -1;

        arm.setPos(-6 * sideMultiplier, ClientOnlyHelper.isPlayerModelSlim(model) ? 2 : 3, 0);
        arm.xRot = 0;
        arm.yRot = FIRST_PERSON_ARM_Y_ROT * sideMultiplier;
        arm.zRot = FIRST_PERSON_ARM_Z_ROT * sideMultiplier;
        sleeve.copyFrom(arm);
        sleeve.visible = player.isModelPartShown(isRightHand ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE);

        ResourceLocation skin = player.getSkinTextureLocation();
        arm.render(ms, bufferSource.getBuffer(RenderType.entitySolid(skin)), light, OverlayTexture.NO_OVERLAY);
        sleeve.render(ms, bufferSource.getBuffer(RenderType.entityTranslucent(skin)), light, OverlayTexture.NO_OVERLAY);
    }

    private static void renderLamp(MatrixStack ms, IRenderTypeBuffer bufferSource, int light, ClientPlayerEntity player, boolean isRightHand, ItemStack itemStack)
    {
        ItemCameraTransforms.TransformType context = isRightHand ? ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
        Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderItem(player, itemStack, context, false, ms, bufferSource, light);
    }

    /* Third-person */

    /**
     * Applied in place of vanilla's arm pose when a player's arm is holding the lamp
     */
    public static void poseArm(BipedModel<?> model, LivingEntity entity, HandSide side)
    {
        ModelRenderer arm = getArm(model, side);
        float sideMultiplier = side == HandSide.RIGHT ? 1 : -1;
        float raise = HandleSoulLampAnim.getArmRaise(entity, side, getPartialTick()) * MAX_ARM_RAISE;

        // Applied to yRot, since the arm is rolled onto its side
        float armHeight = (arm.xRot * 0.5f - (float) Math.PI / 10) * 0.15f - 0.35f - raise;
        arm.xRot = (float) -Math.PI / 2;
        arm.yRot = -(armHeight + (float) Math.PI / 2 * 1.04f) * sideMultiplier;
        arm.zRot = ((float) Math.PI / 2 - 0.05f) * sideMultiplier;
        // Cancel vanilla's crouching offset
        if (model.crouching)
        {   arm.xRot -= 0.4f;
        }
    }

    /**
     * Lowers the arm after the lamp is put away
     */
    public static void lowerArm(BipedModel<?> model, LivingEntity entity, HandSide side)
    {
        if (!ConfigSettings.POSE_SOULSPRING_LAMP.get() || EntityHelper.holdingLamp(entity, side)) return;

        BipedModel.ArmPose pose = side == HandSide.RIGHT ? model.rightArmPose : model.leftArmPose;
        float raise = HandleSoulLampAnim.getArmRaise(entity, side, getPartialTick()) * MAX_ARM_RAISE;
        if (raise > 0.01f && (pose == BipedModel.ArmPose.EMPTY || pose == BipedModel.ArmPose.ITEM))
        {   getArm(model, side).xRot -= raise;
        }
    }

    /**
     * Replaces vanilla's swing animation for the lamp arm
     */
    public static void swingArm(BipedModel<?> model, HandSide side, float attackAnim)
    {
        ModelRenderer arm = getArm(model, side);
        float sideMultiplier = side == HandSide.RIGHT ? 1 : -1;
        float pitch = CSMath.toDegrees(model.head.xRot);
        float swingSin = MathHelper.sin(attackAnim * (float) Math.PI);

        // xRot swings the arm sideways and yRot swings it vertically, since the arm is rolled onto its side
        float windUp = getWindUp(attackAnim) / getSwingHorizontalOffset(side, pitch);
        float sweep = MathHelper.sin((1 - (float) Math.pow(1 - attackAnim, 4)) * (float) Math.PI) * 1.2f
                    + swingSin * (0.7f - model.head.xRot) * 0.75f;
        arm.xRot += windUp - sweep;

        float pitchSwingHeight = pitch < 0 ? pitch/20 : pitch/60;
        arm.yRot += (float) (Math.pow(attackAnim - 0.5, 2) - 0.25) * pitchSwingHeight * sideMultiplier;
        arm.zRot += swingSin * (side == HandSide.RIGHT ? -0.4f : 0.1f);

        // Follow the body's twist
        arm.yRot += model.body.yRot * 3;
        if (side == HandSide.LEFT)
        {   arm.xRot += model.body.yRot;
        }
    }

    /**
     * Applied after vanilla positions the arms
     */
    public static void offsetArm(BipedModel<?> model, LivingEntity entity, HandSide side)
    {
        if (!(entity instanceof PlayerEntity) || !EntityHelper.holdingLamp(entity, side)) return;

        ModelRenderer arm = getArm(model, side);
        arm.x -= side == HandSide.RIGHT ? 1 : -1;
        if (model instanceof PlayerModel<?> && !ClientOnlyHelper.isPlayerModelSlim(model))
        {   arm.y += 1;
        }
    }

    private static float getWindUp(float attackAnim)
    {
        float windUpTime = 0.3f;
        float windUpPoint = 1.5f;
        float midSwingTime = 0.7f;

        if (attackAnim < windUpTime / 3)
        {   return CSMath.blendLog(0f, windUpPoint, attackAnim, 0, windUpTime / 3, 1);
        }
        else if (attackAnim < windUpTime)
        {   return CSMath.blendLog(windUpPoint, 1f, attackAnim, windUpTime / 3, windUpTime, 1);
        }
        else if (attackAnim < midSwingTime)
        {   return CSMath.blend(1f, 0.2f, attackAnim, windUpTime, midSwingTime);
        }
        else
        {   return CSMath.blendExp(0.2f, 0f, attackAnim, midSwingTime, 1);
        }
    }

    private static float getSwingHorizontalOffset(HandSide side, float playerPitch)
    {
        float pitchFactor;
        if (side == HandSide.RIGHT)
        {
            pitchFactor = playerPitch < 0 ? CSMath.blend(1.8f, 0.6f, playerPitch, 0, -90)
                                          : CSMath.blend(1.8f, 3f, playerPitch, 0, 90);
        }
        else
        {
            pitchFactor = playerPitch < 0 ? CSMath.blend(3f, 0.5f, playerPitch, 0, -90)
                                          : CSMath.blend(3f, 3f, playerPitch, 0, 90);
        }
        return pitchFactor;
    }

    private static ModelRenderer getArm(BipedModel<?> model, HandSide side)
    {   return side == HandSide.RIGHT ? model.rightArm : model.leftArm;
    }

    private static float getPartialTick()
    {   return Minecraft.getInstance().getFrameTime();
    }
}
