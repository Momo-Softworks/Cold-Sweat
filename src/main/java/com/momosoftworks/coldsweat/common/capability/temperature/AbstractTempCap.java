package com.momosoftworks.coldsweat.common.capability.temperature;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Temperature.Ability;
import com.momosoftworks.coldsweat.api.util.Temperature.Type;
import com.momosoftworks.coldsweat.common.event.capability.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModDamageSources;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Supplier;

import static com.momosoftworks.coldsweat.common.event.capability.EntityTempManager.VALID_MODIFIER_TYPES;
import static com.momosoftworks.coldsweat.common.event.capability.EntityTempManager.VALID_TEMPERATURE_TYPES;

/**
 * Holds all the information regarding the entity's temperature. This should very rarely be used directly.
 */
public class AbstractTempCap implements ITemperatureCap
{
    boolean changed = true;
    int syncTimer = 0;
    Temperature.Units preferredUnits = Temperature.Units.F;

    private final Set<Attribute> persistentAttributes = new HashSet<>();

    private final EnumMap<Ability, Double> abilities = Arrays.stream(Ability.values()).collect(
            () -> new EnumMap<>(Ability.class),
            (map, type) -> map.put(type, 0.0),
            EnumMap::putAll);

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

    @Override
    public double getTemp(Type type)
    {   // Special case for BODY
        if (type == Type.BODY) return getTemp(Type.CORE) + getTemp(Type.BASE);
        // Throw exception if this temperature type is not supported
        if (!temperatures.containsKey(type))
        {   throw new IllegalArgumentException("Invalid temperature type: " + type);
        }

        return temperatures.get(type);
    }

    @Override
    public double getAbility(Ability type)
    {
        if (!abilities.containsKey(type))
        {   throw new IllegalArgumentException("Invalid ability type: " + type);
        }
        return abilities.get(type);
    }

    @Override
    public EnumMap<Type, Double> getTemperatures()
    {   return new EnumMap<>(temperatures);
    }

    @Override
    public void setTemp(Type type, double value)
    {
        // Throw exception if this temperature type is not supported
        if (temperatures.replace(type, value) == null)
        {   throw new IllegalArgumentException("Invalid temperature type: " + type);
        }
        temperatures.put(type, value);
        changed |= switch (type)
        {
            case CORE  -> ((int) value) != ((int) getTemp(Type.CORE));
            case BASE  -> ((int) value) != ((int) getTemp(Type.BASE));
            case WORLD -> Math.abs(value - getTemp(Type.WORLD)) >= 0.02;
            default -> false;
        };
    }

    public void setTemp(Type type, double value, Entity entity)
    {
        double oldTemp = getTemp(type);
        if (oldTemp != value && entity instanceof ServerPlayer player)
        {   ModAdvancementTriggers.TEMPERATURE_CHANGED.trigger(player, this.getTemperatures());
        }
        this.setTemp(type, value);
    }

    @Override
    public void setAbility(Ability type, double value)
    {   // Throw exception if this ability type is not supported
        if (abilities.replace(type, value) == null)
        {   throw new IllegalArgumentException("Invalid ability type: " + type);
        }
        changed |= value != getAbility(type);
    }

    @Override
    public void addModifier(TempModifier modifier, Type type)
    {   modifiers.get(type).add(modifier);
    }

    @Override
    public List<TempModifier> getModifiers(Type type)
    {   // Throw exception if this modifier type is not supported
        return modifiers.computeIfAbsent(type, t ->
        {   throw new IllegalArgumentException("Invalid modifier type: " + t);
        });
    }

    @Override
    public boolean hasModifier(Type type, Class<? extends TempModifier> mod)
    {   return getModifiers(type).stream().anyMatch(mod::isInstance);
    }

    @Override
    public void markPersistentAttribute(Attribute attribute)
    {   persistentAttributes.add(attribute);
    }

    @Override
    public void clearPersistentAttribute(Attribute attribute)
    {   persistentAttributes.remove(attribute);
    }

    @Override
    public Collection<Attribute> getPersistentAttributes()
    {   return persistentAttributes;
    }

    @Override
    public void clearModifiers(Type type)
    {   getModifiers(type).clear();
    }

    @Override
    public Temperature.Units getPreferredUnits()
    {   return preferredUnits;
    }

    @Override
    public void setPreferredUnits(Temperature.Units units)
    {   preferredUnits = units;
    }

    public boolean shouldShowBodyTemp()
    {   return showBodyTemp;
    }

    public boolean showAdvancedWorldTemp()
    {   return showWorldTemp;
    }

