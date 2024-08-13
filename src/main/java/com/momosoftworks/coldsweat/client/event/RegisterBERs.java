package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.client.renderer.block.IceboxBlockEntityRenderer;
import com.momosoftworks.coldsweat.core.init.BlockEntityInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegisterBERs
{
    @SubscribeEvent
    public static void registerBERs(FMLClientSetupEvent event)
    {
        ClientRegistry.bindTileEntityRenderer(BlockEntityInit.ICEBOX_BLOCK_ENTITY_TYPE.get(), IceboxBlockEntityRenderer::new);
    }
}
