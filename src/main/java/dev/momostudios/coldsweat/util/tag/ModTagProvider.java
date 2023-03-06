package dev.momostudios.coldsweat.util.tag;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.entity.data.edible.*;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Map;

public class ModTagProvider
{
    public static class ItemTags extends ItemTagsProvider
    {
        public ItemTags(DataGenerator generator, BlockTagsProvider blocks, ExistingFileHelper helper)
        {
            super(generator, blocks, ColdSweat.MOD_ID, helper);
        }

        @Override
        public void addTags()
        {
            this.tag(ModTags.Items.CHAMELEON_TAMING).add(ConfigSettings.CHAMELEON_TAME_ITEMS.get().keySet().toArray(new Item[0]));
            this.tag(ModTags.Items.CHAMELEON_HEALING).add(ChameleonEdibles.EDIBLES.entrySet().stream().filter(entry -> entry.getValue() instanceof HealingEdible).map(Map.Entry::getKey).toArray(Item[]::new));
            this.tag(ModTags.Items.CHAMELEON_HOT).add(ChameleonEdibles.EDIBLES.entrySet().stream().filter(entry -> entry.getValue() instanceof HotBiomeEdible).map(Map.Entry::getKey).toArray(Item[]::new));
            this.tag(ModTags.Items.CHAMELEON_COLD).add(ChameleonEdibles.EDIBLES.entrySet().stream().filter(entry -> entry.getValue() instanceof ColdBiomeEdible).map(Map.Entry::getKey).toArray(Item[]::new));
            this.tag(ModTags.Items.CHAMELEON_HUMID).add(ChameleonEdibles.EDIBLES.entrySet().stream().filter(entry -> entry.getValue() instanceof HumidBiomeEdible).map(Map.Entry::getKey).toArray(Item[]::new));
            this.tag(ModTags.Items.CHAMELEON_ARID).add(ChameleonEdibles.EDIBLES.entrySet().stream().filter(entry -> entry.getValue() instanceof AridBiomeEdible).map(Map.Entry::getKey).toArray(Item[]::new));
            this.tag(ModTags.Items.HEARTH_FUEL).add(ConfigSettings.HEARTH_FUEL.get().keySet().toArray(Item[]::new));
            this.tag(ModTags.Items.BOILER_FUEL).add(ConfigSettings.BOILER_FUEL.get().keySet().toArray(Item[]::new));
            this.tag(ModTags.Items.ICEBOX_FUEL).add(ConfigSettings.ICEBOX_FUEL.get().keySet().toArray(Item[]::new));
        }
    }
}