    /* See Temperature.class for more temperature-related methods */

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
    }

    @Override
    public void tick(LivingEntity entity)
    {
        // Tick TempModifiers and pre-attribute-bases
        double newWorldTemp = this.modifyFromAttribute(entity, Type.WORLD, () -> Temperature.apply(0, entity, Type.WORLD, getModifiers(Type.WORLD)));
        double newBaseTemp  = this.modifyFromAttribute(entity, Type.BASE, () -> Temperature.apply(0, entity, Type.BASE, getModifiers(Type.BASE)));
        double newCoreTemp  = Temperature.apply(getTemp(Type.CORE), entity, Type.CORE, getModifiers(Type.CORE));

        // Get abilities
        double maxTemp = this.modifyFromAttribute(entity, Ability.BURNING_POINT, () -> ConfigSettings.MAX_TEMP.get());
        double minTemp = this.modifyFromAttribute(entity, Ability.FREEZING_POINT, () -> ConfigSettings.MIN_TEMP.get());
        double coldDampening   = this.modifyFromAttribute(entity, Ability.COLD_DAMPENING, () -> 0d);
        double heatDampening   = this.modifyFromAttribute(entity, Ability.HEAT_DAMPENING, () -> 0d);
        double coldResistance  = this.modifyFromAttribute(entity, Ability.COLD_RESISTANCE, () -> 0d);
        double heatResistance  = this.modifyFromAttribute(entity, Ability.HEAT_RESISTANCE, () -> 0d);

        // 1 if newWorldTemp is above max, -1 if below min, 0 if between the values (safe)
        int worldTempSign = CSMath.getSignForRange(newWorldTemp, minTemp, maxTemp);

        boolean isFullyColdDampened = worldTempSign < 0 && coldDampening >= 1;
        boolean isFullyHeatDampened = worldTempSign > 0 && heatDampening >= 1;

        // Don't change player temperature if they're in creative/spectator mode
        if (worldTempSign != 0 && (!(entity instanceof Player player) || !player.isCreative()) && !entity.isSpectator())
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
            newCoreTemp += Temperature.apply(changeBy, entity, Type.RATE, this.getModifiers(Type.RATE));
        }

        // Get the sign of the player's core temperature (-1, 0, or 1)
        int coreTempSign = CSMath.sign(newCoreTemp);
        // If needed, blend the player's temperature back to 0
        if (this.getModifiers(Type.CORE).isEmpty())
        {
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
            {   amount = (coreTempSign == 1 ? newWorldTemp - maxTemp : newWorldTemp - minTemp) / 3;
            }
            // Blend back to 0
            if (amount != 0)
            {   double changeBy = CSMath.maxAbs(amount * ConfigSettings.TEMP_RATE.get(), ConfigSettings.TEMP_RATE.get() / 10d * -coreTempSign);
                newCoreTemp += CSMath.minAbs(changeBy, -getTemp(Type.CORE));
            }
        }

        // Write the new temperature values
        this.setTemp(Type.CORE, CSMath.clamp(newCoreTemp, -150, 150), entity);
        this.setTemp(Type.BASE, CSMath.clamp(newBaseTemp, -150, 150), entity);
        this.setTemp(Type.WORLD, newWorldTemp, entity);
        // Write the new ability values
        this.setAbility(Ability.BURNING_POINT, maxTemp);
        this.setAbility(Ability.FREEZING_POINT, minTemp);
        this.setAbility(Ability.COLD_RESISTANCE, coldResistance);
        this.setAbility(Ability.HEAT_RESISTANCE, heatResistance);
        this.setAbility(Ability.COLD_DAMPENING, coldDampening);
        this.setAbility(Ability.HEAT_DAMPENING, heatDampening);

        if (syncTimer > 0)
        {   syncTimer--;
        }

        // Sync the temperature values to the client
        if (changed && syncTimer <= 0)
        {   this.syncValues(entity);
        }

        // Deal damage to the player at a set interval if temperature is critical
        this.tickHurting(entity, heatResistance, coldResistance);
    }

    private double modifyFromAttribute(LivingEntity entity, Object type, Supplier<Double> defaultValue)
    {
        AttributeInstance attribute = EntityTempManager.getAttribute(type, entity);
        // If the attribute is null, return the default value
        if (attribute == null)
        {   return defaultValue.get();
        }
        // If base attribute is unset
        if (Double.isNaN(attribute.getBaseValue()))
        {
            // If also doesn't have modifiers, return the default value
            if (attribute.getModifiers().isEmpty())
            {   return defaultValue.get();
            }
            // If has modifiers, apply the modifiers to the default value
            else
            {
                double value = defaultValue.get();

                for (AttributeModifier mod : attribute.getModifiers(AttributeModifier.Operation.ADDITION))
                {   value += mod.getAmount();
                }
                double base = value;
                for (AttributeModifier mod : attribute.getModifiers(AttributeModifier.Operation.MULTIPLY_BASE))
                {   base += value * mod.getAmount();
                }
                for (AttributeModifier mod : attribute.getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL))
                {   base *= 1.0D + mod.getAmount();
                }
                return base;
            }
        }
        // If attribute is set, return the attribute value
        else
        {   return CSMath.safeDouble(attribute.getValue()).orElse(defaultValue.get());
        }
    }

    public void syncValues(LivingEntity entity)
    {   Temperature.updateTemperature(entity, this, false);
    }

    public void tickHurting(LivingEntity entity, double heatResistance, double coldResistance)
    {
        double bodyTemp = getTemp(Type.BODY);

        boolean hasGrace = entity.hasEffect(ModEffects.GRACE);
        boolean hasFireResist = entity.hasEffect(MobEffects.FIRE_RESISTANCE);
        boolean hasIceResist = entity.hasEffect(ModEffects.ICE_RESISTANCE);

        Registry<DamageType> damageTypes = entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);

        if (!hasGrace && entity.tickCount % 40 == 0)
        {
            if (bodyTemp >= 100 && !(hasFireResist && ConfigSettings.FIRE_RESISTANCE_ENABLED.get()))
            {   DamageSource hot = new DamageSource(damageTypes.getHolderOrThrow(ModDamageSources.HOT));
                DamageSource hotScaling = new DamageSource(damageTypes.getHolderOrThrow(ModDamageSources.HOT_SCALING));

                entity.hurt(ConfigSettings.DAMAGE_SCALING.get() ? hotScaling : hot, (float) CSMath.blend(ConfigSettings.TEMP_DAMAGE.get(), 0, heatResistance, 0, 1));
            }
            else if (bodyTemp <= -100 && !(hasIceResist && ConfigSettings.ICE_RESISTANCE_ENABLED.get()))
            {   DamageSource cold = new DamageSource(damageTypes.getHolderOrThrow(ModDamageSources.COLD));
                DamageSource coldScaling = new DamageSource(damageTypes.getHolderOrThrow(ModDamageSources.COLD_SCALING));

                 entity.hurt(ConfigSettings.DAMAGE_SCALING.get() ? coldScaling : cold, (float) CSMath.blend(ConfigSettings.TEMP_DAMAGE.get(), 0, coldResistance, 0, 1));
            }
        }
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
    {
        CompoundTag nbt = new CompoundTag();
        // Save the player's temperatures
        nbt.put("Temps", this.serializeTemps());
        // Save the player's modifiers
        nbt.put("TempModifiers", this.serializeModifiers());
        // Save the player's abilities
        nbt.put("Abilities", this.serializeAbilities());
        // Save the player's persistent attributes
        ListTag attributes = new ListTag();
        for (Attribute attribute : this.getPersistentAttributes())
        {   attributes.add(StringTag.valueOf(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString()));
        }
        nbt.put("PersistentAttributes", attributes);
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
            {   modifiers.add(NBTHelper.modifierToTag(modifier));
            }
            // Write the list of modifiers to the player's persistent data
            nbt.put(NBTHelper.getTemperatureTag(type), modifiers);
        }
        return nbt;
    }

    @Override
    public CompoundTag serializeAbilities()
    {
        CompoundTag nbt = new CompoundTag();

        // Save the player's abilities
        for (Ability type : Ability.values())
        {   nbt.putDouble(NBTHelper.getAbilityTag(type), this.getAbility(type));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {   // Load the player's temperatures
        deserializeTemps(nbt.getCompound("Temps"));
        // Load the player's modifiers
        deserializeModifiers(nbt.getCompound("TempModifiers"));
        // Load the player's abilities
        deserializeAbilities(nbt.getCompound("Abilities"));
        // Load the player's persistent attributes
        ListTag attributes = nbt.getList("PersistentAttributes", 8);
        for (int i = 0; i < attributes.size(); i++)
        {   this.markPersistentAttribute(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributes.getString(i))));
        }
    }

    @Override
    public void deserializeTemps(CompoundTag nbt)
    {
        for (Type type : VALID_TEMPERATURE_TYPES)
        {   setTemp(type, nbt.getDouble(NBTHelper.getTemperatureTag(type)));
        }
    }

    @Override
    public void deserializeModifiers(CompoundTag nbt)
    {
        for (Type type : VALID_MODIFIER_TYPES)
        {
            getModifiers(type).clear();

            // Get the list of modifiers from the player's persistent data
            ListTag modifiers = nbt.getList(NBTHelper.getTemperatureTag(type), 10);

            // For each modifier in the list
            modifiers.forEach(modNBT ->
            {
                NBTHelper.tagToModifier((CompoundTag) modNBT).ifPresent(modifier ->
                {   getModifiers(type).add(modifier);
                });
            });
        }
    }

    @Override
    public void deserializeAbilities(CompoundTag nbt)
    {
        for (Ability type : Ability.values())
        {   setAbility(type, nbt.getDouble(NBTHelper.getAbilityTag(type)));
        }
    }
}
