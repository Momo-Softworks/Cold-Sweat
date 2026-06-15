package com.momosoftworks.coldsweat.core.properties;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModDamageSources;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import ibxm.Player;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import static com.momosoftworks.coldsweat.common.event.EntityTempManager.VALID_MODIFIER_TYPES;
import static com.momosoftworks.coldsweat.common.event.EntityTempManager.VALID_TEMPERATURE_TYPES;

public class PlayerTempProperty implements IExtendedEntityProperties, IEntityTempProperty
{
    public static final String PROPERTY_NAME = "cold_sweat:temperature";

    private double[] syncedValues = new double[5];
    boolean neverSynced = true;

    public boolean showBodyTemp;
    public boolean showWorldTemp;

    // Map valid temperature types to a new EnumMap
    private final EnumMap<Temperature.Type, Double> temperatures = Arrays.stream(VALID_TEMPERATURE_TYPES).collect(
            () -> new EnumMap<>(Temperature.Type.class),
            (map, type) -> map.put(type, 0.0),
            EnumMap::putAll);

    // Map valid modifier types to a new EnumMap
    private final EnumMap<Temperature.Type, List<TempModifier>> modifiers = Arrays.stream(VALID_MODIFIER_TYPES).collect(
            () -> new EnumMap<>(Temperature.Type.class),
            (map, type) -> map.put(type, new ArrayList<>()),
            EnumMap::putAll);

    public double getTemp(Temperature.Type type)
    {
        // Special case for BODY
        if (type == Temperature.Type.BODY) return getTemp(Temperature.Type.CORE) + getTemp(Temperature.Type.BASE);
        // Throw exception if this temperature type is not supported
        return temperatures.computeIfAbsent(type, t ->
        {   throw new IllegalArgumentException("Invalid temperature type: " + t);
        });
    }

    public EnumMap<Temperature.Type, Double> getTemperatures()
    {   return new EnumMap<>(temperatures);
    }

    public void setTemp(Temperature.Type type, double value)
    {
        // Throw exception if this temperature type is not supported
        if (temperatures.replace(type, value) == null)
        {   throw new IllegalArgumentException("Invalid temperature type: " + type);
        }
    }

    public List<TempModifier> getModifiers(Temperature.Type type)
    {
        // Throw exception if this modifier type is not supported
        return modifiers.computeIfAbsent(type, t ->
        {   throw new IllegalArgumentException("Invalid modifier type: " + t);
        });
    }

    public boolean hasModifier(Temperature.Type type, Class<? extends TempModifier> mod)
    {   return getModifiers(type).stream().anyMatch(mod::isInstance);
    }

    public boolean shouldShowBodyTemp()
    {   return showBodyTemp;
    }

    public boolean showAdvancedWorldTemp()
    {   return showWorldTemp;
    }

    public void clearModifiers(Temperature.Type type)
    {   getModifiers(type).clear();
    }

    public void copy(IEntityTempProperty prop)
    {
        // Copy temperature values
        for (Temperature.Type type : VALID_TEMPERATURE_TYPES)
        {
            if (type == Temperature.Type.BODY || type == Temperature.Type.RATE) continue;
            this.setTemp(type, prop.getTemp(type));
        }

        // Copy the modifiers
        for (Temperature.Type type : VALID_MODIFIER_TYPES)
        {
            this.getModifiers(type).clear();
            this.getModifiers(type).addAll(prop.getModifiers(type));
        }
    }

    public void tickDummy(EntityLivingBase entity)
    {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;

        Temperature.apply(0, player, Temperature.Type.WORLD, getModifiers(Temperature.Type.WORLD));
        Temperature.apply(getTemp(Temperature.Type.CORE), player, Temperature.Type.CORE, getModifiers(Temperature.Type.CORE));
        Temperature.apply(0, player, Temperature.Type.BASE, getModifiers(Temperature.Type.BASE));
        Temperature.apply(0, player, Temperature.Type.FREEZING_POINT, getModifiers(Temperature.Type.FREEZING_POINT));
        Temperature.apply(0, player, Temperature.Type.BURNING_POINT, getModifiers(Temperature.Type.BURNING_POINT));

        if (player.ticksExisted % 20 == 0)
        {   calculateVisibility(player);
        }
    }

