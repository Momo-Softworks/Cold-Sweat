package com.momosoftworks.coldsweat.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.client.renderer.entity.ChameleonEntityRenderer;
import com.momosoftworks.coldsweat.client.renderer.entity.state.ChameleonRenderState;
import com.momosoftworks.coldsweat.client.renderer.model.entity.ChameleonModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;

/**
 * The tongue uses a cutout render type, so it's drawn by a separate tongue-only model
 */
public class ChameleonTongueLayer extends RenderLayer<ChameleonRenderState, ChameleonModel>
{
    private final ChameleonModel adultTongue;
    private final ChameleonModel babyTongue;

    public ChameleonTongueLayer(RenderLayerParent<ChameleonRenderState, ChameleonModel> parentLayer, ChameleonModel adultTongue, ChameleonModel babyTongue)
    {
        super(parentLayer);
        this.adultTongue = adultTongue;
        this.babyTongue = babyTongue;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, ChameleonRenderState state, float yRot, float xRot)
    {
        if (!state.tongueVisible) return;
        ChameleonModel model = state.isBaby ? this.babyTongue : this.adultTongue;
        collector.submitModel(model, state, poseStack, RenderTypes.entityCutout(ChameleonEntityRenderer.CHAMELEON_GREEN), light,
                              LivingEntityRenderer.getOverlayCoords(state, 0), ARGB.white(state.opacity), null, state.outlineColor, null);
    }
}
