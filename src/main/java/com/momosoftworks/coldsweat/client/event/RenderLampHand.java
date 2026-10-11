package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
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

    // Whether each hand has finished raising the lamp, indexed by InteractionHand
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

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        PoseStack ms = event.getPoseStack();
        boolean isRightHand = EntityHelper.getArmFromHand(event.getHand(), player) == HumanoidArm.RIGHT;
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
        float sqrtSwing = Mth.sqrt(swingProgress);
        float handSwingX = -0.3F * Mth.sin(sqrtSwing * (float) Math.PI);
        float handSwingY = 0.1F * Mth.sin(sqrtSwing * (float) Math.PI * 2);
        float handSwingZ = -0.0167F * Mth.sin(swingProgress * (float) Math.PI);
        ms.translate(handX * (handSwingX + 0.64F), handSwingY + 0.2F - equipProgress * 0.6F, handSwingZ - 0.32F);
        ms.mulPose(Vector3f.YP.rotationDegrees(handX * 45));

        // Swing animation
        float handFlailX = Mth.sin(sqrtSwing * (float) Math.PI);
        float handFlailZ = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
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
        renderArm(ms, event.getMultiBufferSource(), event.getPackedLight(), player, isRightHand, playerRenderer);
        ms.popPose();

        // Render lamp item
        ms.pushPose();
        ms.mulPose(Vector3f.XP.rotationDegrees(-90));
        ms.translate(handX * -0.35, 0.1, 0.625);
        ms.scale(1, 1, 0.8f);
        ms.mulPose(Vector3f.ZP.rotationDegrees(90));
        ms.translate(-0.1, 0.125, 0);
        renderLamp(ms, event.getMultiBufferSource(), event.getPackedLight(), player, isRightHand, event.getItemStack());
        ms.popPose();

        ms.popPose();
    }

    /**
     * Renders the player's arm with a fixed pose, instead of going through {@link PlayerRenderer#renderRightHand},
     * which runs {@code setupAnim()} and lets the third-person arm pose leak into the first-person arm.
     */
    private static void renderArm(PoseStack ms, MultiBufferSource bufferSource, int light, LocalPlayer player, boolean isRightHand, PlayerRenderer playerRenderer)
    {
        HumanoidArm side = isRightHand ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        // Allow other mods to render the arm (i.e. first-person armor)
        if (ForgeHooksClient.renderSpecificFirstPersonArm(ms, bufferSource, light, player, side)) return;

        PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
        ModelPart arm = isRightHand ? model.rightArm : model.leftArm;
        ModelPart sleeve = isRightHand ? model.rightSleeve : model.leftSleeve;
        float sideMultiplier = isRightHand ? 1 : -1;

        arm.setPos(-6 * sideMultiplier, ClientOnlyHelper.isPlayerModelSlim(model) ? 2 : 3, 0);
        arm.setRotation(0, FIRST_PERSON_ARM_Y_ROT * sideMultiplier, FIRST_PERSON_ARM_Z_ROT * sideMultiplier);
        sleeve.copyFrom(arm);
        sleeve.visible = player.isModelPartShown(isRightHand ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE);

        ResourceLocation skin = player.getSkinTextureLocation();
        arm.render(ms, bufferSource.getBuffer(RenderType.entitySolid(skin)), light, OverlayTexture.NO_OVERLAY);
        sleeve.render(ms, bufferSource.getBuffer(RenderType.entityTranslucent(skin)), light, OverlayTexture.NO_OVERLAY);
    }

    private static void renderLamp(PoseStack ms, MultiBufferSource bufferSource, int light, LocalPlayer player, boolean isRightHand, ItemStack itemStack)
    {
        ItemTransforms.TransformType context = isRightHand ? ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND : ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
        Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderItem(player, itemStack, context, false, ms, bufferSource, light);
    }

    /* Third-person */

    /**
     * Applied in place of vanilla's arm pose when a player's arm is holding the lamp
     */
    public static void poseArm(HumanoidModel<?> model, LivingEntity entity, HumanoidArm side)
    {
        ModelPart arm = getArm(model, side);
        float sideMultiplier = side == HumanoidArm.RIGHT ? 1 : -1;
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
    public static void lowerArm(HumanoidModel<?> model, LivingEntity entity, HumanoidArm side)
    {
        if (!ConfigSettings.POSE_SOULSPRING_LAMP.get() || EntityHelper.holdingLamp(entity, side)) return;

        HumanoidModel.ArmPose pose = side == HumanoidArm.RIGHT ? model.rightArmPose : model.leftArmPose;
        float raise = HandleSoulLampAnim.getArmRaise(entity, side, getPartialTick()) * MAX_ARM_RAISE;
        if (raise > 0.01f && (pose == HumanoidModel.ArmPose.EMPTY || pose == HumanoidModel.ArmPose.ITEM))
        {   getArm(model, side).xRot -= raise;
        }
    }

    /**
     * Replaces vanilla's swing animation for the lamp arm
     */
    public static void swingArm(HumanoidModel<?> model, HumanoidArm side, float attackAnim)
    {
        ModelPart arm = getArm(model, side);
        float sideMultiplier = side == HumanoidArm.RIGHT ? 1 : -1;
        float pitch = CSMath.toDegrees(model.head.xRot);
        float swingSin = Mth.sin(attackAnim * (float) Math.PI);

        // xRot swings the arm sideways and yRot swings it vertically, since the arm is rolled onto its side
        float windUp = getWindUp(attackAnim) / getSwingHorizontalOffset(side, pitch);
        float sweep = Mth.sin((1 - (float) Math.pow(1 - attackAnim, 4)) * (float) Math.PI) * 1.2f
                    + swingSin * (0.7f - model.head.xRot) * 0.75f;
        arm.xRot += windUp - sweep;

        float pitchSwingHeight = pitch < 0 ? pitch/20 : pitch/60;
        arm.yRot += (float) (Math.pow(attackAnim - 0.5, 2) - 0.25) * pitchSwingHeight * sideMultiplier;
        arm.zRot += swingSin * (side == HumanoidArm.RIGHT ? -0.4f : 0.1f);

        // Follow the body's twist
        arm.yRot += model.body.yRot * 3;
        if (side == HumanoidArm.LEFT)
        {   arm.xRot += model.body.yRot;
        }
    }

    /**
     * Applied after vanilla positions the arms
     */
    public static void offsetArm(HumanoidModel<?> model, LivingEntity entity, HumanoidArm side)
    {
        if (!(entity instanceof Player) || !EntityHelper.holdingLamp(entity, side)) return;

        ModelPart arm = getArm(model, side);
        arm.x -= side == HumanoidArm.RIGHT ? 1 : -1;
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

    private static float getSwingHorizontalOffset(HumanoidArm side, float playerPitch)
    {
        float pitchFactor;
        if (side == HumanoidArm.RIGHT)
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

    private static ModelPart getArm(HumanoidModel<?> model, HumanoidArm side)
    {   return side == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
    }

    private static float getPartialTick()
    {   return Minecraft.getInstance().getFrameTime();
    }
}
