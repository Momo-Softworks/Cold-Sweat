package com.momosoftworks.coldsweat.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.client.event.RegisterModels;
import com.momosoftworks.coldsweat.common.item.ChameleonArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Renders chameleon armor with a translucent red/blue overlay based on its adaptive insulation.<br>
 * The chameleon equipment asset has no layers, so this layer is the only thing that renders it.
 */
public class ChameleonArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<? super S>> extends RenderLayer<S, M>
{
    public static final Identifier GREEN_LAYER_1_LOCATION = ColdSweat.createKey("textures/models/armor/chameleon_layer_1.png");
    public static final Identifier GREEN_LAYER_2_LOCATION = ColdSweat.createKey("textures/models/armor/chameleon_layer_2.png");
    public static final Identifier RED_LAYER_1_LOCATION = ColdSweat.createKey("textures/models/armor/chameleon_layer_1_red.png");
    public static final Identifier RED_LAYER_2_LOCATION = ColdSweat.createKey("textures/models/armor/chameleon_layer_2_red.png");
    public static final Identifier BLUE_LAYER_1_LOCATION = ColdSweat.createKey("textures/models/armor/chameleon_layer_1_blue.png");
    public static final Identifier BLUE_LAYER_2_LOCATION = ColdSweat.createKey("textures/models/armor/chameleon_layer_2_blue.png");

    public ChameleonArmorLayer(RenderLayerParent<S, M> renderer)
    {   super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot)
    {
        this.submitArmorPiece(poseStack, collector, state, state.chestEquipment, EquipmentSlot.CHEST, light);
        this.submitArmorPiece(poseStack, collector, state, state.legsEquipment, EquipmentSlot.LEGS, light);
        this.submitArmorPiece(poseStack, collector, state, state.feetEquipment, EquipmentSlot.FEET, light);
        this.submitArmorPiece(poseStack, collector, state, state.headEquipment, EquipmentSlot.HEAD, light);
    }

    protected void submitArmorPiece(PoseStack poseStack, SubmitNodeCollector collector, S state, ItemStack stack, EquipmentSlot slot, int light)
    {
        if (!(stack.getItem() instanceof ChameleonArmorItem)) return;

        HumanoidModel<HumanoidRenderState> model = RegisterModels.getChameleonArmorModel(slot);
        if (model == null) return;

        // Base (green) texture
        collector.submitModel(model, state, poseStack, RenderTypes.armorTranslucent(Color.GREEN.getLayer(slot)), light, OverlayTexture.NO_OVERLAY,
                              -1, null, state.outlineColor, null);

        // Red/blue overlay, based on the adaptive insulation factor
        double adaptiveFactor = AdaptiveInsulation.getFactorFromArmor(stack);
        float alpha = (float) Math.abs(adaptiveFactor);
        if (alpha > 0)
        {
            Identifier overlay = adaptiveFactor < 0 ? Color.BLUE.getLayer(slot) : Color.RED.getLayer(slot);
            collector.submitModel(model, state, poseStack, RenderTypes.armorTranslucent(overlay), light, OverlayTexture.NO_OVERLAY,
                                  ARGB.white(alpha), null, state.outlineColor, null);
        }

        // Enchantment glint
        if (stack.hasFoil())
        {   collector.submitModel(model, state, poseStack, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY,
                                  -1, null, state.outlineColor, null);
        }
    }

    public enum Color
    {
        GREEN(GREEN_LAYER_1_LOCATION, GREEN_LAYER_2_LOCATION),
        RED(RED_LAYER_1_LOCATION, RED_LAYER_2_LOCATION),
        BLUE(BLUE_LAYER_1_LOCATION, BLUE_LAYER_2_LOCATION);

        private final Identifier layer1;
        private final Identifier layer2;

        Color(Identifier layer1, Identifier layer2)
        {
            this.layer1 = layer1;
            this.layer2 = layer2;
        }

        public Identifier getLayer1()
        {   return layer1;
        }

        public Identifier getLayer2()
        {   return layer2;
        }

        public Identifier getLayer(EquipmentSlot slot)
        {   return slot == EquipmentSlot.LEGS ? layer2 : layer1;
        }
    }
}
