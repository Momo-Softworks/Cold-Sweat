package dev.momostudios.coldsweat.client.renderer.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.renderer.ChameleonEntityRenderer;
import dev.momostudios.coldsweat.client.renderer.model.ChameleonModel;
import dev.momostudios.coldsweat.common.entity.Chameleon;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;

public class ChameleonColorLayer<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M>
{
    private static final RenderType CHAMELEON_SHED = RenderType.entityTranslucent(ChameleonEntityRenderer.CHAMELEON_SHED);
    private static final RenderType CHAMELEON_RED = RenderType.entityTranslucent(ChameleonEntityRenderer.CHAMELEON_RED);
    private static final RenderType CHAMELEON_BLUE = RenderType.entityTranslucent(ChameleonEntityRenderer.CHAMELEON_BLUE);

    public ChameleonColorLayer(RenderLayerParent<T, M> parentLayer)
    {
        super(parentLayer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, T entity, float p_117353_, float p_117354_, float partialTick, float p_117356_, float p_117357_, float p_117358_)
    {
        if (entity instanceof Chameleon chameleon)
        {
            // Overlay color
            ConfigSettings config = ConfigSettings.getInstance();
            float midTemp = (float) CSMath.average(config.minTemp, config.maxTemp);
            if (!CSMath.isInRange(chameleon.getTemperature(), CSMath.average(config.minTemp, midTemp), CSMath.average(config.maxTemp, midTemp)))
            {
                if (chameleon.getTemperature() > midTemp)
                {
                    VertexConsumer vertexConsumer = bufferSource.getBuffer(CHAMELEON_RED);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    float alpha = chameleon.hurtTime > 0 ? 0 : (float) CSMath.blend(0f, 1f, chameleon.getTemperature(), CSMath.average(config.maxTemp, midTemp), config.maxTemp);
                    ((ChameleonModel<Chameleon>) this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha * chameleon.opacity, true);
                    RenderSystem.disableBlend();
                }
                else if (chameleon.getTemperature() < midTemp)
                {
                    VertexConsumer vertexConsumer = bufferSource.getBuffer(CHAMELEON_BLUE);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    float alpha = chameleon.hurtTime > 0 ? 0 : (float) CSMath.blend(1f, 0f, chameleon.getTemperature(), config.minTemp, CSMath.average(config.minTemp, midTemp));
                    ((ChameleonModel<Chameleon>) this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha * chameleon.opacity, true);
                    RenderSystem.disableBlend();
                }
            }

            // Overlay shedding skin
            if (chameleon.isShedding())
            {
                VertexConsumer vertexConsumer = bufferSource.getBuffer(CHAMELEON_SHED);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                float alpha = chameleon.hurtTime > 0 ? 0 : chameleon.getLastShed() == 0 ? 0 : CSMath.blend(0, 0.7f, chameleon.tickCount - chameleon.getLastShed(), 0, chameleon.getTimeToShed());
                ((ChameleonModel<Chameleon>) this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha * chameleon.opacity, true);
                RenderSystem.disableBlend();
            }
        }
    }
}
