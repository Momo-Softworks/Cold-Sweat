package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.event.core.EdiblesRegisterEvent;
import dev.momostudios.coldsweat.common.entity.data.edible.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RegisterChameleonEdibles
{
    @SubscribeEvent
    public static void onWorldLoaded(LevelEvent.Load event)
    {
        if (event.getLevel().isClientSide()) return;

        EdiblesRegisterEvent edibleEvent = new EdiblesRegisterEvent();
        MinecraftForge.EVENT_BUS.post(edibleEvent);
    }

    @SubscribeEvent
    public static void onEdiblesRegister(EdiblesRegisterEvent event)
    {
        event.registerEdible(new HotBiomeEdible(), Items.MAGMA_CREAM, Items.NETHER_WART, Items.CACTUS);
        event.registerEdible(new ColdBiomeEdible(), Items.SNOWBALL, Items.BEETROOT, Items.SWEET_BERRIES);
        event.registerEdible(new HumidBiomeEdible(), Items.SLIME_BALL, Items.INK_SAC, Items.COCOA_BEANS);
        event.registerEdible(new AridBiomeEdible(), Items.DRIED_KELP, Items.DEAD_BUSH, Items.RABBIT_FOOT);
        event.registerEdible(new HealingEdible(), Items.SPIDER_EYE, Items.FERMENTED_SPIDER_EYE);
        event.registerEdible(new HealingEdible(), ItemTags.FISHES);
    }
}
