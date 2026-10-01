package com.momosoftworks.coldsweat.compat;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.init.FetchSeasonsModsEvent;
import com.momosoftworks.coldsweat.api.event.core.registry.LoadRegistriesEvent;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.tag.ModInsulatorTags;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.*;
import java.util.function.UnaryOperator;

@EventBusSubscriber
public class CompatManager
{
    private static final boolean BOP_LOADED = modLoaded("biomesoplenty");
    private static final boolean SERENE_SEASONS_LOADED = modLoaded("sereneseasons");
    private static final boolean CURIOS_LOADED = modLoaded("curios");
    private static final boolean SPIRIT_LOADED = modLoaded("spirit");
    private static final boolean BYG_LOADED = modLoaded("byg");
    private static final boolean BWG_LOADED = modLoaded("biomeswevegone");
    private static final boolean CREATE_LOADED = modLoaded("create", "6.0.0");
    private static final boolean ATMOSPHERIC_LOADED = modLoaded("atmospheric");
    private static final boolean ENVIRONMENTAL_LOADED = modLoaded("environmental");
    private static final boolean TERRALITH_LOADED = modLoaded("terralith");
    private static final boolean WEATHER_LOADED = modLoaded("weather2");
    private static final boolean WYTHERS_LOADED = modLoaded("wwoo");
    private static final boolean TOOLTIPS_LOADED = modLoaded("legendarytooltips");
    private static final boolean PRIMAL_WINTER_LOADED = modLoaded("primalwinter");
    private static final boolean THIRST_LOADED = modLoaded("thirst", "1.21.1-3.0.0");
    private static final boolean ICEBERG_LOADED = modLoaded("iceberg", "1.3.0");
    private static final boolean SPOILED_LOADED = modLoaded("spoiled", "6.2.0");
    private static final boolean SUPPLEMENTARIES_LOADED = modLoaded("supplementaries");
    private static final boolean VALKYRIEN_SKIES_LOADED = modLoaded("valkyrienskies");
    private static final boolean TOUGH_AS_NAILS_LOADED = modLoaded("toughasnails");
    private static final boolean TWILIGHT_FOREST_LOADED = modLoaded("twilightforest");
    private static final boolean AETHER_LOADED = modLoaded("aether");
    private static final boolean REGIONS_UNEXPLORED_LOADED = modLoaded("regions_unexplored");
    private static final boolean SABLE_LOADED = modLoaded("sable");
    private static final boolean CREATE_AERONAUTICS = modLoaded("create_aeronautics");
    private static final boolean IMMERSIVE_ENGINEERING_LOADED = modLoaded("immersiveengineering");

    private static final List<String> SEASONS_MODS = new ArrayList<>();

    public static boolean modLoaded(String modID, String minVersion, String maxVersion)
    {
        List<String> disabledMods = ConfigSettings.DISABLED_MOD_COMPAT.get();
        if (disabledMods.contains(modID))
        {   return false;
        }
        ModFileInfo mod = FMLLoader.getCurrent().getLoadingModList().getModFileById(modID);
        if (mod == null)
        {   return false;
        }
        ArtifactVersion version = mod.getMods().get(0).getVersion();

        if (!minVersion.isEmpty() && version.compareTo(new DefaultArtifactVersion(minVersion)) < 0)
        {
            ColdSweat.LOGGER.error("Cold Sweat requires {} {} or higher for compat to be enabled! (found {})", modID, minVersion, version);
            return false;
        }
        if (!maxVersion.isEmpty() && version.compareTo(new DefaultArtifactVersion(maxVersion)) > 0)
        {
            ColdSweat.LOGGER.error("Cold Sweat requires {} {} or lower for compat to be enabled! (found {})", modID, maxVersion, version);
            return false;
        }
        else return true;
    }

    public static boolean modLoaded(String modID, String minVersion)
    {   return modLoaded(modID, minVersion, "");
    }

    public static boolean modLoaded(String modID)
    {   return modLoaded(modID, "");
    }

    private static List<String> fetchSeasonsMods()
    {
        if (SEASONS_MODS.isEmpty())
        {
            FetchSeasonsModsEvent event = new FetchSeasonsModsEvent();
            if (SERENE_SEASONS_LOADED)
            {   event.addSeasonsMod("sereneseasons");
            }
            NeoForge.EVENT_BUS.post(event);
            SEASONS_MODS.addAll(event.getSeasonsMods());
        }
        return SEASONS_MODS;
    }

    public static List<String> getSeasonsMods()
    {   return fetchSeasonsMods();
    }

