package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.init.InitDynamicTagsEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class ModDimensionTags
{
    public static final TagKey<DimensionType> SOUL_LAMP_VALID = createTag("soulspring_lamp_valid");

    public static final TagKey<DimensionType> HAS_CEILING = createForgeTag("has_ceiling");
    public static final TagKey<DimensionType> HAS_SKY = createForgeTag("has_sky");
    public static final TagKey<DimensionType> NATURAL = createForgeTag("natural");
    public static final TagKey<DimensionType> UNNATURAL = createForgeTag("unnatural");
    public static final TagKey<DimensionType> ULTRAWARM = createForgeTag("ultrawarm");
    public static final TagKey<DimensionType> BED_WORKS = createForgeTag("bed_works");
    public static final TagKey<DimensionType> RESPAWN_ANCHOR_WORKS = createForgeTag("respawn_anchor_works");
    public static final TagKey<DimensionType> PIGLIN_SAFE = createForgeTag("piglin_safe");
    public static final TagKey<DimensionType> HAS_SKYLIGHT = createForgeTag("has_skylight");
    public static final TagKey<DimensionType> HAS_RAIDS = createForgeTag("has_raids");
    public static final TagKey<DimensionType> OVERWORLD_LIKE = createForgeTag("overworld_like");

    private static TagKey<DimensionType> createTag(String name)
    {   return TagKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, name));
    }

    private static TagKey<DimensionType> createForgeTag(String name)
    {   return TagKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath("c", name));
    }


    @SubscribeEvent
    public static void initDynamicTags(InitDynamicTagsEvent<DimensionType> event)
    {
        event.fillTag(HAS_CEILING, DimensionType::hasCeiling);
        event.fillTag(HAS_SKY, dim -> !dim.hasCeiling());
        event.fillTag(NATURAL, DimensionType::natural);
        event.fillTag(UNNATURAL, dim -> !dim.natural());
        event.fillTag(ULTRAWARM, DimensionType::ultraWarm);
        event.fillTag(BED_WORKS, DimensionType::bedWorks);
        event.fillTag(RESPAWN_ANCHOR_WORKS, DimensionType::respawnAnchorWorks);
        event.fillTag(PIGLIN_SAFE, DimensionType::piglinSafe);
        event.fillTag(HAS_SKYLIGHT, DimensionType::hasSkyLight);
        event.fillTag(HAS_RAIDS, DimensionType::hasRaids);
        event.fillTag(OVERWORLD_LIKE, dim -> !dim.hasCeiling() && dim.natural() && dim.hasSkyLight() && !dim.ultraWarm());
    }
}