package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.client.renderer.entity.ChameleonEntityRenderer;
import dev.momostudios.coldsweat.client.renderer.model.armor.*;
import dev.momostudios.coldsweat.client.renderer.model.entity.ChameleonModel;
import dev.momostudios.coldsweat.core.init.EntityInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegisterModels
{
    public static HoglinHeadpieceModel<?> HOGLIN_HEADPIECE_MODEL = null;
    public static HoglinTunicModel<?> HOGLIN_TUNIC_MODEL = null;
    public static HoglinHoovesModel<?> HOGLIN_HOOVES_MODEL = null;
    public static HoglinTrousersModel<?> HOGLIN_TROUSERS_MODEL = null;

    public static GoatCapModel<?> GOAT_CAP_MODEL = null;
    public static GoatParkaModel<?> GOAT_PARKA_MODEL = null;
    public static GoatPantsModel<?> GOAT_PANTS_MODEL = null;
    public static GoatBootsModel<?> GOAT_BOOTS_MODEL = null;

    public static void checkForInitModels()
    {
        if (HOGLIN_HEADPIECE_MODEL != null) return;

        EntityModelSet mcModels = Minecraft.getInstance().getEntityModels();

        HOGLIN_HEADPIECE_MODEL = new HoglinHeadpieceModel<>(mcModels.bakeLayer(HoglinHeadpieceModel.LAYER_LOCATION));
        HOGLIN_TUNIC_MODEL = new HoglinTunicModel<>(mcModels.bakeLayer(HoglinTunicModel.LAYER_LOCATION));
        HOGLIN_HOOVES_MODEL = new HoglinHoovesModel<>(mcModels.bakeLayer(HoglinHoovesModel.LAYER_LOCATION));
        HOGLIN_TROUSERS_MODEL = new HoglinTrousersModel<>(mcModels.bakeLayer(HoglinTrousersModel.LAYER_LOCATION));

        GOAT_CAP_MODEL = new GoatCapModel<>(mcModels.bakeLayer(GoatCapModel.LAYER_LOCATION));
        GOAT_PARKA_MODEL = new GoatParkaModel<>(mcModels.bakeLayer(GoatParkaModel.LAYER_LOCATION));
        GOAT_PANTS_MODEL = new GoatPantsModel<>(mcModels.bakeLayer(GoatPantsModel.LAYER_LOCATION));
        GOAT_BOOTS_MODEL = new GoatBootsModel<>(mcModels.bakeLayer(GoatBootsModel.LAYER_LOCATION));
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(ChameleonModel.LAYER_LOCATION, ChameleonModel::createBodyLayer);

        event.registerLayerDefinition(HoglinHeadpieceModel.LAYER_LOCATION, HoglinHeadpieceModel::createArmorLayer);
        event.registerLayerDefinition(HoglinTunicModel.LAYER_LOCATION, HoglinTunicModel::createArmorLayer);
        event.registerLayerDefinition(HoglinHoovesModel.LAYER_LOCATION, HoglinHoovesModel::createArmorLayer);
        event.registerLayerDefinition(HoglinTrousersModel.LAYER_LOCATION, HoglinTrousersModel::createArmorLayer);

        event.registerLayerDefinition(GoatCapModel.LAYER_LOCATION, GoatCapModel::createArmorLayer);
        event.registerLayerDefinition(GoatParkaModel.LAYER_LOCATION, GoatParkaModel::createArmorLayer);
        event.registerLayerDefinition(GoatPantsModel.LAYER_LOCATION, GoatPantsModel::createArmorLayer);
        event.registerLayerDefinition(GoatBootsModel.LAYER_LOCATION, GoatBootsModel::createArmorLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(EntityInit.CHAMELEON.get(), ChameleonEntityRenderer::new);
    }
}
