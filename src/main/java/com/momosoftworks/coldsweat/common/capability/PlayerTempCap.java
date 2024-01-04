package com.momosoftworks.coldsweat.common.capability;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Temperature.Type;
import com.momosoftworks.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.registries.ModDamageSources;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;
import static com.momosoftworks.coldsweat.common.capability.EntityTempManager.*;

import java.util.*;

/**
 * Holds all the information regarding the entity's temperature. This should very rarely be used directly.
 */
public class PlayerTempCap implements ITemperatureCap
{
    private double[] syncedValues = new double[5];
    boolean neverSynced = true;

    // Map valid temperature types to a new EnumMap
    private final EnumMap<Type, Double> temperatures = Arrays.stream(VALID_TEMPERATURE_TYPES).collect(
            () -> new EnumMap<>(Type.class),
            (map, type) -> map.put(type, 0.0),
            EnumMap::putAll);

    // Map valid modifier types to a new EnumMap
    private final EnumMap<Type, List<TempModifier>> modifiers = Arrays.stream(VALID_MODIFIER_TYPES).collect(
            () -> new EnumMap<>(Type.class),
            (map, type) -> map.put(type, new ArrayList<>()),
            EnumMap::putAll);

    public boolean showBodyTemp;
    public boolean showWorldTemp;

    public double getTemp(Type type)
    {
        // Special case for BODY
        if (type == Type.BODY) return getTemp(Type.CORE) + getTemp(Type.BASE);
        // Throw exception if this temperature type is not supported
        return temperatures.computeIfAbsent(type, t ->
        {   throw new IllegalArgumentException("Invalid temperature type: " + t);
        });
    }

    @Override
    public EnumMap<Type, Double> getTemperatures()
    {   return new EnumMap<>(temperatures);
    }

    public void setTemp(Type type, double value)
    {
        // Throw exception if this temperature type is not supported
        if (temperatures.replace(type, value) == null)
        {   throw new IllegalArgumentException("Invalid temperature type: " + type);
        }
    }

    public List<TempModifier> getModifiers(Type type)
    {
        // Throw exception if this modifier type is not supported
        return modifiers.computeIfAbsent(type, t ->
        {   throw new IllegalArgumentException("Invalid modifier type: " + t);
        });
    }

    public boolean hasModifier(Type type, Class<? extends TempModifier> mod)
    {   return getModifiers(type).stream().anyMatch(mod::isInstance);
    }

    public boolean shouldShowBodyTemp()
    {
        return showBodyTemp;
    }

    public boolean showAdvancedWorldTemp()
    {
        return showWorldTemp;
    }

    public void clearModifiers(Type type)
    {
        getModifiers(type).clear();
    }

    // See Temperature.class for more temperature-related methods

    /**
     * Used for clientside ticking of TempModifiers. The result is not used.
     */
    @Override
    public void tickDummy(LivingEntity entity)
    {
        if (!(entity instanceof Player player)) return;

        Temperature.apply(0, player, Type.WORLD, getModifiers(Type.WORLD));
        Temperature.apply(getTemp(Type.CORE), player, Type.CORE, getModifiers(Type.CORE));
        Temperature.apply(0, player, Type.BASE, getModifiers(Type.BASE));
        Temperature.apply(0, player, Type.FREEZING_POINT, getModifiers(Type.FREEZING_POINT));
        Temperature.apply(0, player, Type.BURNING_POINT, getModifiers(Type.BURNING_POINT));

        if (player.tickCount % 20 == 0)
        {   calculateVisibility(player);
        }
    }

