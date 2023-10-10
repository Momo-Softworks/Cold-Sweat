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

public class PlayerTempProperty implements IExtendedEntityProperties, IEntityTempProperty
{
    public static final String PROPERTY_NAME = "cold_sweat:temperature";
    protected Player player;
    protected World world;

    static Temperature.Type[] VALID_MODIFIER_TYPES = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.RATE, Temperature.Type.BURNING_POINT, Temperature.Type.FREEZING_POINT, Temperature.Type.WORLD};
    static Temperature.Type[] VALID_TEMPERATURE_TYPES = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.BURNING_POINT, Temperature.Type.FREEZING_POINT, Temperature.Type.WORLD};

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

        double maxTemp = ConfigSettings.MAX_TEMP.get() + newMaxOffset;
        double minTemp = ConfigSettings.MIN_TEMP.get() + newMinOffset;

        // 1 if newWorldTemp is above max, -1 if below min, 0 if between the values (safe)
        int magnitude = CSMath.getSignForRange(newWorldTemp, minTemp, maxTemp);

        // Don't change player temperature if they're in creative/spectator mode
        if (magnitude != 0 && !player.capabilities.isCreativeMode)
        {
            // How much hotter/colder the player's temp is compared to max/min
            double difference = Math.abs(newWorldTemp - CSMath.clamp(newWorldTemp, minTemp, maxTemp));
            double changeBy = (Math.max(
                    // Ensure a minimum speed for temperature change
                    (difference / 7d) * ConfigSettings.TEMP_RATE.get().floatValue(),
                    Math.abs(ConfigSettings.TEMP_RATE.get().floatValue() / 50d)
                    // If it's hot or cold
            ) * magnitude)
                    // Apply resistance from NBT
                    * ((100 - player.getEntityData().getInteger(magnitude > 0 ? "HeatResistance" : "ColdResistance")) / 100d);
            newCoreTemp += Temperature.apply(changeBy, player, Temperature.Type.RATE, getModifiers(Temperature.Type.RATE));
        }
        // If the player's temperature and world temperature are not both hot or both cold, return to neutral
        int tempSign = CSMath.getSign(newCoreTemp);
        if (tempSign != 0 && magnitude != tempSign && getModifiers(Temperature.Type.CORE).isEmpty())
        {
            double factor = (tempSign == 1 ? newWorldTemp - maxTemp : newWorldTemp - minTemp) / 3;
            double changeBy = CSMath.maxAbs(factor * ConfigSettings.TEMP_RATE.get(), ConfigSettings.TEMP_RATE.get() / 10d * -tempSign);
            newCoreTemp += CSMath.minAbs(changeBy, -getTemp(Temperature.Type.CORE));
        }

        // Update whether certain UI elements are being displayed (temp isn't synced if the UI element isn't showing)
        if (player.ticksExisted % 20 == 0)
        {   calculateVisibility(player);
        }

        // Write the new temperature values
        this.setTemperatures(player, new double[]{newWorldTemp, newMaxOffset, newMinOffset, CSMath.clamp(newCoreTemp, -150, 150), newBaseTemp});

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

        boolean hasGrace      = false;//player.getActivePotionEffect(ModEffects.GRACE) != null;
        boolean hasFireResist = false;//player.getActivePotionEffect(Effects.FIRE_RESISTANCE) != null;
        boolean hasIceResist  = false;//player.getActivePotionEffect(ModEffects.ICE_RESISTANCE) != null;

        //Deal damage to the player if temperature is critical
        if (!player.capabilities.isCreativeMode)
        {
            if (player.ticksExisted % 40 == 0 && !hasGrace)
            {
                if (bodyTemp >= 100 && !(hasFireResist && ConfigSettings.FIRE_RESISTANCE_ENABLED.get()))
                {   this.dealTempDamage(player, ModDamageSources.HOT, 2f);
                }
                else if (bodyTemp <= -100 && !(hasIceResist && ConfigSettings.ICE_RESISTANCE_ENABLED.get()))
                {   this.dealTempDamage(player, ModDamageSources.COLD, 2f);
                }
            }
        }
        else setTemp(Temperature.Type.CORE, 0);
    }

    private void setTemperatures(EntityPlayerMP player, double[] temps)
    {
        for (Temperature.Type type : VALID_TEMPERATURE_TYPES)
        {
            double oldTemp = getTemp(type);
            double newTemp = temps[type.ordinal()];
            //if (oldTemp != newTemp)
            //    ModAdvancementTriggers.TEMPERATURE_CHANGED.trigger(player, this.getTemperatures());

            this.setTemp(type, newTemp);
        }
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