    public static boolean isBiomesOPlentyLoaded()
    {   return BOP_LOADED;
    }
    public static boolean isSereneSeasonsLoaded()
    {   return SERENE_SEASONS_LOADED;
    }
    public static boolean isCuriosLoaded()
    {   return CURIOS_LOADED;
    }
    public static boolean isSpiritLoaded()
    {   return SPIRIT_LOADED;
    }
    public static boolean isBiomesYoullGoLoaded()
    {   return BYG_LOADED;
    }
    public static boolean isBiomesWeveGoneLoaded()
    {   return BWG_LOADED;
    }
    public static boolean isCreateLoaded()
    {   return CREATE_LOADED;
    }
    public static boolean isAtmosphericLoaded()
    {   return ATMOSPHERIC_LOADED;
    }
    public static boolean isEnvironmentalLoaded()
    {   return ENVIRONMENTAL_LOADED;
    }
    public static boolean isTerralithLoaded()
    {   return TERRALITH_LOADED;
    }
    public static boolean isWeather2Loaded()
    {   return WEATHER_LOADED;
    }
    public static boolean isWythersLoaded()
    {   return WYTHERS_LOADED;
    }
    public static boolean isLegendaryTooltipsLoaded()
    {   return TOOLTIPS_LOADED;
    }
    public static boolean isPrimalWinterLoaded()
    {   return PRIMAL_WINTER_LOADED;
    }
    public static boolean isThirstLoaded()
    {   return THIRST_LOADED;
    }
    public static boolean isIcebergLoaded()
    {   return ICEBERG_LOADED;
    }
    public static boolean isSpoiledLoaded()
    {   return SPOILED_LOADED;
    }
    public static boolean isSupplementariesLoaded()
    {   return SUPPLEMENTARIES_LOADED;
    }
    public static boolean isValkyrienSkiesLoaded()
    {   return VALKYRIEN_SKIES_LOADED;
    }
    /**
     * @return True if any loaded mod implements "sublevels" (movable block structures, i.e. Valkyrien Skies ships)
     */
    public static boolean isSublevelCompatLoaded()
    {   return VALKYRIEN_SKIES_LOADED || SABLE_LOADED;
    }
    public static boolean isToughAsNailsLoaded()
    {   return TOUGH_AS_NAILS_LOADED;
    }
    public static boolean isTwilightForestLoaded()
    {   return TWILIGHT_FOREST_LOADED;
    }
    public static boolean isAetherLoaded()
    {   return AETHER_LOADED;
    }
    public static boolean isRegionsUnexploredLoaded()
    {   return REGIONS_UNEXPLORED_LOADED;
    }
    public static boolean isSableLoaded()
    {   return SABLE_LOADED;
    }
    public static boolean isCreateAeronauticsLoaded()
    {   return CREATE_AERONAUTICS;
    }
    public static boolean isImmersiveEngineeringLoaded()
    {   return IMMERSIVE_ENGINEERING_LOADED;
    }

    public static abstract class Curios
    {
        public static boolean hasCurio(LivingEntity player, Item curio)
        {
            // TODO(26.2): restore when Curios is updated to 26.2 (see 1.21-FG branch)
            return false;
        }

        public static boolean hasCurio(LivingEntity player, ItemStack curio)
        {   return CURIOS_LOADED && getCurios(player).contains(curio);
        }

        public static List<ItemStack> getCurios(LivingEntity entity)
        {
            // TODO(26.2): restore when Curios is updated to 26.2 (see 1.21-FG branch)
            return new ArrayList<>();
        }
    }

    public static abstract class Create
    {
        public static boolean isFluidPipe(BlockState state)
        {
            // TODO(26.2): restore when Create is updated to 26.2 (see 1.21-FG branch)
            return false;
        }
    }

    public static abstract class Weather2
    {
        public static boolean isRainstormAt(Level level, BlockPos pos)
        {
            // TODO(26.2): restore when Weather2 is updated to 26.2 (see 1.21-FG branch)
            return false;
        }

        public static Object getClosestStorm(Level level, BlockPos pos)
        {
            // TODO(26.2): restore when Weather2 is updated to 26.2 (see 1.21-FG branch)
            return null;
        }
    }

    public static abstract class SereneSeasons
    {
        public static boolean isColdEnoughToSnow(Level level, BlockPos pos)
        {
            // TODO(26.2): restore when Serene Seasons is updated to 26.2 (see 1.21-FG branch)
            return false;
        }
    }

    public static abstract class PrimalWinter
    {
        public static boolean isWinterAt(Level level, Holder<Biome> biome)
        {
            // TODO(26.2): restore when Primal Winter is updated to 26.2 (see 1.21-FG branch)
            //  return ForgePrimalWinter.CONFIG.isWinterBiome(biome.unwrapKey().get())
            //      && ForgePrimalWinter.CONFIG.isWinterDimension(level.dimension());
            return false;
        }
    }

    public static abstract class Thirst
    {
        public static boolean hasPurity(ItemStack stack)
        {
            // TODO(26.2): restore when Thirst is updated to 26.2 (see 1.21-FG branch)
            return false;
        }

        public static int getPurity(ItemStack stack)
        {
            // TODO(26.2): restore when Thirst is updated to 26.2 (see 1.21-FG branch)
            return 0;
        }

