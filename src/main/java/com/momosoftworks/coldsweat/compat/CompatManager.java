package com.momosoftworks.coldsweat.compat;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import com.anthonyhilyard.iceberg.component.TitleBreakComponent;
import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.init.FetchSeasonsModsEvent;
import com.momosoftworks.coldsweat.api.event.core.registry.LoadRegistriesEvent;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.temperature.modifier.compat.SereneSeasonsTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.compat.create.ColdSweatPonderPlugin;
import com.momosoftworks.coldsweat.compat.curios.EquipableCurio;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.tag.ModInsulatorTags;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.content.equipment.armor.BacktankItem;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import cn.mlus.thirst.content.purity.ContainerWithPurity;
import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.content.registry.ThirstComponent;
import cn.mlus.thirst.foundation.common.event.RegisterThirstValueEvent;
import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import glitchcore.event.EventManager;
import net.createmod.ponder.foundation.PonderIndex;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import sereneseasons.api.season.SeasonChangedEvent;
import sereneseasons.season.SeasonHooks;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;

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
        ModFileInfo mod = FMLLoader.getLoadingModList().getModFileById(modID);
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
            return CURIOS_LOADED
                && Optional.ofNullable(player.getCapability(CuriosCapability.INVENTORY))
                           .map(cap -> cap.findFirstCurio(curio))
                           .map(Optional::isPresent)
                           .orElse(false);
        }

        public static boolean hasCurio(LivingEntity player, ItemStack curio)
        {   return CURIOS_LOADED && getCurios(player).contains(curio);
        }

        public static List<ItemStack> getCurios(LivingEntity entity)
        {
            if (!CURIOS_LOADED) return new ArrayList<>();
            return Optional.ofNullable(entity.getCapability(CuriosCapability.INVENTORY))
                           .map(curiosHandler -> curiosHandler.getEquippedCurios())
                           .map(stacks ->
                           {
                               List<ItemStack> list = new ArrayList<>();
                               for (int i = 0; i < stacks.getSlots(); i++)
                               {   list.add(stacks.getStackInSlot(i));
                               }
                               return list;
                           }).orElse(new ArrayList<>());
        }
    }

    public static abstract class Create
    {
        public static boolean isFluidPipe(BlockState state)
        {
            return CompatManager.isCreateLoaded()
                && (state.getBlock() instanceof FluidPipeBlock
                 || state.getBlock() instanceof GlassFluidPipeBlock
                 || state.getBlock() instanceof EncasedPipeBlock);
        }
    }

    public static abstract class Weather2
    {
        public static boolean isRainstormAt(Level level, BlockPos pos)
        {
            if (WEATHER_LOADED)
            {
                WeatherManagerServer weatherManager = ServerTickHandler.getWeatherManagerFor(level.dimension());
                if (weatherManager == null) return false;
                StormObject rainStorm = weatherManager.getClosestStormAny(new Vec3(pos.getX(), pos.getY(), pos.getZ()), 250);
                if (rainStorm == null) return false;

                if (rainStorm.isPrecipitating() && rainStorm.levelTemperature > 0.0f
                && Math.sqrt(Math.pow(pos.getX() - rainStorm.pos.x, 2) + Math.pow(pos.getX() - rainStorm.pos.x, 2)) < rainStorm.getSize())
                {   return true;
                }
            }
            return false;
        }

        public static Object getClosestStorm(Level level, BlockPos pos)
        {
            if (WEATHER_LOADED)
            {
                WeatherManagerServer weatherManager = ServerTickHandler.getWeatherManagerFor(level.dimension());
                if (weatherManager == null) return null;

                double distance = Double.POSITIVE_INFINITY;
                WeatherObject closestStorm = null;
                for (WeatherObject stormObject : weatherManager.getStormObjects())
                {
                    double newDistance = stormObject.pos.distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                    if (newDistance < distance)
                    {   distance = newDistance;
                        closestStorm = stormObject;
                    }
                }
                return closestStorm;
            }
            return null;
        }
    }

    public static abstract class SereneSeasons
    {
        public static boolean isColdEnoughToSnow(Level level, BlockPos pos)
        {
            return SERENE_SEASONS_LOADED && SeasonHooks.coldEnoughToSnowSeasonal(level, level.getBiome(pos), pos);
        }
    }

    public static abstract class Thirst
    {
        public static boolean hasPurity(ItemStack stack)
        {
            if (THIRST_LOADED)
            {   return stack.has(ThirstComponent.PURITY);
            }
            return false;
        }

        public static int getPurity(ItemStack stack)
        {
            if (THIRST_LOADED)
            {   return WaterPurity.getPurity(stack);
            }
            return 0;
        }

        public static ItemStack setPurity(ItemStack stack, int purity)
        {
            if (THIRST_LOADED)
            {   stack.set(ThirstComponent.PURITY, purity);
                return stack;
            }
            return stack;
        }

        public static ItemStack setPurityFromBlock(ItemStack item, BlockPos pos, Level level)
        {
            if (THIRST_LOADED)
            {   int purity = WaterPurity.getBlockPurity(level, pos);
                item.set(ThirstComponent.PURITY, purity);
                return item;
            }
            return item;
        }
    }

    public static abstract class LegendaryTooltips
    {
        public static int getTooltipStartIndex(List<Either<FormattedText, TooltipComponent>> tooltip)
        {
            if (isIcebergLoaded())
            {
                int index = CSMath.getIndexOf(tooltip, element -> element.right().map(component -> component instanceof TitleBreakComponent).orElse(false));
                if (index == -1) return 0;
                return index;
            }
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
        public static final ActiveSableCompanion COMPANION = (ActiveSableCompanion) SableCompanion.INSTANCE;

        public static boolean isInPlotGrid(Level level, BlockPos pos)
        {   return COMPANION.isInPlotGrid(level, pos);
        }

        public static AABB transformSublToWorld(Level level, AABB aabb)
        {
            if (!COMPANION.isInPlotGrid(level, aabb.getCenter())) return aabb;
            SubLevel subLevel = COMPANION.getContaining(level, aabb.getCenter());
            if (subLevel == null) return aabb;
            return transformAABB(aabb, subLevel.logicalPose()::transformPosition);
        }

        public static Collection<AABB> transformWorldToSubl(Level level, AABB aabb)
        {
            Iterable<SubLevel> subLevels = COMPANION.getAllIntersecting(level, new BoundingBox3d(aabb));
            if (!subLevels.iterator().hasNext()) return Set.of();
            Set<AABB> subAABBs = new HashSet<>();
            subLevels.forEach(subLevel ->
            {   subAABBs.add(transformAABB(aabb, subLevel.logicalPose()::transformPositionInverse));
            });
            return Collections.unmodifiableSet(subAABBs);
        }

        /**
         * Transforms all 8 corners of the AABB and returns their enclosing box.<br>
         * Transforming only the min/max corners gives the wrong bounds if the sublevel is rotated.
         */
        private static AABB transformAABB(AABB aabb, UnaryOperator<Vec3> transform)
        {
            double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < 8; i++)
            {
                Vec3 corner = transform.apply(new Vec3((i & 1) == 0 ? aabb.minX : aabb.maxX,
                                                       (i & 2) == 0 ? aabb.minY : aabb.maxY,
                                                       (i & 4) == 0 ? aabb.minZ : aabb.maxZ));
                minX = Math.min(minX, corner.x); minY = Math.min(minY, corner.y); minZ = Math.min(minZ, corner.z);
                maxX = Math.max(maxX, corner.x); maxY = Math.max(maxY, corner.y); maxZ = Math.max(maxZ, corner.z);
            }
            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }

        public static BlockPos transformSublToWorld(Level level, BlockPos pos)
        {
            if (!COMPANION.isInPlotGrid(level, pos)) return pos;
            Vec3 transformed = COMPANION.projectOutOfSubLevel(level, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
            return BlockPos.containing(transformed);
        }

        public static BlockPos transformWorldToSubl(Level level, BlockPos pos)
        {
            if (COMPANION.isInPlotGrid(level, pos)) return pos;
            Iterator<SubLevel> iter = COMPANION.getAllIntersecting(level, new BoundingBox3d(pos)).iterator();
            if (!iter.hasNext()) return pos;
            SubLevel subLevel = iter.next();
            Vec3 transformed = subLevel.logicalPose().transformPositionInverse(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
            return BlockPos.containing(transformed);
        }
    }

    public static abstract class ImmersiveEngineering
    {
        public static final int ENERGY_PER_FUEL = 256;
        private static final Map<HearthBlockEntity, ExternalHeaterHandler.IExternalHeatable> HEATER_CAPS = new HashMap<>();

        public static ExternalHeaterHandler.IExternalHeatable getHeaterCap(HearthBlockEntity hearthLike)
        {
            ExternalHeaterHandler.IExternalHeatable heaterCap = HEATER_CAPS.computeIfAbsent(hearthLike, hearth ->
                new ExternalHeaterHandler.IExternalHeatable()
                {
                    @Override
                    public int doHeatTick(int energyAvailable, boolean redstone)
                    {
                        if (hearth.getFuel(HearthBlockEntity.FuelType.HOT).get() < hearth.getMaxFuel())
                        {
                            if (energyAvailable >= ENERGY_PER_FUEL)
                            {
                                hearth.addHotFuel(1, true);
                                return ENERGY_PER_FUEL;
                            }
                        }
                        return 0;
                    }
                });
            if (hearthLike.getLevel() instanceof ServerLevel serverLevel)
            {   serverLevel.registerCapabilityListener(hearthLike.getBlockPos(), () -> HEATER_CAPS.remove(hearthLike) != null);
            }
            return heaterCap;
        }
    }

    /* Compat Events */

    public static void registerEventHandlers()
    {
        if (CURIOS_LOADED)
        {
            NeoForge.EVENT_BUS.register(new Object()
            {
                @SubscribeEvent
                public void onCurioChange(CurioChangeEvent event)
                {
                    EntityTempManager.updateInsulationAttributeModifiers(event.getEntity(), event.getFrom(), event.getTo(), Insulation.Slot.CURIO);
                }
            });

            NeoForge.EVENT_BUS.register(new Object()
            {
                @SubscribeEvent
                public void registerEquipableCurios(LoadRegistriesEvent.Pre event)
                {
                    BuiltInRegistries.ITEM.getTag(ModItemTags.EQUIPABLE_CURIOS).ifPresent(tag ->
                    {
                        for (Holder<Item> item : tag)
                        {
                            if (CuriosApi.getCurio(item.value().getDefaultInstance()).isPresent()) continue;
                            CuriosApi.registerCurio(item.value(), new EquipableCurio());
                        }
                    });
                }
            });
        }

        if (THIRST_LOADED)
        {
            NeoForge.EVENT_BUS.register(new Object()
            {
                @SubscribeEvent
                public void registerThirstItems(RegisterThirstValueEvent event)
                {
                    event.addDrink(ModItems.FILLED_WATERSKIN.value(), 6, 3);
                    event.addContainer(new ContainerWithPurity(ModItems.WATERSKIN.value(),
                                                               ModItems.FILLED_WATERSKIN.value()));
                }
            });
        }

        if (SERENE_SEASONS_LOADED)
        {
            // Register event to GlitchCore's stupid redundant proprietary event bus
            new Object()
            {
                public void registerListener()
                {
                    EventManager.<SeasonChangedEvent.Standard>addListener(event ->
                    {
                        for (Player player : event.getLevel().players())
                        {
                            Temperature.getModifier(player, Temperature.Trait.WORLD, SereneSeasonsTempModifier.class)
                                       .ifPresent(mod -> mod.update(mod.getLastInput(Temperature.Trait.WORLD), player, Temperature.Trait.WORLD));
                        }
                    });
                }
            }.registerListener();
        }
    }

    public static boolean USING_BACKTANK = false;

    @SubscribeEvent
    public static void drainCreateBacktank(PlayerTickEvent.Post event)
    {
        Player player = event.getEntity();
        if (!CompatManager.isCreateLoaded()) return;

        ItemStack backTank = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(backTank.getItem() instanceof BacktankItem)) return;

        double worldTemp = Temperature.get(player, Temperature.Trait.WORLD);
        double minTemp = Temperature.get(player, Temperature.Trait.FREEZING_POINT);
        double maxTemp = Temperature.get(player, Temperature.Trait.BURNING_POINT);

        if (CSMath.betweenExclusive(worldTemp, minTemp, maxTemp)) return;
        if (worldTemp < minTemp && !ConfigSettings.COLD_DRAINS_BACKTANK.get()) return;
        if (worldTemp > maxTemp && !ConfigSettings.HEAT_DRAINS_BACKTANK.get()) return;

        if (USING_BACKTANK && player.level().isClientSide)
        {
            player.getPersistentData().putInt("VisualBacktankAir", BacktankUtil.getAllWithAir(player).stream()
                                                                               .map(BacktankUtil::getAir)
                                                                               .reduce(0, Integer::sum) - 1);
        }

        if (player.tickCount % 20 != 0)
        {   return;
        }

        if (!player.isCreative() && !player.isInLava() && backTank.getItem() instanceof BacktankItem)
        {
            // Ensure player is wearing a full set of fire-resistant armor
            List<InsulatorData> drainingInsulators = ConfigHelper.getTaggedConfigsFor(backTank.getItem(), ModInsulatorTags.DRAINS_BACKTANK, ConfigSettings.INSULATING_ARMORS.get());
            if (drainingInsulators.stream().noneMatch(insulator -> insulator.test(player, backTank)))
            {   return;
            }

            if (player.level().isClientSide)
                USING_BACKTANK = true;

            if (backTank.getOrDefault(AllDataComponents.BACKTANK_AIR, 0) > 0)
            {   // Drain air
                BacktankUtil.consumeAir(player, backTank, 1);
                //Update backtank air status
                if (player.level().isClientSide)
                {
                    player.getPersistentData().putInt("VisualBacktankAir", BacktankUtil.getAllWithAir(player).stream()
                                                                                             .map(BacktankUtil::getAir)
                                                                                             .reduce(0, Integer::sum));
                }
            }
        }
        else if (player.level().isClientSide)
        {   USING_BACKTANK = false;
        }
    }

    @EventBusSubscriber(modid = ColdSweat.MOD_ID)
    public static class ModEvents
    {
        @SubscribeEvent
        public static void setupModEvents(FMLCommonSetupEvent event)
        {
            if (isCreateLoaded())
            {
                event.enqueueWork(() ->
                {
                    // Register top/bottom halves of hearth as being connected
                    BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) ->
                    {
                        if (state.getBlock() == ModBlocks.HEARTH_BOTTOM.value())
                        {   return BlockMovementChecks.CheckResult.of(direction == Direction.UP);
                        }
                        if (state.getBlock() == ModBlocks.HEARTH_TOP.value())
                        {   return BlockMovementChecks.CheckResult.of(direction == Direction.DOWN);
                        }
                        return BlockMovementChecks.CheckResult.PASS;
                    });
                });
            }
        }

        @SubscribeEvent
        public static void setupModClientEvents(FMLClientSetupEvent event)
        {
            event.enqueueWork(() ->
            {
                if (isCreateLoaded())
                {
                    new Object()
                    {
                        public void registerPonderPlugin()
                        {   PonderIndex.addPlugin(new ColdSweatPonderPlugin());
                        }
                    }.registerPonderPlugin();
                }
            });
        }
    }
}
