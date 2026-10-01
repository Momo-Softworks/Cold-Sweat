package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.client.renderer.block.HearthBlockEntityRenderer;
import com.momosoftworks.coldsweat.client.renderer.block.IceboxBlockEntityRenderer;
import com.momosoftworks.coldsweat.client.renderer.entity.ChameleonEntityRenderer;
import com.momosoftworks.coldsweat.client.renderer.item.SoulSpringLampRenderer;
import com.momosoftworks.coldsweat.client.renderer.layer.ChameleonArmorLayer;
import com.momosoftworks.coldsweat.client.renderer.model.armor.*;
import com.momosoftworks.coldsweat.client.renderer.model.entity.ChameleonModel;
import com.momosoftworks.coldsweat.core.init.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(value = Dist.CLIENT)
public class RegisterModels
{
    public static HoglinHelmetModel HOGLIN_HELMET_MODEL = null;
    public static HoglinChestplateModel HOGLIN_CHESTPLATE_MODEL = null;
    public static HoglinLeggingsModel HOGLIN_LEGGINGS_MODEL = null;
    public static HoglinBootsModel HOGLIN_BOOTS_MODEL = null;

    public static GoatHelmetModel GOAT_HELMET_MODEL = null;
    public static GoatChestplateModel GOAT_CHESTPLATE_MODEL = null;
    public static GoatLeggingsModel GOAT_LEGGINGS_MODEL = null;
    public static GoatBootsModel GOAT_BOOTS_MODEL = null;

    public static ChameleonHelmetModel CHAMELEON_HELMET_MODEL = null;
    public static ChameleonChestplateModel CHAMELEON_CHESTPLATE_MODEL = null;
    public static ChameleonLeggingsModel CHAMELEON_LEGGINGS_MODEL = null;
    public static ChameleonBootsModel CHAMELEON_BOOTS_MODEL = null;

    public static void checkForInitModels()
    {
        if (HOGLIN_HELMET_MODEL != null) return;

        EntityModelSet mcModels = Minecraft.getInstance().getEntityModels();

        HOGLIN_HELMET_MODEL = new HoglinHelmetModel(mcModels.bakeLayer(HoglinHelmetModel.LAYER_LOCATION));
        HOGLIN_CHESTPLATE_MODEL = new HoglinChestplateModel(mcModels.bakeLayer(HoglinChestplateModel.LAYER_LOCATION));
        HOGLIN_BOOTS_MODEL = new HoglinBootsModel(mcModels.bakeLayer(HoglinBootsModel.LAYER_LOCATION));
        HOGLIN_LEGGINGS_MODEL = new HoglinLeggingsModel(mcModels.bakeLayer(HoglinLeggingsModel.LAYER_LOCATION));

        GOAT_HELMET_MODEL = new GoatHelmetModel(mcModels.bakeLayer(GoatHelmetModel.LAYER_LOCATION));
        GOAT_CHESTPLATE_MODEL = new GoatChestplateModel(mcModels.bakeLayer(GoatChestplateModel.LAYER_LOCATION));
        GOAT_LEGGINGS_MODEL = new GoatLeggingsModel(mcModels.bakeLayer(GoatLeggingsModel.LAYER_LOCATION));
        GOAT_BOOTS_MODEL = new GoatBootsModel(mcModels.bakeLayer(GoatBootsModel.LAYER_LOCATION));

        CHAMELEON_HELMET_MODEL = new ChameleonHelmetModel(mcModels.bakeLayer(ChameleonHelmetModel.LAYER_LOCATION));
        CHAMELEON_CHESTPLATE_MODEL = new ChameleonChestplateModel(mcModels.bakeLayer(ChameleonChestplateModel.LAYER_LOCATION));
        CHAMELEON_LEGGINGS_MODEL = new ChameleonLeggingsModel(mcModels.bakeLayer(ChameleonLeggingsModel.LAYER_LOCATION));
        CHAMELEON_BOOTS_MODEL = new ChameleonBootsModel(mcModels.bakeLayer(ChameleonBootsModel.LAYER_LOCATION));
    }

    @Nullable
    public static HumanoidModel<HumanoidRenderState> getHoglinArmorModel(EquipmentSlot slot)
    {
        checkForInitModels();
        return switch (slot)
        {
            case HEAD -> HOGLIN_HELMET_MODEL;
            case CHEST -> HOGLIN_CHESTPLATE_MODEL;
            case LEGS -> HOGLIN_LEGGINGS_MODEL;
            case FEET -> HOGLIN_BOOTS_MODEL;
            default -> null;
        };
    }

