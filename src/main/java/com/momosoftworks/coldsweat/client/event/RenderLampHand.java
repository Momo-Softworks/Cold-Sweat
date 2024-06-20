package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

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
            ms.translate(isRightHand ? 0.5 : -1.2, 0.2, -0.3);
            // Move for equip progress
            ms.translate(event.getEquipProgress() * (isRightHand ? -0.2 : 0.2), -event.getEquipProgress(), 0);
            // Apply rotations
            ms.mulPose(Axis.XP.rotationDegrees(90));
            ms.mulPose(Axis.ZP.rotationDegrees(180));
            // Swing animation
            ms.translate(0,
                         Math.sin(event.getSwingProgress()*Math.PI) * 0.3,
                         Math.sin(event.getSwingProgress()*event.getSwingProgress()*Math.PI) * 0.4);
            ms.mulPose(Axis.YP.rotationDegrees((float) Math.sin(event.getSwingProgress()*Math.PI) * (isRightHand ? -30 : 30)));
            ms.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(event.getSwingProgress()*Math.PI) * (isRightHand ? -30 : 30)));
            // Render the item/hand
            renderHand(ms, event.getMultiBufferSource(), event.getPackedLight(), player, isRightHand, event.getHand(), handRenderer, playerRenderer, event.getItemStack());
            ms.popPose();
        }
    }

    private static void renderHand(PoseStack ms, MultiBufferSource bufferSource, int light, LocalPlayer player, boolean isRightHand, InteractionHand hand, ItemInHandRenderer handRenderer, PlayerRenderer playerRenderer, ItemStack itemStack)
    {
        boolean isSelected = player.getItemInHand(hand).is(ModItems.SOULSPRING_LAMP);
        // Render arm
        ms.pushPose();
        ms.pushPose();
        ms.scale(1, 1.2f, 1);
        ms.mulPose(Axis.XP.rotationDegrees(-25));
        ms.translate(0, -0.2, 0.25);
        // The hand moves a little bit when being put away. I don't know why
        if (!isSelected)
        {   ms.translate(0, isRightHand ? -0.012 : 0.015, 0);
            ms.mulPose(Axis.ZP.rotationDegrees(2.3f * (isRightHand ? -1 : 1)));
        }
        // Render arm for the correct side
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
        ms.mulPose(Axis.XP.rotationDegrees(-90));
        ms.translate(-0.35, 0.1, 0.625);
        ms.scale(1, 1, 0.8f);
        if (isRightHand)
        {   handRenderer.renderItem(player, itemStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, ms, bufferSource, light);
        }
        else
        {   handRenderer.renderItem(player, itemStack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, false, ms, bufferSource, light);
        }
        ms.popPose();
        ms.popPose();
    }
}
