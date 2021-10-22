package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.core.util.MathHelperCS;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class PlayerHoldLamp
{
    @SubscribeEvent
    public static void renderPlayerPre(RenderPlayerEvent.Pre event)
    {
        PlayerEntity player = event.getPlayer();
        event.getRenderer().getEntityModel().bipedRightArm.showModel = false;
    }

    @SubscribeEvent
    public static void renderPlayerPost(RenderPlayerEvent.Post event)
    {
        ClientPlayerEntity player = (ClientPlayerEntity) event.getPlayer();
        MatrixStack matrixStack = event.getMatrixStack();
        PlayerModel<AbstractClientPlayerEntity> model = event.getRenderer().getEntityModel();

        model.bipedRightArm.showModel = true;
        matrixStack.push();
        matrixStack.rotate(new Quaternion(90, 0, player.renderYawOffset, true));
        if (model.isSneak)
            matrixStack.translate(0.01, -0.3, -1.1);
        else
            matrixStack.translate(0.01, -0.075, -1.275);
        model.bipedRightArm.rotateAngleX *= 0.2;
        model.bipedRightArm.rotateAngleY *= 0.2;
        event.getRenderer().renderRightArm(
                event.getMatrixStack(), event.getBuffers(),
                event.getLight(), player
        );
        event.getRenderer().renderItem(
                event.getMatrixStack(), event.getBuffers(),
                event.getLight(), player, model.bipedRightArm, 
        );
        matrixStack.pop();
    }
}
