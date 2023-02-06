package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.client.renderer.ChameleonEntityRenderer;
import dev.momostudios.coldsweat.client.renderer.model.ChameleonModel;
import dev.momostudios.coldsweat.core.init.EntityInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegisterModels
{
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(ChameleonModel.LAYER_LOCATION, ChameleonModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(EntityInit.CHAMELEON.get(), ChameleonEntityRenderer::new);
    }
}
