package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.util.tag.ModTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegisterTags
{
    @SubscribeEvent
    public static void generateTags(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        event.getGenerator().addProvider(event.includeServer(), new ModTagProvider.ItemTags(generator, new ForgeBlockTagsProvider(generator, event.getExistingFileHelper()), event.getExistingFileHelper()));
    }
}
