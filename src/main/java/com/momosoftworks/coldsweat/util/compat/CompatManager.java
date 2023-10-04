package com.momosoftworks.coldsweat.util.compat;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModDamageSources;
import com.momosoftworks.coldsweat.util.world.BlockPos;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;


public class CompatManager
{
    private static final boolean BOP_LOADED = modLoaded("biomesoplenty");
    private static final boolean SEASONS_LOADED = modLoaded("sereneseasons");
    private static final boolean CURIOS_LOADED = modLoaded("curios");
    private static final boolean WEREWOLVES_LOADED = modLoaded("werewolves");
    private static final boolean SPIRIT_LOADED = modLoaded("spirit");
    private static final boolean ARMOR_UNDERWEAR_LOADED = modLoaded("armorunder");
    private static final boolean BYG_LOADED = modLoaded("byg");
    private static final boolean CREATE_LOADED = modLoaded("create", "0.5.1");
    private static final boolean ATMOSPHERIC_LOADED = modLoaded("atmospheric");
    private static final boolean ENVIRONMENTAL_LOADED = modLoaded("environmental");
    private static final boolean TERRALITH_LOADED = modLoaded("terralith");
    private static final boolean WEATHER_LOADED = modLoaded("weather2");
    private static final boolean WYTHERS_LOADED = modLoaded("wwoo");
    private static final boolean BETTER_WEATHER_LOADED = modLoaded("betterweather");
    private static final boolean CAVES_AND_CLIFFS_LOADED = modLoaded("cavesandcliffs");

    private static boolean modLoaded(String modID, String version)
    {
        ModContainer mod = Loader.instance().getIndexedModList().get(modID);
        if (mod == null) return false;

        ArtifactVersion modVer = mod.getProcessedVersion();
        if (modVer.getVersionString().equals(version))
        {   return true;
        }
        else
        {   ColdSweat.LOGGER.error("Cold Sweat requires {} {} or higher for compat to be enabled!", modID, version);
            return false;
        }
    }

    private static boolean modLoaded(String modID)
    {   return modLoaded(modID, "");
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
    public static boolean isBetterWeatherLoaded()
    {   return BETTER_WEATHER_LOADED;
    }
    public static boolean isCavesAndCliffsLoaded()
    {   return CAVES_AND_CLIFFS_LOADED;
    }

    public static boolean hasOzzyLiner(ItemStack stack)
    {
        return false;//ARMOR_UNDERWEAR_LOADED && Armory.getXLining(stack).has(Armory.XLining.TEMPERATURE_REGULATOR);
    }
    public static boolean hasOttoLiner(ItemStack stack)
    {
        return false;//ARMOR_UNDERWEAR_LOADED && Armory.getXLining(stack).has(Armory.XLining.ANTIFREEZE_SHIELD);
    }
    public static boolean hasOllieLiner(ItemStack stack)
    {
        return false;//ARMOR_UNDERWEAR_LOADED && Armory.getXLining(stack).has(Armory.XLining.ANTIBURN_SHIELD);
    }

    public static boolean isWerewolf(EntityPlayer player)
    {
        return false;//WEREWOLVES_LOADED && WerewolfPlayer.getOpt(player).filter(w -> w.getLevel() > 0).map(w -> w.getForm().isTransformed()).orElse(false);
    }

    public static boolean isWeather2RainingAt(World world, BlockPos pos)
    {
        /*if (!WEATHER_LOADED) return false;
        WeatherManagerServer weatherManager = ServerTickHandler.getWeatherManagerFor(world.dimension());
        if (weatherManager == null) return false;
        StormObject rainStorm = weatherManager.getClosestStormAny(new Vector3d(pos.getX(), pos.getY(), pos.getZ()), 250);
        if (rainStorm == null) return false;

        return WorldHelper.canSeeSky(world, pos, 60) && rainStorm.isPrecipitating() && rainStorm.levelTemperature > 0.0f
            && Math.sqrt(Math.pow(pos.getX() - rainStorm.pos.x, 2) + Math.pow(pos.getX() - rainStorm.pos.x, 2)) < rainStorm.getSize();*/
        return false;
    }

    public static boolean isGoat(Entity entity)
    {   return false;//isCavesAndCliffsLoaded() && entity instanceof GoatEntity;
    }

    @SubscribeEvent
    public void onLivingTempDamage(LivingEvent event)
    {
        if (!(event instanceof LivingHurtEvent || event instanceof LivingAttackEvent)) return;
        // Armor Underwear compat
        if (ARMOR_UNDERWEAR_LOADED && !event.entityLiving.worldObj.isRemote && event.entityLiving instanceof EntityPlayer)
        {
            // Get the damage source from the event (different methods for LivingDamage/LivingAttack)
            DamageSource source = event instanceof LivingHurtEvent
                                  ? ((LivingHurtEvent) event).source
                                  : ((LivingAttackEvent) event).source;
            if (source == null) return;

            boolean isDamageCold;
            if (((isDamageCold = source == ModDamageSources.COLD) || source == ModDamageSources.HOT))
            {
                int liners = 0;
                for (ItemStack stack : ((EntityPlayer) event.entityLiving).inventory.armorInventory)
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
                if (event instanceof LivingHurtEvent)
                {   LivingHurtEvent damageEvent = (LivingHurtEvent) event;
                    damageEvent.ammount = CSMath.blend(damageEvent.ammount, 0, liners, 0, 4);
                }
            }
        }
    }
}
