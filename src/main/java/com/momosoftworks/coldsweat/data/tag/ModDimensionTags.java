package com.momosoftworks.coldsweat.data.tag;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.init.InitDynamicTagsEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.ServerConfigsLoadedEvent;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber
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
    {   return TagKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(ColdSweat.MOD_ID, name));
    }

    private static TagKey<DimensionType> createForgeTag(String name)
    {   return TagKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("forge", name));
    }


    @SubscribeEvent
    public static void initDynamicTags(InitDynamicTagsEvent event)
    {
        event.fillTag(HAS_CEILING, DimensionType::hasCeiling, Registry.DIMENSION_TYPE_REGISTRY);
        event.fillTag(HAS_SKY, dimensionType -> !dimensionType.hasCeiling(), Registry.DIMENSION_TYPE_REGISTRY);
        event.fillTag(NATURAL, DimensionType::natural, Registry.DIMENSION_TYPE_REGISTRY);
        event.fillTag(UNNATURAL, dimensionType -> !dimensionType.natural(), Registry.DIMENSION_TYPE_REGISTRY);
        event.fillTag(ULTRAWARM, DimensionType::ultraWarm, Registry.DIMENSION_TYPE_REGISTRY);
        event.fillTag(BED_WORKS, DimensionType::bedWorks, Registry.DIMENSION_TYPE_REGISTRY);
        event.fillTag(RESPAWN_ANCHOR_WORKS, DimensionType::respawnAnchorWorks, Registry.DIMENSION_TYPE_REGISTRY);
        event.fillTag(PIGLIN_SAFE, DimensionType::piglinSafe, Registry.DIMENSION_TYPE_REGISTRY);
        event.fillTag(HAS_SKYLIGHT, DimensionType::hasSkyLight, Registry.DIMENSION_TYPE_REGISTRY);
        event.fillTag(HAS_RAIDS, DimensionType::hasRaids, Registry.DIMENSION_TYPE_REGISTRY);
        event.fillTag(OVERWORLD_LIKE,
                      dimensionType -> !dimensionType.hasCeiling()
                                    && dimensionType.natural()
                                    && dimensionType.hasSkyLight()
                                    && !dimensionType.ultraWarm(),
                      Registry.DIMENSION_TYPE_REGISTRY);
    }
}
