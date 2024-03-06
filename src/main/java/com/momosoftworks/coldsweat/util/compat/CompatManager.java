package com.momosoftworks.coldsweat.util.compat;

import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.temperature.modifier.compat.CuriosTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.util.ItemData;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModDamageSources;
import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.content.purity.ContainerWithPurity;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber
public class CompatManager
{
    private static final boolean BOP_LOADED = modLoaded("biomesoplenty");
    private static final boolean SEASONS_LOADED = modLoaded("sereneseasons");
    private static final boolean CURIOS_LOADED = modLoaded("curios");
    private static final boolean WEREWOLVES_LOADED = modLoaded("werewolves");
    private static final boolean SPIRIT_LOADED = modLoaded("spirit");
    private static final boolean ARMOR_UNDERWEAR_LOADED = modLoaded("armorunder");
    private static final boolean BYG_LOADED = modLoaded("byg");
    private static final boolean CREATE_LOADED = modLoaded("create", 0, 5, 1);
    private static final boolean ATMOSPHERIC_LOADED = modLoaded("atmospheric");
    private static final boolean ENVIRONMENTAL_LOADED = modLoaded("environmental");
    private static final boolean TERRALITH_LOADED = modLoaded("terralith");
    private static final boolean WEATHER_LOADED = modLoaded("weather2");
    private static final boolean WYTHERS_LOADED = modLoaded("wwoo");
    private static final boolean TOOLTIPS_LOADED = modLoaded("legendarytooltips");
    private static final boolean PRIMAL_WINTER_LOADED = modLoaded("primalwinter");
    private static final boolean THIRST_LOADED = modLoaded("thirst");

    public static boolean modLoaded(String modID, int minMajorVer, int minMinorVer, int minPatchVer)
    {
        if (minMajorVer > 0 || minMinorVer > 0 || minPatchVer > 0)
        {
            ModContainer mod = ModList.get().getModContainerById(modID).orElse(null);
            if (mod == null) return false;

            ArtifactVersion version = mod.getModInfo().getVersion();
            if (version.getMajorVersion() >= minMajorVer
            &&  version.getMinorVersion() >= minMinorVer
            &&  version.getIncrementalVersion() >= minPatchVer)
            {   return true;
            }
            else
            {   ColdSweat.LOGGER.error("Cold Sweat requires {} {} or higher for compat to be enabled!", modID.substring(0, 1).toUpperCase() + modID.substring(1),
                                                                                                                minMajorVer + "." + minMinorVer + "." + minPatchVer);
                return false;
            }
        }
        else return ModList.get().isLoaded(modID);
    }

    public static boolean modLoaded(String modID)
    {   return modLoaded(modID, 0, 0, 0);
    }

