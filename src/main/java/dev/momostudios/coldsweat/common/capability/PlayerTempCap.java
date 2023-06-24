package dev.momostudios.coldsweat.common.capability;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.api.util.Temperature.Type;
import dev.momostudios.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModDamageSources;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

/**
 * Holds all the information regarding the entity's temperature. This should very rarely be used directly.
 */
public class PlayerTempCap implements ITemperatureCap
{
    static Type[] VALID_TEMPERATURE_TYPES = {Type.CORE, Type.BASE, Type.FREEZING_POINT, Type.BURNING_POINT, Type.WORLD};
    static Type[] VALID_MODIFIER_TYPES    = {Type.CORE, Type.BASE, Type.RATE, Type.FREEZING_POINT, Type.BURNING_POINT, Type.WORLD};

    private double[] syncedValues = new double[5];
    boolean neverSynced = true;

    // Map valid temperature types to a new EnumMap
    private final EnumMap<Type, Double> temperatures = Arrays.stream(VALID_MODIFIER_TYPES).collect(
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
        {
            throw new IllegalArgumentException("Invalid temperature type: " + t);
        });
    }

    public void setTemp(Type type, double value)
    {
        // Throw exception if this temperature type is not supported
        if (temperatures.replace(type, value) == null)
        {
            throw new IllegalArgumentException("Invalid temperature type: " + type);
        }
    }

    public List<TempModifier> getModifiers(Type type)
    {
        // Throw exception if this modifier type is not supported
        return modifiers.computeIfAbsent(type, t ->
        {
            throw new IllegalArgumentException("Invalid modifier type: " + t);
        });
    }

    public boolean hasModifier(Type type, Class<? extends TempModifier> mod)
    {
        return getModifiers(type).stream().anyMatch(mod::isInstance);
    }

    public boolean shouldShowBodyTemp()
    {
        return showBodyTemp;
    }

    public boolean shouldShowWorldTemp()
    {
        return showWorldTemp;
    }

    public void clearModifiers(Type type)
    {
        getModifiers(type).clear();
    }

    @Override
    public void tickDummy(LivingEntity entity)
    {
        if (!(entity instanceof Player player)) return;

        for (Type type : VALID_MODIFIER_TYPES)
        {   Temperature.apply(0, player, type, getModifiers(type));
        }

        if (player.tickCount % 20 == 0)
        {   calculateVisibility(player);
        }
    }

    @Override
    public void tick(LivingEntity entity)
    {
        if (!(entity instanceof ServerPlayer player)) return;

        // Tick expiration time for world modifiers
        double newWorldTemp = Temperature.apply(0, player, Type.WORLD, getModifiers(Type.WORLD));
        double newCoreTemp  = Temperature.apply(getTemp(Type.CORE), player, Type.CORE, getModifiers(Type.CORE));
        double newBaseTemp  = Temperature.apply(0, player, Type.BASE, getModifiers(Type.BASE));
        double newMaxOffset = Temperature.apply(0, player, Type.FREEZING_POINT, getModifiers(Type.FREEZING_POINT));
        double newMinOffset = Temperature.apply(0, player, Type.BURNING_POINT, getModifiers(Type.BURNING_POINT));

        double maxTemp = ConfigSettings.MAX_TEMP.get() + newMaxOffset;
        double minTemp = ConfigSettings.MIN_TEMP.get() + newMinOffset;

        // 1 if newWorldTemp is above max, -1 if below min, 0 if between the values (safe)
        int magnitude = CSMath.getSignForRange(newWorldTemp, minTemp, maxTemp);

        // Don't change player temperature if they're in creative/spectator mode
        if (magnitude != 0 && !(player.isCreative() || player.isSpectator()))
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
                    * ((100 - player.getPersistentData().getInt(magnitude > 0 ? "HeatResistance" : "ColdResistance")) / 100d);
            newCoreTemp += Temperature.apply(changeBy, player, Type.RATE, getModifiers(Type.RATE));
        }
        // If the player's temperature and world temperature are not both hot or both cold
        int tempSign = CSMath.getSign(newCoreTemp);
        if (tempSign != 0 && magnitude != tempSign)
        {
            double factor = (tempSign == 1 ? newWorldTemp - maxTemp : newWorldTemp - minTemp) / 3;
            double changeBy = CSMath.maxAbs(factor * ConfigSettings.TEMP_RATE.get(), ConfigSettings.TEMP_RATE.get() / 10d * -tempSign);
            newCoreTemp += CSMath.minAbs(changeBy, -getTemp(Type.CORE));
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
        ||  Math.abs(syncedValues[4] - newMinOffset) >= 0.02) && showWorldTemp))
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
                {   this.dealTempDamage(player, ModDamageSources.HOT, 2f);
                }
                else if (bodyTemp <= -100 && !(hasIceResist && ConfigSettings.ICE_RESISTANCE_ENABLED.get()))
                {   this.dealTempDamage(player, ModDamageSources.COLD, 2f);
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
                ModAdvancementTriggers.TEMPERATURE_CHANGED.trigger(player, type, getTemp(type));

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
        {
            if (type == Type.BODY || type == Type.RATE) continue;
            this.setTemp(type, cap.getTemp(type));
        }

        // Copy the modifiers
        for (Type type : VALID_MODIFIER_TYPES)
        {
            this.getModifiers(type).clear();
            this.getModifiers(type).addAll(cap.getModifiers(type));
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        // Save the player's temperatures
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
        {
            nbt.putDouble(NBTHelper.getTemperatureTag(type), this.getTemp(type));
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
    {
        // Load the player's temperatures
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
                TempModifier modifier = NBTHelper.tagToModifier((CompoundTag) modNBT);

                // Add the modifier to the player's temperature
                if (modifier != null)
                    getModifiers(type).add(modifier);
                else
                    ColdSweat.LOGGER.error("Failed to load modifier \"{}\" of type {}", ((CompoundTag) modNBT).getString("id"), type);
            });
        }
    }
}
