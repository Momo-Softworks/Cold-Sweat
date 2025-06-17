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
    public static void initDynamicTags(InitDynamicTagsEvent event)
    {
        event.fillTag(HAS_CEILING, DimensionType::hasCeiling, Registries.DIMENSION_TYPE);
        event.fillTag(HAS_SKY, dimensionType -> !dimensionType.hasCeiling(), Registries.DIMENSION_TYPE);
        event.fillTag(NATURAL, DimensionType::natural, Registries.DIMENSION_TYPE);
        event.fillTag(UNNATURAL, dimensionType -> !dimensionType.natural(), Registries.DIMENSION_TYPE);
        event.fillTag(ULTRAWARM, DimensionType::ultraWarm, Registries.DIMENSION_TYPE);
        event.fillTag(BED_WORKS, DimensionType::bedWorks, Registries.DIMENSION_TYPE);
        event.fillTag(RESPAWN_ANCHOR_WORKS, DimensionType::respawnAnchorWorks, Registries.DIMENSION_TYPE);
        event.fillTag(PIGLIN_SAFE, DimensionType::piglinSafe, Registries.DIMENSION_TYPE);
        event.fillTag(HAS_SKYLIGHT, DimensionType::hasSkyLight, Registries.DIMENSION_TYPE);
        event.fillTag(HAS_RAIDS, DimensionType::hasRaids, Registries.DIMENSION_TYPE);
        event.fillTag(OVERWORLD_LIKE,
                      dimensionType -> !dimensionType.hasCeiling()
                                    && dimensionType.natural()
                                    && dimensionType.hasSkyLight()
                                    && !dimensionType.ultraWarm(),
                      Registries.DIMENSION_TYPE);
    }
}
