package com.momosoftworks.coldsweat.client.renderer;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.capability.handler.ShearableFurManager;
import com.momosoftworks.coldsweat.common.capability.shearing.IShearableCap;
import net.minecraft.client.renderer.entity.GoatRenderer;
import net.minecraft.client.renderer.entity.state.GoatRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.animal.goat.Goat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

/**
 * Entity data needed by renderers, which only have access to render states
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class RenderStateData
{
    public static final ContextKey<Boolean> GOAT_SHEARED = new ContextKey<>(ColdSweat.createKey("goat_sheared"));

    @SubscribeEvent
    public static void registerModifiers(RegisterRenderStateModifiersEvent event)
    {
        event.registerEntityModifier(GoatRenderer.class, (Goat goat, GoatRenderState state) ->
        {   state.setRenderData(GOAT_SHEARED, ShearableFurManager.getFurCap(goat).map(IShearableCap::isSheared).orElse(false));
        });
    }
}
