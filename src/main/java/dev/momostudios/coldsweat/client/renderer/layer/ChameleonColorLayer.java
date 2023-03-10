package dev.momostudios.coldsweat.client.renderer.layer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.momostudios.coldsweat.client.renderer.ChameleonEntityRenderer;
import dev.momostudios.coldsweat.client.renderer.model.ChameleonModel;
import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
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
        if (entity instanceof ChameleonEntity chameleon)
        {
            // Overlay color

            float midTemp = (float) CSMath.average(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get());

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            if (!CSMath.isInRange(chameleon.getTemperature(), CSMath.average(ConfigSettings.MIN_TEMP.get(), midTemp), CSMath.average(ConfigSettings.MAX_TEMP.get(), midTemp)))
            {
                if (chameleon.getTemperature() > midTemp)
                {
                    VertexConsumer vertexConsumer = bufferSource.getBuffer(CHAMELEON_RED);
                    float alpha = chameleon.hurtTime > 0 ? 0 : (float) CSMath.blend(0f, 1f, chameleon.getTemperature(), CSMath.average(ConfigSettings.MAX_TEMP.get(), midTemp), ConfigSettings.MAX_TEMP.get());
                    if (alpha > 0)
                        ((ChameleonModel<ChameleonEntity>) this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha * chameleon.opacity, true);
                }
                else
                {
                    VertexConsumer vertexConsumer = bufferSource.getBuffer(CHAMELEON_BLUE);
                    float alpha = chameleon.hurtTime > 0 ? 0 : (float) CSMath.blend(1f, 0f, chameleon.getTemperature(), ConfigSettings.MIN_TEMP.get(), CSMath.average(ConfigSettings.MIN_TEMP.get(), midTemp));
                    if (alpha > 0)
                        ((ChameleonModel<ChameleonEntity>) this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha * chameleon.opacity, true);
                }
            }

            // Overlay shedding skin
            if (chameleon.isShedding())
            {
                VertexConsumer vertexConsumer = bufferSource.getBuffer(CHAMELEON_SHED);
                float alpha = chameleon.hurtTime > 0 || chameleon.getLastShed() == 0 ? 0 : CSMath.blend(0, 0.7f, chameleon.getAgeSecs() * 20 - chameleon.getLastShed(), 0, chameleon.getTimeToShed());
                if (alpha > 0)
                    ((ChameleonModel<ChameleonEntity>) this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha * chameleon.opacity, true);
            }
            RenderSystem.disableBlend();
        }
    }
}
