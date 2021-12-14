package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.PlayerHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class PlayerHoldLamp
{
    static Field layerRenderers;
    static Method renderHeldItem;
    static Field armorModel;
    static Field leggingsModel;
    static Method renderArmor;

    static
    {
        renderHeldItem = ObfuscationReflectionHelper.findMethod(HeldItemLayer.class, "func_229135_a_",
                LivingEntity.class, ItemStack.class, ItemCameraTransforms.TransformType.class, HandSide.class, MatrixStack.class, IRenderTypeBuffer.class, int.class);
        renderHeldItem.setAccessible(true);

        layerRenderers = ObfuscationReflectionHelper.findField(LivingRenderer.class, "field_177097_h");
        layerRenderers.setAccessible(true);

        armorModel = ObfuscationReflectionHelper.findField(BipedArmorLayer.class, "field_177186_d");
        armorModel.setAccessible(true);

        leggingsModel = ObfuscationReflectionHelper.findField(BipedArmorLayer.class, "field_177189_c");
        leggingsModel.setAccessible(true);

        renderArmor = ObfuscationReflectionHelper.findMethod(BipedArmorLayer.class, "func_241739_a_",
                MatrixStack.class, IRenderTypeBuffer.class, LivingEntity.class, EquipmentSlotType.class, int.class, BipedModel.class);
        renderArmor.setAccessible(true);
    }

    private static boolean shouldCustomRenderRight(PlayerEntity player, PlayerModel model, float partialRenderTick)
    {
        return model.rightArmPose != BipedModel.ArmPose.BOW_AND_ARROW && model.rightArmPose != BipedModel.ArmPose.CROSSBOW_CHARGE &&
               model.rightArmPose != BipedModel.ArmPose.THROW_SPEAR && model.rightArmPose != BipedModel.ArmPose.CROSSBOW_HOLD &&
                player.getSwimAnimation(partialRenderTick) == 0 && !player.isElytraFlying() && player.isAlive();
    }

    private static boolean shouldCustomRenderLeft(PlayerEntity player, PlayerModel model, float partialRenderTick)
    {
        return model.leftArmPose != BipedModel.ArmPose.BOW_AND_ARROW && model.leftArmPose != BipedModel.ArmPose.CROSSBOW_CHARGE &&
                model.leftArmPose != BipedModel.ArmPose.THROW_SPEAR && model.leftArmPose != BipedModel.ArmPose.CROSSBOW_HOLD &&
                player.getSwimAnimation(partialRenderTick) == 0 && !player.isElytraFlying() && player.isAlive();
    }

    @SubscribeEvent
    public static void renderPlayerPre(RenderPlayerEvent.Pre event)
    {
        // Determine whether to render the lamp pose
        PlayerRenderer renderer = event.getRenderer();
        PlayerEntity player = event.getPlayer();
        if (!player.isSpectator())
        {
            boolean renderingLamp = false;

            if (PlayerHelper.holdingLamp(player, HandSide.RIGHT) &&
            shouldCustomRenderRight(event.getPlayer(), renderer.getEntityModel(), event.getPartialRenderTick()))
            {
                renderer.getEntityModel().bipedRightArm.showModel = false;
                renderer.getEntityModel().bipedRightArmwear.showModel = false;
                renderingLamp = true;
            }

            if (PlayerHelper.holdingLamp(player, HandSide.LEFT) &&
            shouldCustomRenderLeft(event.getPlayer(), renderer.getEntityModel(), event.getPartialRenderTick()))
            {
                renderer.getEntityModel().bipedLeftArm.showModel = false;
                renderer.getEntityModel().bipedLeftArmwear.showModel = false;
                renderingLamp = true;
            }
            try
            {
                if (renderingLamp)
                {
                    ((List<LayerRenderer>) layerRenderers.get(renderer)).removeIf(layerRenderer ->
                            layerRenderer instanceof HeldItemLayer || layerRenderer instanceof BipedArmorLayer);
                }
                else
                {
                    if (((List<LayerRenderer>) layerRenderers.get(renderer)).stream().noneMatch(layerRenderer ->
                            layerRenderer instanceof HeldItemLayer || layerRenderer instanceof BipedArmorLayer))
                    {
                        ((List<LayerRenderer>) layerRenderers.get(renderer)).add(new HeldItemLayer(event.getRenderer()));
                        ((List<LayerRenderer>) layerRenderers.get(renderer)).add(new BipedArmorLayer(event.getRenderer(), new BipedModel(0.5F), new BipedModel(1.0F)));
                    }
                    event.getPlayer().getPersistentData().putFloat("lanternRightArmRot", 3.0f);
                    event.getPlayer().getPersistentData().putFloat("lanternLeftArmRot", 3.0f);
                }
            } catch (Exception e) {}
        }
    }

    @SubscribeEvent
    public static void renderPlayerPost(RenderPlayerEvent.Post event)
    {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) event.getPlayer();
        PlayerModel<AbstractClientPlayerEntity> model = event.getRenderer().getEntityModel();
        float renderTick = event.getPartialRenderTick();
        boolean renderRight = shouldCustomRenderRight(player, model, renderTick) && PlayerHelper.holdingLamp(player, HandSide.RIGHT);
        boolean renderLeft = shouldCustomRenderLeft(player, model, renderTick) && PlayerHelper.holdingLamp(player, HandSide.LEFT);

        if ((renderRight || renderLeft) && !player.isSpectator())
        {
            MatrixStack matrixStack = event.getMatrixStack();
            PlayerRenderer renderer = event.getRenderer();
            IRenderTypeBuffer buffers = event.getBuffers();
            int light = event.getLight();
            float rightArmRot = player.getPersistentData().getFloat("lanternRightArmRot");
            float leftArmRot = player.getPersistentData().getFloat("lanternLeftArmRot");
            float naturalRightArmRot = model.bipedRightArm.rotateAngleX / 30 + 0.01f;
            float naturalLeftArmRot = model.bipedLeftArm.rotateAngleX / 30 + 0.01f;

            // Render Right Arm
            matrixStack.push();
            if (renderRight) {
                renderCustomRight(model, matrixStack, player, event.getBuffers(), event.getLight(), renderTick, naturalRightArmRot, rightArmRot, event.getRenderer());
            }
            else
                event.getPlayer().getPersistentData().putFloat("lanternRightArmRot", 3.0f);
            matrixStack.pop();

            // Render Left Arm
            matrixStack.push();
            if (renderLeft) {
                renderCustomLeft(model, matrixStack, player, event.getBuffers(), event.getLight(), renderTick, naturalLeftArmRot, leftArmRot, event.getRenderer());
            }
            else
                event.getPlayer().getPersistentData().putFloat("lanternLeftArmRot", 3.0f);
            matrixStack.pop();

            // Render held items
            try
            {
                if (renderRight)
                {
                    // MatrixStack transforms for right hand
                    matrixStack.push();
                    matrixStack.rotate(new Quaternion(0, -MathHelper.lerp(renderTick, player.prevRenderYawOffset, player.renderYawOffset), 0, true));
                    // Render right hand
                    renderHeldItem.invoke(new HeldItemLayer<>(renderer), player, PlayerHelper.getItemInHand(player, HandSide.RIGHT),
                            ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStack, buffers, Math.max(128, light));
                    if (!renderLeft)
                    {
                        matrixStack.rotate(Vector3f.XP.rotation(MathHelperCS.toRadians(180)));
                        matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
                        matrixStack.translate(0, -1.5, 0);
                        renderHeldItem.invoke(new HeldItemLayer<>(renderer), player, PlayerHelper.getItemInHand(player, HandSide.LEFT),
                                ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStack, buffers, light);
                    }
                    matrixStack.pop();
                }
                if (renderLeft)
                {
                    // MatrixStack transforms for left hand
                    matrixStack.push();
                    matrixStack.rotate(new Quaternion(0, -MathHelper.lerp(renderTick, player.prevRenderYawOffset, player.renderYawOffset), 0, true));
                    // Render left hand
                    renderHeldItem.invoke(new HeldItemLayer<>(renderer), player, PlayerHelper.getItemInHand(player, HandSide.LEFT),
                            ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStack, buffers, Math.max(128, light));
                    if (!renderRight)
                    {
                        matrixStack.rotate(Vector3f.XP.rotation(MathHelperCS.toRadians(180)));
                        matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
                        matrixStack.translate(0, -1.5, 0);
                        renderHeldItem.invoke(new HeldItemLayer<>(renderer), player, PlayerHelper.getItemInHand(player, HandSide.RIGHT),
                                ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStack, buffers, light);
                    }
                    matrixStack.pop();
                }
            } catch (Exception e) {}

            // Render armor
            try
            {
                matrixStack.push();
                matrixStack.rotate(Vector3f.XP.rotation(MathHelperCS.toRadians(180)));
                matrixStack.rotate(new Quaternion(0, MathHelper.lerp(renderTick, player.prevRenderYawOffset, player.renderYawOffset), 0, true));
                matrixStack.translate(0, -1.407, 0);
                matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
                if (renderRight)
                {
                    model.bipedRightArm.rotationPointY = model.isSneak ? 5f : 2f;
                    model.bipedRightArm.rotateAngleX += MathHelperCS.toRadians(180);
                }
                if (renderLeft)
                {
                    model.bipedLeftArm.rotationPointY = model.isSneak ? 5f : 2f;
                    model.bipedLeftArm.rotateAngleX += MathHelperCS.toRadians(180);
                }
                BipedArmorLayer armorLayer = new BipedArmorLayer<>(renderer, new BipedModel(0.5F), new BipedModel(1.0F));
                renderArmor.invoke(armorLayer, matrixStack, buffers, player, EquipmentSlotType.HEAD, light, armorModel.get(armorLayer));
                renderArmor.invoke(armorLayer, matrixStack, buffers, player, EquipmentSlotType.CHEST,light, armorModel.get(armorLayer));
                renderArmor.invoke(armorLayer, matrixStack, buffers, player, EquipmentSlotType.LEGS, light, leggingsModel.get(armorLayer));
                renderArmor.invoke(armorLayer, matrixStack, buffers, player, EquipmentSlotType.FEET, light, armorModel.get(armorLayer));
                matrixStack.pop();
            } catch (Exception e) {}
        }
    }

    private static void renderCustomRight(PlayerModel<AbstractClientPlayerEntity> model, MatrixStack matrixStack, AbstractClientPlayerEntity player, IRenderTypeBuffer buffers,
                                   int light, float renderTick, float naturalRightArmRot, float rightArmRot, PlayerRenderer renderer)
    {
        model.bipedRightArm.showModel = true;
        model.bipedRightArmwear.showModel = true;
        // Set arm transforms before render
        model.bipedRightArm.rotateAngleX = naturalRightArmRot + (rightArmRot + ((model.isSneak ? 1.4f : 1.6f) - rightArmRot) / 10);
        player.getPersistentData().putFloat("lanternRightArmRot", model.bipedRightArm.rotateAngleX);
        model.bipedRightArm.rotateAngleY = model.bipedRightArm.rotateAngleY / 5f;
        model.bipedRightArm.rotateAngleZ = model.bipedRightArm.rotateAngleZ / 5f;
        model.bipedRightArm.rotationPointY = model.isSneak ? 17.5f : 20.5f;
        model.bipedRightArm.rotationPointX += 0.15f;
        model.bipedRightArmwear.copyModelAngles(model.bipedRightArm);
        matrixStack.rotate(new Quaternion(0, -MathHelper.lerp(renderTick, player.prevRenderYawOffset, player.renderYawOffset), 0, true));
        // Render right arm
        model.bipedRightArm.render(matrixStack, buffers.getBuffer(RenderType.getEntitySolid(player.getLocationSkin())),
                light, OverlayTexture.NO_OVERLAY);
        model.bipedRightArmwear.render(matrixStack, buffers.getBuffer(RenderType.getEntityCutout(player.getLocationSkin())),
                light, OverlayTexture.NO_OVERLAY);
    }

    private static void renderCustomLeft(PlayerModel<AbstractClientPlayerEntity> model, MatrixStack matrixStack, AbstractClientPlayerEntity player, IRenderTypeBuffer buffers,
                                         int light, float renderTick, float naturalLeftArmRot, float leftArmRot, PlayerRenderer renderer)
    {
        model.bipedLeftArm.showModel = true;
        model.bipedLeftArmwear.showModel = true;
        // Set arm transforms before render
        model.bipedLeftArm.rotateAngleX = naturalLeftArmRot + (leftArmRot + ((model.isSneak ? 1.4f : 1.6f) - leftArmRot) / 10);
        player.getPersistentData().putFloat("lanternLeftArmRot", model.bipedLeftArm.rotateAngleX);
        model.bipedLeftArm.rotateAngleY = model.bipedLeftArm.rotateAngleY / 5f;
        model.bipedLeftArm.rotateAngleZ = model.bipedLeftArm.rotateAngleZ / 5f;
        model.bipedLeftArm.rotationPointY = model.isSneak ? 17.5f : 20.5f;
        model.bipedLeftArm.rotationPointX += 0.15f;
        model.bipedLeftArmwear.copyModelAngles(model.bipedLeftArm);
        matrixStack.rotate(new Quaternion(0, -MathHelper.lerp(renderTick, player.prevRenderYawOffset, player.renderYawOffset), 0, true));
        // Render left arm
        model.bipedLeftArm.render(matrixStack, buffers.getBuffer(RenderType.getEntitySolid(player.getLocationSkin())),
                light, OverlayTexture.NO_OVERLAY);
        model.bipedLeftArmwear.render(matrixStack, buffers.getBuffer(RenderType.getEntityCutout(player.getLocationSkin())),
                light, OverlayTexture.NO_OVERLAY);
    }
}