    public static boolean isBiomesOPlentyLoaded()
    {   return BOP_LOADED;
    }
    public static boolean isSereneSeasonsLoaded()
    {   return SEASONS_LOADED;
    }
    public static boolean isCuriosLoaded()
    {   return CURIOS_LOADED;
    }
    public static boolean isWerewolvesLoaded()
    {   return WEREWOLVES_LOADED;
    }
    public static boolean isSpiritLoaded()
    {   return SPIRIT_LOADED;
    }
    public static boolean isArmorUnderwearLoaded()
    {   return ARMOR_UNDERWEAR_LOADED;
    }
    public static boolean isBiomesYoullGoLoaded()
    {   return BYG_LOADED;
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

    public static boolean hasOzzyLiner(ItemStack stack)
    {
        return false;
        //return ARMOR_UNDERWEAR_LOADED && Armory.getXLining(stack).has(Armory.XLining.TEMPERATURE_REGULATOR);
    }
    public static boolean hasOttoLiner(ItemStack stack)
    {
        return false;
        //return ARMOR_UNDERWEAR_LOADED && Armory.getXLining(stack).has(Armory.XLining.ANTIFREEZE_SHIELD);
    }
    public static boolean hasOllieLiner(ItemStack stack)
    {
        return false;
        //return ARMOR_UNDERWEAR_LOADED && Armory.getXLining(stack).has(Armory.XLining.ANTIBURN_SHIELD);
    }

    public static boolean isWerewolf(Player player)
    {
        return WEREWOLVES_LOADED && WerewolfPlayer.getOpt(player).filter(w -> w.getLevel() > 0).map(w -> w.getForm().isTransformed()).orElse(false);
    }

    public static boolean isWeather2RainingAt(Level level, BlockPos pos)
    {
        return false;
        /*if (!WEATHER_LOADED) return false;
        WeatherManagerServer weatherManager = ServerTickHandler.getWeatherManagerFor(level.dimension());
        if (weatherManager == null) return false;
        StormObject rainStorm = weatherManager.getClosestStormAny(new Vec3(pos.getX(), pos.getY(), pos.getZ()), 250);
        if (rainStorm == null) return false;

        return WorldHelper.canSeeSky(level, pos, 60) && rainStorm.isPrecipitating() && rainStorm.levelTemperature > 0.0f
            && Math.sqrt(Math.pow(pos.getX() - rainStorm.pos.x, 2) + Math.pow(pos.getX() - rainStorm.pos.x, 2)) < rainStorm.getSize();*/
    }

    @SubscribeEvent
    public static void onLivingTempDamage(LivingEvent event)
    {
        if (!(event instanceof LivingDamageEvent || event instanceof LivingAttackEvent)) return;
        // Armor Underwear compat
        if (ARMOR_UNDERWEAR_LOADED && !event.getEntity().level.isClientSide)
        {
            // Get the damage source from the event (different methods for LivingDamage/LivingAttack)
            DamageSource source = event instanceof LivingDamageEvent
                                  ? ((LivingDamageEvent) event).getSource()
                                  : ((LivingAttackEvent) event).getSource();
            if (source == null) return;

            boolean isDamageCold;
            if (((isDamageCold = source == ModDamageSources.COLD) || source == ModDamageSources.HOT))
            {
                int liners = 0;
                for (ItemStack stack : event.getEntity().getArmorSlots())
                {
                    if (isDamageCold ? hasOttoLiner(stack) : hasOllieLiner(stack))
                        liners++;
                }
                // Cancel the event if full liners
                if (liners >= 4)
                {   event.setCanceled(true);
                    return;
                }
                // Dampen the damage as the number of liners increases
                if (event instanceof LivingDamageEvent damageEvent)
                    damageEvent.setAmount(CSMath.blend(damageEvent.getAmount(), 0, liners, 0, 4));
            }
        }
    }

    public static int getWaterPurity(ItemStack stack)
    {
        if (THIRST_LOADED)
        {   return WaterPurity.getPurity(stack);
        }
        return 0;
    }

    public static ItemStack setWaterPurity(ItemStack stack, int purity)
    {
        if (THIRST_LOADED)
        {   return WaterPurity.addPurity(stack, purity);
        }
        return stack;
    }

    public static ItemStack setWaterPurity(ItemStack item, BlockPos pos, Level level)
    {
        if (THIRST_LOADED)
        {   return WaterPurity.addPurity(item, pos, level);
        }
        return item;
    }

    @SubscribeEvent
    public static void tickCurios(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && CURIOS_LOADED && event.player.tickCount % 20 == 0)
        {
            CuriosApi.getCuriosHelper().getCuriosHandler(event.player).ifPresent(curiosInv ->
            {
                curiosInv.getCurios().forEach((identifier, curioStackHandler) ->
                {
                    for (int i = 0; i < curioStackHandler.getStacks().getSlots(); i++)
                    {
                        ItemStack stack = curioStackHandler.getStacks().getStackInSlot(i);
                        if (!stack.isEmpty() && ConfigSettings.INSULATING_CURIOS.get().containsKey(ItemData.of(stack)))
                        {
                            Insulation insulation = ConfigSettings.INSULATING_CURIOS.get().get(ItemData.of(stack));
                            double cold = insulation.getCold();
                            double hot = insulation.getHot();
                            Temperature.addOrReplaceModifier(event.player, new CuriosTempModifier(cold, hot).expires(20), Temperature.Type.RATE);
                        }
                    }
                });
            });
        }
    }

    @Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents
    {
        @SubscribeEvent
        public static void addThirstDrinks(FMLCommonSetupEvent event)
        {
            if (isThirstLoaded())
            {   ThirstHelper.addDrink(ModItems.FILLED_WATERSKIN, 6, 12);
                WaterPurity.addContainer(new ContainerWithPurity(ModItems.WATERSKIN.getDefaultInstance(),
                                                                 ModItems.FILLED_WATERSKIN.getDefaultInstance()));
            }
        }
    }
}
