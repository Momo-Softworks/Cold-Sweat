package dev.momostudios.coldsweat.client.itemproperties;

import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.util.PlayerHelper;
import dev.momostudios.coldsweat.util.registrylists.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ItemRenderProperties
{
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            ItemProperties.register(ModItems.HELLSPRING_LAMP, new ResourceLocation("cold_sweat:hellspring_state"), (stack, level, entity, p_174679_) ->
            {
                if (stack.getOrCreateTag().getBoolean("isOn"))
                {
                    return stack.getOrCreateTag().getInt("fuel") > 43 ? 3 :
                            stack.getOrCreateTag().getInt("fuel") > 22 ? 2 : 1;
                } else
                {
                    return 0;
                }
            });

            ItemProperties.register(ModItems.THERMOMETER, new ResourceLocation("cold_sweat:temperature"), (stack, level, entity, p_174679_) ->
            {
                Player player = Minecraft.getInstance().player;
                if (player != null)
                {
                    ConfigCache config = ConfigCache.getInstance();
                    float minTemp = (float) config.minTemp;
                    float maxTemp = (float) config.maxTemp;

                    float ambientTemp = (float) PlayerHelper.getTemperature(player, PlayerHelper.Types.AMBIENT).get();

                    float ambientAdjusted = ambientTemp - minTemp;
                    float tempScaleFactor = 1 / ((maxTemp - minTemp) / 2);

                    return ambientAdjusted * tempScaleFactor - 1;
                }
                return 1;
            });
        });
    }
}