        public static ItemStack setPurity(ItemStack stack, int purity)
        {
            // TODO(26.2): restore when Thirst is updated to 26.2 (see 1.21-FG branch)
            return stack;
        }

        public static ItemStack setPurityFromBlock(ItemStack item, BlockPos pos, Level level)
        {
            // TODO(26.2): restore when Thirst is updated to 26.2 (see 1.21-FG branch)
            return item;
        }

        public static ItemStack removePurity(ItemStack stack)
        {
            // TODO(26.2): restore when Thirst is updated to 26.2 (see 1.21-FG branch)
            return stack;
        }
    }

    public static abstract class LegendaryTooltips
    {
        public static int getTooltipStartIndex(List<Either<FormattedText, TooltipComponent>> tooltip)
        {
            // TODO(26.2): restore when Iceberg is updated to 26.2 (see 1.21-FG branch)
            return 0;
        }
    }

    //TODO: Reimplement when Valkyrien is updated to this version
    public static abstract class Valkyrien
    {
        public static boolean isInShipyard(Level level, BlockPos pos)
        {
            return false;
        }

        /*public static Vec3 translateToShipCoords(Vec3 pos, Ship ship)
        {
            if (ship != null)
            {
                Vector3d posVec = VectorConversionsMCKt.toJOML(pos);
                ship.getWorldToShip().transformPosition(posVec);
                return VectorConversionsMCKt.toMinecraft(posVec);
            }
            return pos;
        }*/

        public static AABB transformShipToWorld(Level level, AABB aabb)
        {
            /*
            AABBd aabbd = VectorConversionsMCKt.toJOML(aabb);
            Ship ship = VSGameUtilsKt.getLoadedShipManagingPos(level, VectorConversionsMCKt.toJOML(aabb.getCenter()));
            if (ship == null) return aabb;
            aabbd = aabbd.transform(ship.getShipToWorld());
            return VectorConversionsMCKt.toMinecraft(aabbd);
             */
            return aabb;
        }
        public static Collection<AABB> transformWorldToShip(Level level, AABB aabb)
        {
            /*
            Iterable<Ship> ships = VSGameUtilsKt.getShipsIntersecting(level, aabb);
            if (!ships.iterator().hasNext()) return Set.of();
            Set<AABB> subAABBs = new HashSet<>();
            ships.forEach(ship ->
            {
                AABBd aabbd = VectorConversionsMCKt.toJOML(aabb);
                Matrix4dc worldToShip = ship.getWorldToShip();
                aabbd = aabbd.transform(worldToShip);
                subAABBs.add(VectorConversionsMCKt.toMinecraft(aabbd));
            });
            return Collections.unmodifiableSet(subAABBs);
            */
            return Set.of();
        }

        public static BlockPos transformShipToWorld(Level level, BlockPos pos)
        {
            /*
            Ship ship = VSGameUtilsKt.getShipManagingPos(level, pos);
            if (ship == null) return pos;
            Vector3d translated = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
            translated = ship.getShipToWorld().transformPosition(translated);
            return BlockPos.containing(VectorConversionsMCKt.toMinecraft(translated));
             */
            return pos;
        }
        public static BlockPos transformWorldToShip(Level level, BlockPos pos)
        {
            /*
            Ship ship = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
            if (ship == null) return pos;
            Vector3d translated = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
            translated = ship.getWorldToShip().transformPosition(translated);
            return BlockPos.containing(VectorConversionsMCKt.toMinecraft(translated));
             */
            return pos;
        }
    }

    public static abstract class Sable
    {
        // TODO(26.2): restore when Sable is updated to 26.2 (see 1.21-FG branch)
        public static boolean isInPlotGrid(Level level, BlockPos pos)
        {   return false;
        }

        public static AABB transformSublToWorld(Level level, AABB aabb)
        {   return aabb;
        }

        public static Collection<AABB> transformWorldToSubl(Level level, AABB aabb)
        {   return Set.of();
        }

        public static BlockPos transformSublToWorld(Level level, BlockPos pos)
        {   return pos;
        }

        public static BlockPos transformWorldToSubl(Level level, BlockPos pos)
        {   return pos;
        }
    }

    public static abstract class ImmersiveEngineering
    {
        // TODO(26.2): restore heater capability when Immersive Engineering is updated to 26.2 (see 1.21-FG branch)
        public static final int ENERGY_PER_FUEL = 256;
    }

    /* Compat Events */

    public static void registerEventHandlers()
    {
        // TODO(26.2): restore Curios, Thirst and Serene Seasons event handlers when ported (see 1.21-FG branch)
    }

    public static boolean USING_BACKTANK = false;

    // TODO(26.2): restore Create backtank draining, contraption attachment checks, and Ponder plugin
    //  when Create is updated to 26.2 (see 1.21-FG branch)
}