    @Override
    public void tick(LivingEntity entity)
    {
        if (!(entity instanceof ServerPlayer player)) return;

        // Get the base temperature values as defined by the player's attributes
        Double[] attributeBases = EntityTempManager.getAttributes(player);

        // Tick TempModifiers and pre-attribute-bases
        double newWorldTemp = Temperature.apply(attributeBases[0], player, Type.WORLD, getModifiers(Type.WORLD));
        double newCoreTemp  = Temperature.apply(getTemp(Type.CORE) + attributeBases[1], player, Type.CORE, getModifiers(Type.CORE));
        double newBaseTemp  = Temperature.apply(attributeBases[2], player, Type.BASE, getModifiers(Type.BASE));
        double newMaxOffset = Temperature.apply(attributeBases[3], player, Type.FREEZING_POINT, getModifiers(Type.FREEZING_POINT));
        double newMinOffset = Temperature.apply(attributeBases[4], player, Type.BURNING_POINT, getModifiers(Type.BURNING_POINT));
        double coldDampening = attributeBases[5];
        double heatDampening = attributeBases[6];
        double coldResistance = attributeBases[7];
        double heatResistance = attributeBases[8];

        // Apply attribute modifiers after TempModifiers
        double[] modifiedAttributes = EntityTempManager.applyAttributes(player, new double[] {newWorldTemp, newCoreTemp, newBaseTemp, newMaxOffset, newMinOffset, coldDampening, heatDampening, coldResistance, heatResistance});
        newWorldTemp = modifiedAttributes[0];
        newCoreTemp  = modifiedAttributes[1];
        newBaseTemp  = modifiedAttributes[2];
        newMaxOffset = modifiedAttributes[3];
        newMinOffset = modifiedAttributes[4];

        double maxTemp = ConfigSettings.MAX_TEMP.get() + newMaxOffset;
        double minTemp = ConfigSettings.MIN_TEMP.get() + newMinOffset;

        // 1 if newWorldTemp is above max, -1 if below min, 0 if between the values (safe)
        int worldTempSign = CSMath.getSignForRange(newWorldTemp, minTemp, maxTemp);

        boolean isFullyColdDampened = worldTempSign < 0 && coldDampening >= 1;
        boolean isFullyHeatDampened = worldTempSign > 0 && heatDampening >= 1;

        // Don't change player temperature if they're in creative/spectator mode
        if (worldTempSign != 0 && !(player.isCreative() || player.isSpectator())
        && !(isFullyColdDampened || isFullyHeatDampened))
        {
            // How much hotter/colder the player's temp is compared to max/min
            double difference = Math.abs(newWorldTemp - CSMath.clamp(newWorldTemp, minTemp, maxTemp));

            // How much the player's temperature should change
            double changeBy = (Math.max(
                    // Change proportionally to the extremity of the world temperature
                    (difference / 7d) * ConfigSettings.TEMP_RATE.get().floatValue(),
                    // Ensure a minimum speed for temperature change
                    Math.abs(ConfigSettings.TEMP_RATE.get().floatValue() / 50d)
            // If it's hot or cold
            ) * worldTempSign);

            // Apply cold/heat dampening to slow/increase the rate
            if (changeBy < 0) changeBy = (coldDampening < 0 ? changeBy * -(1 + coldDampening) : CSMath.blend(changeBy, 0, coldDampening, 0, 1));
            else              changeBy = (heatDampening < 0 ? changeBy * -(1 + heatDampening) : CSMath.blend(changeBy, 0, heatDampening, 0, 1));
            newCoreTemp += Temperature.apply(changeBy, player, Type.RATE, getModifiers(Type.RATE));
        }

        // If the player's temperature and world temperature are not both hot or both cold, return to neutral
        int coreTempSign = CSMath.getSign(newCoreTemp);
        if (getModifiers(Type.CORE).isEmpty())
        {
            double factor = 0;
            if (isFullyColdDampened && coreTempSign < 0)
            {   factor = ConfigSettings.TEMP_RATE.get() / 10d;
            }
            else if (isFullyHeatDampened && coreTempSign > 0)
            {   factor = ConfigSettings.TEMP_RATE.get() / -10d;
            }
            else if (coreTempSign != 0 && coreTempSign != worldTempSign)
            {   factor = (coreTempSign == 1 ? newWorldTemp - maxTemp : newWorldTemp - minTemp) / 3;
            }
            if (factor != 0)
            {   double changeBy = CSMath.maxAbs(factor * ConfigSettings.TEMP_RATE.get(), ConfigSettings.TEMP_RATE.get() / 10d * -coreTempSign);
                newCoreTemp += CSMath.minAbs(changeBy, -getTemp(Type.CORE));
            }
        }

        // Update whether certain UI elements are being displayed (temp isn't synced if the UI element isn't showing)
        if (player.tickCount % 20 == 0)
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
        double bodyTemp = getTemp(Type.BODY);

        boolean hasGrace = player.hasEffect(ModEffects.GRACE);
        boolean hasFireResist = player.hasEffect(MobEffects.FIRE_RESISTANCE);
        boolean hasIceResist = player.hasEffect(ModEffects.ICE_RESISTANCE);

        //Deal damage to the player if temperature is critical
        if (!player.isCreative() && !player.isSpectator())
        {
            if (player.tickCount % 40 == 0 && !hasGrace)
            {
                if (bodyTemp >= 100 && !(hasFireResist && ConfigSettings.FIRE_RESISTANCE_ENABLED.get()))
                {   this.dealTempDamage(player, ConfigSettings.DAMAGE_SCALING.get() ? ModDamageSources.HOT.setScalesWithDifficulty()
                                                                                    : ModDamageSources.HOT, (float) CSMath.blend(ConfigSettings.TEMP_DAMAGE.get(), 0, heatResistance, 0, 1));
                }
                else if (bodyTemp <= -100 && !(hasIceResist && ConfigSettings.ICE_RESISTANCE_ENABLED.get()))
                {   this.dealTempDamage(player, ConfigSettings.DAMAGE_SCALING.get() ? ModDamageSources.COLD.setScalesWithDifficulty()
                                                                                    : ModDamageSources.COLD, (float) CSMath.blend(ConfigSettings.TEMP_DAMAGE.get(), 0, coldResistance, 0, 1));
                }
            }
        }
        else setTemp(Type.CORE, 0);
    }

