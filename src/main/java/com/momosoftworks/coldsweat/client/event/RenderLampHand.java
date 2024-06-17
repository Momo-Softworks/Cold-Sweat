package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;
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

            MatrixStack ms = event.getMatrixStack();
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player == null) return;

            boolean isRightHand = EntityHelper.getArmFromHand(event.getHand(), player) == HandSide.RIGHT;
            PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
            FirstPersonRenderer handRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;

            ms.pushPose();
            // Idle position on the screen
            ms.translate(isRightHand ? 0.5 : -1.2, 0.2, -0.3);
            // Move for equip progress
            ms.translate(event.getEquipProgress() * (isRightHand ? -0.2 : 0.2), -event.getEquipProgress(), 0);
            // Apply rotations
            ms.mulPose(Vector3f.XP.rotationDegrees(90));
            ms.mulPose(Vector3f.ZP.rotationDegrees(180));
            // Swing animation
            ms.translate(0,
                         Math.sin(event.getSwingProgress()*Math.PI) * 0.3,
                         Math.sin(event.getSwingProgress()*event.getSwingProgress()*Math.PI) * 0.4);
            ms.mulPose(Vector3f.YP.rotationDegrees((float) Math.sin(event.getSwingProgress()*Math.PI) * (isRightHand ? -30 : 30)));
            ms.mulPose(Vector3f.ZP.rotationDegrees((float) Math.sin(event.getSwingProgress()*Math.PI) * (isRightHand ? -30 : 30)));
            // Render the item/hand
            renderHand(ms, event.getBuffers(), event.getLight(), player, isRightHand, handRenderer, playerRenderer, event.getItemStack());
            ms.popPose();
        }
    }

    private static void renderHand(MatrixStack ms, IRenderTypeBuffer bufferSource, int light, ClientPlayerEntity player, boolean isRightHand, FirstPersonRenderer handRenderer, PlayerRenderer playerRenderer, ItemStack itemStack)
    {
        // Render arm
        ms.pushPose();
        ms.pushPose();
        ms.scale(1, 1.2f, 1);
        ms.mulPose(Vector3f.XP.rotationDegrees(-25));
        ms.translate(0, -0.2, 0.25);
        if (isRightHand)
        {   playerRenderer.renderRightHand(ms, bufferSource, light, player);
        }
        else
        {   ms.translate(-0.7, 0, 0);
            playerRenderer.renderLeftHand(ms, bufferSource, light, player);
        }
        ms.popPose();

        // Render lamp item
        ms.pushPose();
        ms.mulPose(Vector3f.XP.rotationDegrees(-90));
        ms.translate(-0.35, 0.1, 0.625);
        ms.scale(1, 1, 0.8f);
        if (isRightHand)
        {   handRenderer.renderItem(player, itemStack, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, false, ms, bufferSource, light);
        }
        else
        {   handRenderer.renderItem(player, itemStack, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, false, ms, bufferSource, light);
        }
        ms.popPose();
        ms.popPose();
    }
}