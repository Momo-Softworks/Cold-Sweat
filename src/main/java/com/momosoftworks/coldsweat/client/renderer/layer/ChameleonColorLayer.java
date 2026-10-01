package com.momosoftworks.coldsweat.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.client.renderer.entity.ChameleonEntityRenderer;
import com.momosoftworks.coldsweat.client.renderer.entity.state.ChameleonRenderState;
import com.momosoftworks.coldsweat.client.renderer.model.entity.ChameleonModel;
import com.momosoftworks.coldsweat.common.entity.Chameleon;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;

/**
 * Red/blue temperature tint and shedding skin overlays
 */
public class ChameleonColorLayer extends RenderLayer<ChameleonRenderState, ChameleonModel>
{
    private static final RenderType CHAMELEON_SHED = RenderTypes.entityTranslucent(ChameleonEntityRenderer.CHAMELEON_SHED);
    private static final RenderType CHAMELEON_RED = RenderTypes.entityTranslucent(ChameleonEntityRenderer.CHAMELEON_RED);
    private static final RenderType CHAMELEON_BLUE = RenderTypes.entityTranslucent(ChameleonEntityRenderer.CHAMELEON_BLUE);

    public ChameleonColorLayer(RenderLayerParent<ChameleonRenderState, ChameleonModel> parentLayer)
    {   super(parentLayer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, ChameleonRenderState state, float yRot, float xRot)
    {
        Chameleon chameleon = state.chameleon;
        if (chameleon == null) return;

        // Overlay color
        float midTemp = (float) CSMath.average(ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get());

        if (!CSMath.betweenInclusive(chameleon.getTemperature(), CSMath.average(ConfigSettings.MIN_TEMP.get(), midTemp), CSMath.average(ConfigSettings.MAX_TEMP.get(), midTemp)))
        {
            if (chameleon.getTemperature() > midTemp)
            {
                float alpha = chameleon.hurtTime > 0 ? 0 : (float) CSMath.blend(0f, 1f, chameleon.getTemperature(), CSMath.average(ConfigSettings.MAX_TEMP.get(), midTemp), ConfigSettings.MAX_TEMP.get());
                this.submitOverlay(poseStack, collector, light, state, CHAMELEON_RED, alpha);
            }
            else
            {
                float alpha = chameleon.hurtTime > 0 ? 0 : (float) CSMath.blend(1f, 0f, chameleon.getTemperature(), ConfigSettings.MIN_TEMP.get(), CSMath.average(ConfigSettings.MIN_TEMP.get(), midTemp));
                this.submitOverlay(poseStack, collector, light, state, CHAMELEON_BLUE, alpha);
            }
        }

        // Overlay shedding skin
        if (chameleon.isShedding())
        {
            float alpha = chameleon.hurtTime > 0 ? 0 : CSMath.blend(0, 0.7f, chameleon.getShedTime(), 0, chameleon.getTimeToShed());
            this.submitOverlay(poseStack, collector, light, state, CHAMELEON_SHED, alpha);
        }
    }

    private void submitOverlay(PoseStack poseStack, SubmitNodeCollector collector, int light, ChameleonRenderState state, RenderType renderType, float alpha)
    {
        if (alpha <= 0) return;
        collector.submitModel(this.getParentModel(), state, poseStack, renderType, light, OverlayTexture.NO_OVERLAY,
                              ARGB.white(alpha * state.opacity), null, state.outlineColor, null);
    }
}
