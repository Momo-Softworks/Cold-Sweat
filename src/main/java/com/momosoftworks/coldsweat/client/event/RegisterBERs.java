package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.client.renderer.block.HearthBlockEntityRenderer;
import com.momosoftworks.coldsweat.client.renderer.block.IceboxBlockEntityRenderer;
import com.momosoftworks.coldsweat.core.init.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class RegisterBERs
{
    @SubscribeEvent
    public static void registerBERs(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(ModBlockEntities.ICEBOX.get(), IceboxBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEARTH.get(), HearthBlockEntityRenderer::new);
    }
}
