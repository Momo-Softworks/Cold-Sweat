package dev.momostudios.coldsweat.util.tag;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags
{
    public static class Items
    {
        public static final TagKey<Item> CHAMELEON_TAMING  = create("chameleon_taming");
        public static final TagKey<Item> CHAMELEON_HEALING = create("chameleon_healing");
        public static final TagKey<Item> CHAMELEON_HOT     = create("chameleon_hot");
        public static final TagKey<Item> CHAMELEON_COLD    = create("chameleon_cold");
        public static final TagKey<Item> CHAMELEON_HUMID   = create("chameleon_humid");
        public static final TagKey<Item> CHAMELEON_ARID    = create("chameleon_arid");
        public static final TagKey<Item> HEARTH_FUEL       = create("hearth_fuel");
        public static final TagKey<Item> BOILER_FUEL       = create("boiler_fuel");
        public static final TagKey<Item> ICEBOX_FUEL       = create("icebox_fuel");

        private static TagKey<Item> create(String name)
        {
            return ItemTags.create(new ResourceLocation(ColdSweat.MOD_ID, name));
        }
    }

    public static class Blocks
    {
    }
}
