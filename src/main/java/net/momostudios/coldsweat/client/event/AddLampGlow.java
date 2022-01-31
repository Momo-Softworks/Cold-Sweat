package net.momostudios.coldsweat.client.event;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.model.TransformationHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.util.CSMath;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class AddLampGlow
{
   /* @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event)
    {
        ResourceLocation texture = new ResourceLocation("cold_sweat:textures/entity/hellspring_lamp/hellspring_lamp_glow_3.png");
        event.getRenderer().getRenderManager().textureManager.bindTexture(texture);

        ModelRenderer lampBody = new ModelRenderer(event.getRenderer().getEntityModel(), 0, 0);
        lampBody.addBox(-4.65f, 3f, -11.8f, 8f, 12f, 8f, -1.39f);
        lampBody.copyModelAngles(event.getRenderer().getEntityModel().bipedRightArm);
        lampBody.rotateAngleX += CSMath.toRadians(90);

        ModelRenderer lampHeart = new ModelRenderer(event.getRenderer().getEntityModel(), 32, 0);
        lampHeart.addBox(0f, 0f, 0f, 5f, 5f, 5f);
        lampHeart.copyModelAngles(event.getRenderer().getEntityModel().bipedRightArm);

        float bodyRotation = (float) CSMath.blend(event.getEntityLiving().prevRenderYawOffset, event.getEntityLiving().renderYawOffset, event.getPartialRenderTick(), 0, 1);

        event.getMatrixStack().rotate(TransformationHelper.quatFromXYZ(new Vector3f(180, bodyRotation, 0), true));
        event.getMatrixStack().translate(0, -1.41, 0);
        lampBody.render(event.getMatrixStack(), event.getBuffers().getBuffer(RenderType.getEntityTranslucent(texture)), 15728880, OverlayTexture.NO_OVERLAY);
        lampHeart.render(event.getMatrixStack(), event.getBuffers().getBuffer(RenderType.getEntityTranslucent(texture)), 15728880, OverlayTexture.NO_OVERLAY);
    }*/
}
