package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
@OnlyIn(Dist.CLIENT)
public class PlayerHoldLamp
{
    /*@SubscribeEvent
    public static void renderPlayerPre(RenderLivingEvent.Pre<PlayerEntity, PlayerModel<PlayerEntity>> event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            event.getRenderer().getEntityModel().bipedRightArm.showModel = false;
            event.getRenderer().getEntityModel().bipedRightArmwear.showModel = false;
        }
    }

    @SubscribeEvent
    public static void renderPlayerPost(RenderLivingEvent.Post<PlayerEntity, PlayerModel<PlayerEntity>> event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            PlayerModel model = event.getRenderer().getEntityModel();
            MatrixStack matrixStack = event.getMatrixStack();
            IVertexBuilder buffer = event.getBuffers().getBuffer(RenderType.getEntitySolid(((AbstractClientPlayerEntity) event.getEntity()).getLocationSkin()));

            model.bipedRightArm.showModel = true;
            model.bipedRightArmwear.showModel = true;
            matrixStack.push();
            matrixStack.rotate(new Quaternion(model.bipedBody.rotateAngleX, model.bipedBody.rotateAngleY, model.bipedBody.rotateAngleZ, false));
            matrixStack.translate(-0.2, 0.5, 0);
            model.bipedRightArm.render(event.getMatrixStack(), buffer, event.getLight(), OverlayTexture.NO_OVERLAY);
            matrixStack.pop();
            model.bipedRightArm.rotateAngleX = (float) Math.PI * 0.25f;
        }
    }*/
}
