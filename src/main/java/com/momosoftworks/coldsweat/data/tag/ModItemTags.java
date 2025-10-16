package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.init.InitDynamicTagsEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ModItemTags
{
    public static final TagKey<Item> BOILER_VALID = createTag("boiler_valid");
    public static final TagKey<Item> ICEBOX_VALID = createTag("icebox_valid");

    public static final TagKey<Item> NOT_INSULATABLE = createTag("not_insulatable");

    public static final TagKey<Item> CHAMELEON_TAMING = createTag("chameleon/taming");
    public static final TagKey<Item> CHAMELEON_HOT = createTag("chameleon/find_hot_biomes");
    public static final TagKey<Item> CHAMELEON_COLD = createTag("chameleon/find_cold_biomes");
    public static final TagKey<Item> CHAMELEON_HUMID = createTag("chameleon/find_humid_biomes");

    public static final TagKey<Item> ENCASES_SMOKESTACK = createTag("encases_smokestack");
    public static final TagKey<Item> GROWS_SOUL_STALK = createTag("grows_soul_stalk");

    public static final TagKey<Item> HOGLIN_LEATHERS = createForgeTag("leathers/hoglin");
    public static final TagKey<Item> GOAT_FURS = createForgeTag("furs/goat");
    public static final TagKey<Item> CHAMELEON_SCALES = createForgeTag("scales/chameleon");

    public static final TagKey<Item> FOOD = createForgeTag("food");

    @SubscribeEvent
    public static void initDynamicTags(InitDynamicTagsEvent event)
    {
        event.fillTag(FOOD, Item::isEdible);
    }

    private static TagKey<Item> createTag(String name)
    {   return ItemTags.create(new ResourceLocation(ColdSweat.MOD_ID, name));
    }

    private static TagKey<Item> createForgeTag(String name)
    {   return ItemTags.create(new ResourceLocation("forge", name));
    }
}