    public void tick(EntityLivingBase entity)
    {
        if (!(entity instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) entity;

        // Tick expiration time for world modifiers
        double newWorldTemp = Temperature.apply(0, player, Temperature.Type.WORLD, getModifiers(Temperature.Type.WORLD));
        double newCoreTemp  = Temperature.apply(getTemp(Temperature.Type.CORE), player, Temperature.Type.CORE, getModifiers(Temperature.Type.CORE));
        double newBaseTemp  = Temperature.apply(0, player, Temperature.Type.BASE, getModifiers(Temperature.Type.BASE));
        double newMaxOffset = Temperature.apply(0, player, Temperature.Type.FREEZING_POINT, getModifiers(Temperature.Type.FREEZING_POINT));
        double newMinOffset = Temperature.apply(0, player, Temperature.Type.BURNING_POINT, getModifiers(Temperature.Type.BURNING_POINT));

        // Attribute traits (resistance/dampening). Computed from their modifier lists.
        // Resistance reduces temperature *damage* (see below); dampening reduces the *rate* of temp change.
        double coldResistance = Temperature.apply(0, player, Temperature.Type.COLD_RESISTANCE, getModifiers(Temperature.Type.COLD_RESISTANCE));
        double heatResistance = Temperature.apply(0, player, Temperature.Type.HEAT_RESISTANCE, getModifiers(Temperature.Type.HEAT_RESISTANCE));
        double coldDampening  = Temperature.apply(0, player, Temperature.Type.COLD_DAMPENING,  getModifiers(Temperature.Type.COLD_DAMPENING));
        double heatDampening  = Temperature.apply(0, player, Temperature.Type.HEAT_DAMPENING,  getModifiers(Temperature.Type.HEAT_DAMPENING));
        setTemp(Temperature.Type.COLD_RESISTANCE, coldResistance);
        setTemp(Temperature.Type.HEAT_RESISTANCE, heatResistance);
        setTemp(Temperature.Type.COLD_DAMPENING, coldDampening);
        setTemp(Temperature.Type.HEAT_DAMPENING, heatDampening);

        double maxTemp = ConfigSettings.MAX_TEMP.get() + newMaxOffset;
        double minTemp = ConfigSettings.MIN_TEMP.get() + newMinOffset;

        // 1 if newWorldTemp is above max, -1 if below min, 0 if between the values (safe)
        int magnitude = CSMath.getSignForRange(newWorldTemp, minTemp, maxTemp);

        // The rate of body-temperature change applied this tick (used to accelerate temperature damage)
        double rate = 0;

        // Don't change player temperature if they're in creative/spectator mode
        if (magnitude != 0 && !player.capabilities.isCreativeMode)
        {
            // How much hotter/colder the player's temp is compared to max/min
            double difference = Math.abs(newWorldTemp - CSMath.clamp(newWorldTemp, minTemp, maxTemp));
            double changeBy = Math.max(
                    // Change proportionally to the magnitude of the world temperature
                    (difference / 7d) * ConfigSettings.TEMP_RATE.get(),
                    // Ensure a minimum speed for temperature change
                    Math.abs(ConfigSettings.TEMP_RATE.get() / 50d)
                    // If it's hot or cold
            ) * magnitude;

            // Temp is decreasing; apply cold dampening
            if (changeBy < 0)
            {   changeBy = (coldDampening < 0
                            // Cold dampening is negative; increase the change by the dampening
                            ? changeBy * (1 + Math.abs(coldDampening))
                            // Cold dampening is positive; apply the change as a percentage of the dampening
                            : CSMath.blend(changeBy, 0, coldDampening, 0, 1));
            }
            // Temp is increasing; apply heat dampening
            else if (changeBy > 0)
            {   changeBy = (heatDampening < 0
                            // Heat dampening is negative; increase the change by the dampening
                            ? changeBy * (1 + Math.abs(heatDampening))
                            // Heat dampening is positive; apply the change as a percentage of the dampening
                            : CSMath.blend(changeBy, 0, heatDampening, 0, 1));
            }
            rate = Temperature.apply(changeBy, player, Temperature.Type.RATE, getModifiers(Temperature.Type.RATE));
            newCoreTemp += rate;
        }

        // If needed, equalize the player's core temperature back toward 0
        double equilibrium = getEquilibriumDelta(newCoreTemp, newWorldTemp, minTemp, maxTemp, coldDampening, heatDampening);
        int coreDeltaSign = CSMath.getSign(newCoreTemp - getTemp(Temperature.Type.CORE));
        int equilibriumSign = CSMath.getSign(equilibrium);
        // Only apply the equilibrium delta if it isn't working against any CORE modifiers
        if (coreDeltaSign == 0 || coreDeltaSign == equilibriumSign)
        {   newCoreTemp += equilibrium;
        }

        // Update whether certain UI elements are being displayed (temp isn't synced if the UI element isn't showing)
        if (player.ticksExisted % 20 == 0)
        {   calculateVisibility(player);
        }

        // Write the new temperature values
        this.setTemperatures(player, newWorldTemp, newMaxOffset, newMinOffset, CSMath.clamp(newCoreTemp, -150, 150), newBaseTemp);

        // Sync the temperature values to the client
        if ((neverSynced
                || ((int) syncedValues[0] != (int) newCoreTemp
                || ((int) syncedValues[1] != (int) newBaseTemp) && showBodyTemp)
                || (Math.abs(syncedValues[2] - newWorldTemp) >= 0.02
                ||  Math.abs(syncedValues[3] - newMaxOffset) >= 0.02
                ||  Math.abs(syncedValues[4] - newMinOffset) >= 0.02)))
        {
            Temperature.updateTemperature(player, this, false);
            syncedValues = new double[] { newCoreTemp, newBaseTemp, newWorldTemp, newMaxOffset, newMinOffset };
            neverSynced = false;
        }

        // Calculate body/base temperatures with modifiers
        double bodyTemp = getTemp(Temperature.Type.BODY);
        double damage = ConfigSettings.TEMP_DAMAGE.get();
        int hurtInterval = ConfigSettings.TEMPERATURE_HURT_INTERVAL.get();

        boolean hasGrace      = false;//player.getActivePotionEffect(ModEffects.GRACE) != null;
        boolean hasFireResist = false;//player.getActivePotionEffect(Effects.FIRE_RESISTANCE) != null;
        boolean hasIceResist  = false;//player.getActivePotionEffect(ModEffects.ICE_RESISTANCE) != null;

        //Deal damage to the player if temperature is critical
        if (!player.capabilities.isCreativeMode)
        {
            if (hurtInterval >= 1 && !hasGrace)
            {
                // Don't damage faster if body temp is equalizing
                double rateFactor = CSMath.getSign(bodyTemp) == CSMath.getSign(rate) ? Math.abs(rate) : 0;
                // Get damage interval based on rate of temp change
                int rateInterval = (int) CSMath.blend(1, 4, rateFactor, 0, 0.7);

                if (player.ticksExisted % (hurtInterval / rateInterval) == 0)
                {
                    if (bodyTemp >= 100 && !(hasFireResist && ConfigSettings.FIRE_RESISTANCE_ENABLED.get()))
                    {   this.dealTempDamage(player, ModDamageSources.HOT, (float) CSMath.blend(damage, 0, heatResistance, 0, 1));
                    }
                    else if (bodyTemp <= -100 && !(hasIceResist && ConfigSettings.ICE_RESISTANCE_ENABLED.get()))
                    {   this.dealTempDamage(player, ModDamageSources.COLD, (float) CSMath.blend(damage, 0, coldResistance, 0, 1));
                    }
                }
            }
        }
        else setTemp(Temperature.Type.CORE, 0);
    }

    /**
     * Computes how much the player's core temperature should equalize back toward 0 this tick.<br>
     * Mirrors 1.16's {@code AbstractTempCap.getEquilibriumDelta}: when the player is fully dampened against the
     * current world temperature, their body returns to neutral; otherwise it drifts back when core and world
     * temperatures disagree.
     */
    private double getEquilibriumDelta(double coreTemp, double worldTemp, double minTemp, double maxTemp, double coldDampening, double heatDampening)
    {
        int worldTempSign = CSMath.getSignForRange(worldTemp, minTemp, maxTemp);
        boolean isFullyColdDampened = worldTempSign < 0 && coldDampening >= 1;
        boolean isFullyHeatDampened = worldTempSign > 0 && heatDampening >= 1;

        // Get the sign of the player's core temperature (-1, 0, or 1)
        int coreTempSign = CSMath.getSign(coreTemp);
        // If needed, blend the player's temperature back to 0
        double amount = 0;
        // Player is fully cold dampened & body is cold
        if (isFullyColdDampened && coreTempSign < 0)
        {   amount = ConfigSettings.TEMP_RATE.get() / 10d;
        }
        // Player is fully heat dampened & body is hot
        else if (isFullyHeatDampened && coreTempSign > 0)
        {   amount = ConfigSettings.TEMP_RATE.get() / -10d;
        }
        // Else if the player's core temp is not the same as the world temp
        else if (coreTempSign != 0 && coreTempSign != worldTempSign)
        {   amount = (coreTempSign == 1 ? worldTemp - maxTemp : worldTemp - minTemp) / 3;
        }
        // Blend back to 0
        if (amount != 0)
        {
            double changeBy = CSMath.maxAbs(amount * ConfigSettings.TEMP_RATE.get(), ConfigSettings.TEMP_RATE.get() / 10d * -coreTempSign);
            return CSMath.minAbs(changeBy, -getTemp(Temperature.Type.CORE));
        }
        return 0;
    }

    private void setTemperatures(EntityPlayerMP player, double world, double freezing, double burning, double core, double base)
    {
        this.setTemp(Temperature.Type.WORLD, world);
        this.setTemp(Temperature.Type.FREEZING_POINT, freezing);
        this.setTemp(Temperature.Type.BURNING_POINT, burning);
        this.setTemp(Temperature.Type.CORE, core);
        this.setTemp(Temperature.Type.BASE, base);
    }

    public void calculateVisibility(EntityPlayer player)
    {
        showWorldTemp = !ConfigSettings.REQUIRE_THERMOMETER.get()
                || Arrays.stream(player.inventory.mainInventory).limit(9).anyMatch(stack -> stack != null && stack.getItem() == ModItems.THERMOMETER);
                /*|| CompatManager.isCuriosLoaded() && CuriosApi.getCuriosHelper().findFirstCurio(player, ModItems.THERMOMETER).isPresent();*/
        showBodyTemp = !player.capabilities.isCreativeMode;
    }

    @Override
    public void saveNBTData(NBTTagCompound nbt)
    {
        NBTTagCompound propNBT = new NBTTagCompound();

        // Save the player's temps
        NBTTagCompound tempNBT = this.serializeTemps();
        for (Object o : tempNBT.func_150296_c())
        {
            String key = (String) o;
            propNBT.setTag(key, tempNBT.getTag(key));
        }

        // Save the player's modifiers
        NBTTagCompound modsNBT = this.serializeModifiers();
        for (Object o : modsNBT.func_150296_c())
        {
            String key = (String) o;
            propNBT.setTag(key, modsNBT.getTag(key));
        }

        nbt.setTag(PROPERTY_NAME, propNBT);
    }

    public NBTTagCompound serializeTemps()
    {
        NBTTagCompound nbt = new NBTTagCompound();

        // Save the player's temperature data
        for (Temperature.Type type : VALID_TEMPERATURE_TYPES)
        {
            nbt.setDouble(NBTHelper.getTemperatureNBTKey(type), this.getTemp(type));
        }
        return nbt;
    }

    public NBTTagCompound serializeModifiers()
    {
        NBTTagCompound nbt = new NBTTagCompound();

        // Save the player's modifiers
        for (Temperature.Type type : VALID_MODIFIER_TYPES)
        {
            NBTTagList modifiers = new NBTTagList();
            for (TempModifier modifier : this.getModifiers(type))
            {
                modifiers.appendTag(NBTHelper.modifierToNBT(modifier));
            }

            // Write the list of modifiers to the player's persistent data
            nbt.setTag(NBTHelper.getModifierNBTKey(type), modifiers);
        }
        return nbt;
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        deserializeTemps(compound);
        deserializeModifiers(compound);
    }

    public void deserializeTemps(NBTTagCompound nbt)
    {
        for (Temperature.Type type : VALID_TEMPERATURE_TYPES)
        {   setTemp(type, nbt.getDouble(NBTHelper.getTemperatureNBTKey(type)));
        }
    }

    public void deserializeModifiers(NBTTagCompound nbt)
    {
        for (Temperature.Type type : VALID_MODIFIER_TYPES)
        {
            getModifiers(type).clear();

            // Get the list of modifiers from the player's persistent data
            NBTTagList modifiers = nbt.getTagList(NBTHelper.getModifierNBTKey(type), 10);

            // For each modifier in the list
            for (int i = 0; i < modifiers.tagCount(); i++)
            {
                NBTTagCompound modNBT = modifiers.getCompoundTagAt(i);
                NBTHelper.NBTToModifier(modNBT).ifPresent(modifier ->
                {   getModifiers(type).add(modifier);
                });
            }
        }
    }

    @Override
    public void init(Entity entity, World world)
    {}
}
