package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.util.tag.ModTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegisterTags
{
    @SubscribeEvent
    public static void generateTags(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer())
        {
            event.getGenerator().addProvider(new ModTagProvider.ItemTags(generator, new ForgeBlockTagsProvider(generator, event.getExistingFileHelper()), event.getExistingFileHelper()));
        }
    }
}