    private void setTemperatures(ServerPlayer player, double[] temps)
    {
        for (Type type : VALID_TEMPERATURE_TYPES)
        {
            double oldTemp = getTemp(type);
            double newTemp = temps[type.ordinal()];
            if (oldTemp != newTemp)
                ModAdvancementTriggers.TEMPERATURE_CHANGED.trigger(player, this.getTemperatures());

            this.setTemp(type, newTemp);
        }
    }

    public void calculateVisibility(Player player)
    {
        showWorldTemp = !ConfigSettings.REQUIRE_THERMOMETER.get()
                || player.getInventory().items.stream().limit(9).anyMatch(stack -> stack.getItem() == ModItems.THERMOMETER)
                || player.getOffhandItem().getItem() == ModItems.THERMOMETER
                || CompatManager.isCuriosLoaded() && CuriosApi.getCuriosHelper().findFirstCurio(player, ModItems.THERMOMETER).isPresent();
        showBodyTemp = !player.isCreative() && !player.isSpectator();
    }

    @Override
    public void copy(ITemperatureCap cap)
    {
        // Copy temperature values
        for (Type type : VALID_TEMPERATURE_TYPES)
        {   this.setTemp(type, cap.getTemp(type));
        }

        // Copy the modifiers
        for (Type type : VALID_MODIFIER_TYPES)
        {   this.getModifiers(type).clear();
            this.getModifiers(type).addAll(cap.getModifiers(type));
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {   // Save the player's temperatures
        CompoundTag nbt = this.serializeTemps();
        // Save the player's modifiers
        nbt.merge(this.serializeModifiers());
        return nbt;
    }

    @Override
    public CompoundTag serializeTemps()
    {
        CompoundTag nbt = new CompoundTag();

        // Save the player's temperature data
        for (Type type : VALID_TEMPERATURE_TYPES)
        {   nbt.putDouble(NBTHelper.getTemperatureTag(type), this.getTemp(type));
        }
        return nbt;
    }

    @Override
    public CompoundTag serializeModifiers()
    {
        CompoundTag nbt = new CompoundTag();

        // Save the player's modifiers
        for (Type type : VALID_MODIFIER_TYPES)
        {
            ListTag modifiers = new ListTag();
            for (TempModifier modifier : this.getModifiers(type))
            {
                modifiers.add(NBTHelper.modifierToTag(modifier));
            }
            // Write the list of modifiers to the player's persistent data
            nbt.put(NBTHelper.getModifierTag(type), modifiers);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {   // Load the player's temperatures
        deserializeTemps(nbt);
        // Load the player's modifiers
        deserializeModifiers(nbt);
    }

    @Override
    public void deserializeTemps(CompoundTag nbt)
    {
        for (Type type : VALID_TEMPERATURE_TYPES)
        {
            setTemp(type, nbt.getDouble(NBTHelper.getTemperatureTag(type)));
        }
    }

    @Override
    public void deserializeModifiers(CompoundTag nbt)
    {
        for (Type type : VALID_MODIFIER_TYPES)
        {
            getModifiers(type).clear();

            // Get the list of modifiers from the player's persistent data
            ListTag modifiers = nbt.getList(NBTHelper.getModifierTag(type), 10);

            // For each modifier in the list
            modifiers.forEach(modNBT ->
            {
                NBTHelper.tagToModifier((CompoundTag) modNBT).ifPresent(modifier ->
                {   getModifiers(type).add(modifier);
                });
            });
        }
    }
}