    @Nullable
    public static HumanoidModel<HumanoidRenderState> getGoatArmorModel(EquipmentSlot slot)
    {
        checkForInitModels();
        return switch (slot)
        {
            case HEAD -> GOAT_HELMET_MODEL;
            case CHEST -> GOAT_CHESTPLATE_MODEL;
            case LEGS -> GOAT_LEGGINGS_MODEL;
            case FEET -> GOAT_BOOTS_MODEL;
            default -> null;
        };
    }

    @Nullable
    public static HumanoidModel<HumanoidRenderState> getChameleonArmorModel(EquipmentSlot slot)
    {
        checkForInitModels();
        return switch (slot)
        {
            case HEAD -> CHAMELEON_HELMET_MODEL;
            case CHEST -> CHAMELEON_CHESTPLATE_MODEL;
            case LEGS -> CHAMELEON_LEGGINGS_MODEL;
            case FEET -> CHAMELEON_BOOTS_MODEL;
            default -> null;
        };
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(ChameleonModel.LAYER_LOCATION, ChameleonModel::createBodyLayer);
        event.registerLayerDefinition(ChameleonModel.BABY_LAYER_LOCATION, () -> ChameleonModel.createBodyLayer().apply(ChameleonModel.BABY_TRANSFORMER));

        event.registerLayerDefinition(HoglinHelmetModel.LAYER_LOCATION, HoglinHelmetModel::createArmorLayer);
        event.registerLayerDefinition(HoglinChestplateModel.LAYER_LOCATION, HoglinChestplateModel::createArmorLayer);
        event.registerLayerDefinition(HoglinBootsModel.LAYER_LOCATION, HoglinBootsModel::createArmorLayer);
        event.registerLayerDefinition(HoglinLeggingsModel.LAYER_LOCATION, HoglinLeggingsModel::createArmorLayer);

        event.registerLayerDefinition(GoatHelmetModel.LAYER_LOCATION, GoatHelmetModel::createArmorLayer);
        event.registerLayerDefinition(GoatChestplateModel.LAYER_LOCATION, GoatChestplateModel::createArmorLayer);
        event.registerLayerDefinition(GoatLeggingsModel.LAYER_LOCATION, GoatLeggingsModel::createArmorLayer);
        event.registerLayerDefinition(GoatBootsModel.LAYER_LOCATION, GoatBootsModel::createArmorLayer);

        event.registerLayerDefinition(ChameleonHelmetModel.LAYER_LOCATION, ChameleonHelmetModel::createArmorLayer);
        event.registerLayerDefinition(ChameleonChestplateModel.LAYER_LOCATION, ChameleonChestplateModel::createArmorLayer);
        event.registerLayerDefinition(ChameleonLeggingsModel.LAYER_LOCATION, ChameleonLeggingsModel::createArmorLayer);
        event.registerLayerDefinition(ChameleonBootsModel.LAYER_LOCATION, ChameleonBootsModel::createArmorLayer);

        event.registerLayerDefinition(IceboxBlockEntityRenderer.LAYER_LOCATION, IceboxBlockEntityRenderer::createBodyLayer);
        event.registerLayerDefinition(HearthBlockEntityRenderer.LAYER_LOCATION, HearthBlockEntityRenderer::createBodyLayer);
        event.registerLayerDefinition(SoulSpringLampRenderer.LAYER_LOCATION, SoulSpringLampRenderer::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ModEntities.CHAMELEON.get(), ChameleonEntityRenderer::new);
    }

    /**
     * Chameleon armor is drawn by {@link ChameleonArmorLayer} on every humanoid renderer
     */
    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addLayers(EntityRenderersEvent.AddLayers event)
    {
        for (PlayerModelType skin : event.getSkins())
        {
            LivingEntityRenderer playerRenderer = event.getPlayerRenderer(skin);
            if (playerRenderer != null)
            {   playerRenderer.addLayer(new ChameleonArmorLayer<>(playerRenderer));
            }
            LivingEntityRenderer mannequinRenderer = event.getMannequinRenderer(skin);
            if (mannequinRenderer != null)
            {   mannequinRenderer.addLayer(new ChameleonArmorLayer<>(mannequinRenderer));
            }
        }
        for (EntityType<?> type : event.getEntityTypes())
        {
            EntityRenderer<?, ?> renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer livingRenderer && livingRenderer.getModel() instanceof HumanoidModel<?>)
            {   livingRenderer.addLayer(new ChameleonArmorLayer<>(livingRenderer));
            }
        }
    }
}
