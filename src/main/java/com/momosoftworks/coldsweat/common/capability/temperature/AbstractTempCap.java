package com.momosoftworks.coldsweat.common.capability.temperature;

import com.google.common.math.DoubleMath;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.common.temperautre.TemperatureChangedEvent;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Temperature.Trait;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import com.momosoftworks.coldsweat.data.codec.configuration.EntityClimateData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModDamageSources;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.StringNBT;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager.*;

/**
 * Holds all the information regarding the entity's temperature. This should very rarely be used directly.
 */
public class AbstractTempCap implements ITemperatureCap
{
    boolean changed = true;
    int syncTimer = 0;
    Temperature.Units preferredUnits = Temperature.Units.F;

    private final Set<Attribute> persistentAttributes = new HashSet<>();

    // Map valid temperature types to a new EnumMap
    private final EnumMap<Trait, Double> traits = new EnumMap<>(Trait.class);

    // Map valid modifier types to a new EnumMap
    private final EnumMap<Trait, List<TempModifier>> modifiers = new EnumMap<>(Trait.class);

    // Store entity's attribute data for faster access
    private final EnumMap<Trait, ModifiableAttributeInstance> attributes = new EnumMap<>(Trait.class);
    private final Map<ModifiableAttributeInstance, Map<AttributeModifier.Operation, Set<AttributeModifier>>> attributeModifiers = new HashMap<>();

    private final HashMap<TempEffectType<?>, TempEffect> tempEffects = new HashMap<>();

    public boolean showBodyTemp;
    public boolean showWorldTemp;

    @Override
    public double getTrait(Trait trait)
    {
        // Special case for BODY
        if (trait == Trait.BODY)
        {   return getTrait(Trait.CORE) + getTrait(Trait.BASE);
        }
        // Throw exception if this temperature trait is not supported
        if (!trait.isForTemperature())
        {   throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("Invalid temperature trait: " + trait));
        }

        return traits.computeIfAbsent(trait, t -> 0.0);
    }

    @Override
    public EnumMap<Trait, Double> getTraits()
    {   return new EnumMap<>(traits);
    }

    @Override
    public void setTrait(Trait trait, double value)
    {
        // Throw exception if this temperature trait is not supported
        if (!trait.isForTemperature())
        {   throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("Invalid temperature trait: " + trait));
        }
        switch (trait)
        {
            case CORE  : changed |= ((int) value) != ((int) getTrait(Trait.CORE)); break;
            case BASE  : changed |= ((int) value) != ((int) getTrait(Temperature.Trait.BASE)); break;
            case WORLD : changed |= Math.abs(value - getTrait(Trait.WORLD)) >= 0.02; break;
            default : changed |= true;
        }
        traits.put(trait, value);
    }

    public void setTrait(Trait trait, double value, LivingEntity entity)
    {
        double oldTemp = this.getTrait(trait);
        this.setTrait(trait, value);
        if (entity.tickCount > 5 && oldTemp != value && entity instanceof ServerPlayerEntity)
        {   ModAdvancementTriggers.TEMPERATURE_CHANGED.trigger(((ServerPlayerEntity) entity), this.getTraits());
        }
    }

    @Override
    public void addModifier(TempModifier modifier, Trait trait)
    {
        if (!trait.isForModifiers())
            throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("Invalid modifier trait: " + trait));
        this.getModifiers(trait).add(modifier);
    }

    @Override
    public void removeModifier(TempModifier modifier, Trait trait)
    {
        if (!trait.isForModifiers())
            throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("Invalid modifier trait: " + trait));
        this.getModifiers(trait).remove(modifier);
    }

    @Override
    public EnumMap<Trait, List<TempModifier>> getModifiers()
    {   return modifiers;
    }

    @Override
    public List<TempModifier> getModifiers(Trait trait)
    {
        // Throw exception if this modifier type is not supported
        if (!trait.isForModifiers())
            throw ColdSweat.LOGGER.throwing(new IllegalArgumentException("Invalid modifier trait: " + trait));
        return modifiers.computeIfAbsent(trait, t -> new ArrayList<>());
    }

    @Override
    public void clearModifiers()
    {   this.modifiers.clear();
    }

    @Override
    public void setModifiers(Map<Trait, List<TempModifier>> modifiers)
    {   this.clearModifiers();
        this.modifiers.putAll(modifiers);
    }

    @Override
    public boolean hasModifier(Trait trait, Class<? extends TempModifier> mod)
    {   return this.getModifiers(trait).stream().anyMatch(mod::isInstance);
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
    public void clearModifiers(Trait trait)
    {   getModifiers(trait).clear();
    }

    public boolean shouldShowBodyTemp()
    {   return showBodyTemp;
    }

    public boolean showAdvancedWorldTemp()
    {   return showWorldTemp;
    }

    private ModifiableAttributeInstance getAttribute(LivingEntity entity, Trait trait)
    {   return attributes.computeIfAbsent(trait, t -> EntityTempManager.getAttribute(t, entity));
    }

    private Set<AttributeModifier> getAttributeModifiers(ModifiableAttributeInstance attribute, AttributeModifier.Operation operation)
    {
        Map<AttributeModifier.Operation, Set<AttributeModifier>> modifiers = this.attributeModifiers.computeIfAbsent(attribute, at ->
        {
            Map<AttributeModifier.Operation, Set<AttributeModifier>> map = new HashMap<>();
            for (AttributeModifier.Operation op : AttributeModifier.Operation.values())
            {   map.put(op, at.getModifiers(op));
            }
            return map;
        });
        return modifiers.computeIfAbsent(operation, attribute::getModifiers);
    }

    @Override
    public HashMap<TempEffectType<?>, TempEffect> getTempEffects()
    {   return tempEffects;
    }

    @Override
    public void addTempEffect(TempEffect effect, boolean isClient)
    {
        if (!tempEffects.containsKey(effect.type()))
        {
            // Add temp effect
            tempEffects.put(effect.type(), effect);
            // Register the effect to the event bus
            if (effect.getSide().checkSide(isClient))
            {   MinecraftForge.EVENT_BUS.register(effect);
            }
        }
    }

    @Override
    public void removeTempEffect(TempEffectType<?> effect)
    {
        this.tempEffects.computeIfPresent(effect, (type, e) ->
        {
            MinecraftForge.EVENT_BUS.unregister(e);
            return null;
        });
    }

    @Override
    public void clearTempEffects()
    {   this.tempEffects.values().forEach(MinecraftForge.EVENT_BUS::unregister);
        this.tempEffects.clear();
    }

    @Override
    public void tick(LivingEntity entity)
    {
        // Apply temp modifiers
        double worldTemp = this.modifyFromAttribute(entity, Trait.WORLD, 0);
        double baseTemp  = this.modifyFromAttribute(entity, Trait.BASE,  0);
        double coreTemp  = Temperature.apply(this.getTrait(Trait.CORE), entity, Trait.CORE, this.getModifiers(Trait.CORE));
        double maxTemp = this.modifyFromAttribute(entity, Trait.BURNING_POINT,  ConfigSettings.MAX_TEMP.get());
        double minTemp = this.modifyFromAttribute(entity, Trait.FREEZING_POINT, ConfigSettings.MIN_TEMP.get());
        double coldDampening  = this.modifyFromAttribute(entity, Trait.COLD_DAMPENING,  0);
        double heatDampening  = this.modifyFromAttribute(entity, Trait.HEAT_DAMPENING,  0);
        double coldResistance = this.modifyFromAttribute(entity, Trait.COLD_RESISTANCE, 0);
        double heatResistance = this.modifyFromAttribute(entity, Trait.HEAT_RESISTANCE, 0);

        double rate = 0;

        // 1 if newWorldTemp is above max, -1 if below min, 0 if between the values (safe)
        int worldTempSign = CSMath.signForRange(worldTemp, minTemp, maxTemp);

        // Don't change player temperature if they're in creative/spectator mode
        if (worldTempSign != 0 && (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isCreative()) && !entity.isSpectator()
        && !EntityTempManager.isPeacefulMode(entity))
        {
            // How much hotter/colder the player's temp is compared to max/min
            double difference = Math.abs(worldTemp - CSMath.clamp(worldTemp, minTemp, maxTemp));

            // How much the player's temperature should change
            double changeBy = (Math.max(
                    // Change proportionally to the w of the world temperature
                    (difference / 7d) * ConfigSettings.TEMP_RATE.get(),
                    // Ensure a minimum speed for temperature change
                    Math.abs(ConfigSettings.TEMP_RATE.get() / 50d)
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
            // Apply temp/attribute modifiers
            rate = this.modifyFromAttribute(entity, Trait.RATE, changeBy);
            // Apply rate multiplier if entity has climate data
            rate *= CSMath.getIfNotNull(ConfigHelper.getFirstOrNull(ConfigSettings.ENTITY_CLIMATES, entity.getType(), data -> data.test(entity)), EntityClimateData::rate, 1.0);
            // Apply the rate to entity's temperature
            coreTemp += rate;
        }

        // If needed, equalize the player's temperature back to 0
        double equilibrium = getEquilibriumDelta(coreTemp, entity);
        int coreDeltaSign = CSMath.sign(coreTemp - this.getTrait(Trait.CORE));
        int equilibriumSign = CSMath.sign(equilibrium);
        // Only apply equilibrium delta if it isn't working against any CORE modifiers
        if (coreDeltaSign == 0 || coreDeltaSign == equilibriumSign)
        {   coreTemp += equilibrium;
        }

        // Write the new temperature values
        this.setTrait(Trait.CORE, CSMath.clamp(coreTemp, -150, 150), entity);
        this.setTrait(Trait.BASE, CSMath.clamp(baseTemp, -150, 150), entity);
        this.setTrait(Trait.WORLD, worldTemp, entity);
        // Write the new ability values
        this.setTrait(Trait.BURNING_POINT, maxTemp);
        this.setTrait(Trait.FREEZING_POINT, minTemp);
        this.setTrait(Trait.COLD_RESISTANCE, coldResistance);
        this.setTrait(Trait.HEAT_RESISTANCE, heatResistance);
        this.setTrait(Trait.COLD_DAMPENING, coldDampening);
        this.setTrait(Trait.HEAT_DAMPENING, heatDampening);
        this.setTrait(Trait.RATE, rate);

        if (syncTimer > 0)
        {   syncTimer--;
        }

        // Sync the temperature values to the client
        if (changed && syncTimer <= 0)
        {   this.syncValues(entity);
        }

        // Deal damage to the player at a set interval if temperature is critical
        this.tickHurting(entity);
    }

    protected double modifyFromAttribute(LivingEntity entity, Temperature.Trait trait, double baseValue)
    {
        double newValue = this.modifyFromAttribute(entity, trait, this.getModifiers(trait), baseValue);
        double oldValue = this.getTrait(trait);
        if (!DoubleMath.fuzzyEquals(newValue, oldValue, 0.001))
        {
            // Fire temperature change event
            TemperatureChangedEvent event = new TemperatureChangedEvent(entity, trait, oldValue, newValue);
            MinecraftForge.EVENT_BUS.post(event);
            // Write new value to NBT
            double eventTemp = event.getTemperature();
            NBTHelper.getOrPutTag(entity, "Temperature", new CompoundNBT()).putDouble(trait.getSerializedName(), eventTemp);
            // Return temperature from event
            return eventTemp;
        }
        // Return
        return oldValue;
    }

    protected double getEquilibriumDelta(double coreTemp, LivingEntity entity)
    {
        double worldTemp = this.getTrait(Trait.WORLD);
        double minTemp = this.getTrait(Trait.FREEZING_POINT);
        double maxTemp = this.getTrait(Trait.BURNING_POINT);
        double coldDampening = this.getTrait(Trait.COLD_DAMPENING);
        double heatDampening = this.getTrait(Trait.HEAT_DAMPENING);
        boolean immuneToTemp = isPeacefulMode(entity);

        int worldTempSign = CSMath.signForRange(worldTemp, minTemp, maxTemp);
        boolean isFullyColdDampened = worldTempSign < 0 && (coldDampening >= 1 || immuneToTemp);
        boolean isFullyHeatDampened = worldTempSign > 0 && (heatDampening >= 1 || immuneToTemp);

        // Get the sign of the player's core temperature (-1, 0, or 1)
        int coreTempSign = CSMath.sign(coreTemp);
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
            changeBy = CSMath.minAbs(changeBy, -getTrait(Trait.CORE));
            return changeBy;
        }
        return 0;
    }

    @Override
    public double modifyFromAttribute(LivingEntity entity, Trait trait, List<TempModifier> modifiers, double baseValue)
    {
        Supplier<Double> defaultSupplier = () -> Temperature.apply(baseValue, entity, trait, modifiers);
        ModifiableAttributeInstance attribute = this.getAttribute(entity, trait);
        double newValue;
        // If the attribute is null, return the default value
        if (attribute == null)
        {   newValue = defaultSupplier.get();
        }
        else
        {
            double base = CSMath.safeDouble(attribute.getBaseValue()).orElseGet(defaultSupplier);

            if (modifiers.isEmpty())
            {   newValue = base;
            }
            else
            {
                for (AttributeModifier mod : this.getAttributeModifiers(attribute, AttributeModifier.Operation.ADDITION))
                {   base += mod.getAmount();
                }
                double value = base;
                for (AttributeModifier mod : this.getAttributeModifiers(attribute, AttributeModifier.Operation.MULTIPLY_BASE))
                {   value += base * mod.getAmount();
                }
                for (AttributeModifier mod : this.getAttributeModifiers(attribute, AttributeModifier.Operation.MULTIPLY_TOTAL))
                {
                        value *= 1.0D + mod.getAmount();
                }
                newValue = value;
            }
        }
        return newValue;
    }

    @Override
    public void syncValues(LivingEntity entity)
    {
        Temperature.updateTemperature(entity, this, false);
        changed = false;
        syncTimer = 5;
    }

    public int getHurtInterval(LivingEntity entity)
    {   return ConfigSettings.TEMPERATURE_HURT_INTERVAL.get();
    }

    public void tickHurting(LivingEntity entity)
    {
        if (EntityTempManager.isPeacefulMode(entity)) return;

        double bodyTemp = this.getTrait(Temperature.Trait.BODY);
        double heatResistance = this.getTrait(Trait.HEAT_RESISTANCE);
        double coldResistance = this.getTrait(Trait.COLD_RESISTANCE);
        double damage = ConfigSettings.TEMP_DAMAGE.get();
        double rate = this.getTrait(Trait.RATE);
        int hurtInterval = this.getHurtInterval(entity);

        if (hurtInterval < 1) return;

        boolean hasGrace = entity.hasEffect(ModEffects.GRACE);
        boolean hasFireResist = entity.hasEffect(Effects.FIRE_RESISTANCE);
        boolean hasIceResist = entity.hasEffect(ModEffects.ICE_RESISTANCE);

        // Don't damage faster if body temp is equalizing
        double rateFactor = CSMath.sign(bodyTemp) == CSMath.sign(rate) ? Math.abs(rate) : 0;
        // Get damage interval based on rate of temp change
        int rateInterval = (int) CSMath.blend(1, 4, rateFactor, 0, 0.7);

        if (!hasGrace && entity.tickCount % (hurtInterval / rateInterval) == 0)
        {
            if (bodyTemp >= 100 && !(hasFireResist && ConfigSettings.FIRE_RESISTANCE_ENABLED.get()))
            {
                DamageSource hot = ModDamageSources.HOT;
                entity.hurt(hot, (float) CSMath.blend(damage, 0, heatResistance, 0, 1));
            }
            else if (bodyTemp <= -100 && !(hasIceResist && ConfigSettings.ICE_RESISTANCE_ENABLED.get()))
            {
                DamageSource cold = ModDamageSources.COLD;
                entity.hurt(cold, (float) CSMath.blend(damage, 0, coldResistance, 0, 1));
            }
        }
    }

    @Override
    public void copy(ITemperatureCap cap)
    {
        // Copy temperature values
        for (Trait trait : VALID_TEMPERATURE_TRAITS)
        {   this.setTrait(trait, cap.getTrait(trait));
        }

        // Copy the modifiers
        for (Trait trait : VALID_MODIFIER_TRAITS)
        {   this.getModifiers(trait).clear();
            this.getModifiers(trait).addAll(cap.getModifiers(trait));
        }

        // Copy persistent attributes
        this.persistentAttributes.clear();
        this.persistentAttributes.addAll(cap.getPersistentAttributes());
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT nbt = new CompoundNBT();
        // Save the player's temperatures
        nbt.put("Traits", this.serializeTraits());
        // Save the player's modifiers
        nbt.put("TempModifiers", this.serializeModifiers());
        // Save the player's persistent attributes
        ListNBT attributes = new ListNBT();
        for (Attribute attribute : this.getPersistentAttributes())
        {   attributes.add(StringNBT.valueOf(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString()));
        }
        nbt.put("PersistentAttributes", attributes);
        return nbt;
    }

    @Override
    public CompoundNBT serializeTraits()
    {
        CompoundNBT nbt = new CompoundNBT();

        // Save the player's temperature data
        for (Map.Entry<Trait, Double> trait : traits.entrySet())
        {   nbt.putDouble(NBTHelper.getTraitTagKey(trait.getKey()), trait.getValue());
        }
        return nbt;
    }

    @Override
    public CompoundNBT serializeModifiers()
    {
        CompoundNBT nbt = new CompoundNBT();

        // Save the player's modifiers
        for (Trait trait : VALID_MODIFIER_TRAITS)
        {
            ListNBT modifiers = new ListNBT();
            for (TempModifier modifier : this.getModifiers(trait))
            {   modifiers.add(TempModifier.CODEC.encodeStart(NBTDynamicOps.INSTANCE, modifier).getOrThrow(false, ColdSweat.LOGGER::error));
            }
            // Write the list of modifiers to the player's persistent data
            nbt.put(NBTHelper.getTraitTagKey(trait), modifiers);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {   // Load the player's temperatures
        deserializeTraits(nbt.getCompound("Traits"));
        // Load the player's modifiers
        deserializeModifiers(nbt.getCompound("TempModifiers"));
        // Load the player's persistent attributes
        ListNBT attributes = nbt.getList("PersistentAttributes", 8);
        for (int i = 0; i < attributes.size(); i++)
        {   this.markPersistentAttribute(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributes.getString(i))));
        }
    }

    @Override
    public void deserializeTraits(CompoundNBT nbt)
    {
        for (Trait trait : VALID_TEMPERATURE_TRAITS)
        {   setTrait(trait, nbt.getDouble(NBTHelper.getTraitTagKey(trait)));
        }
    }

    @Override
    public void deserializeModifiers(CompoundNBT nbt)
    {
        Map<Trait, List<TempModifier>> modifiers = new EnumMap<>(Trait.class);
        Map<Integer, TempModifier> modifierHashes = new HashMap<>();
        for (Trait trait : VALID_MODIFIER_TRAITS)
        {
            // Get the list of modifiers from the player's persistent data
            ListNBT modTags = nbt.getList(NBTHelper.getTraitTagKey(trait), 10);

            // For each modifier in the list
            modTags.forEach(entry ->
            {
                CompoundNBT modNBT = ((CompoundNBT) entry);
                TempModifier modifier;
                boolean isLegacy = modNBT.contains("Id"); // Legacy NBT data stored not using the codec
                int modHash = modNBT.getInt(isLegacy ? "Hash" : "hash");
                if (modHash == 0 || !modifierHashes.containsKey(modHash))
                {
                    Optional<TempModifier> modOpt = isLegacy ? NBTHelper.tagToModifier(modNBT) // Legacy modifier NBT data
                                                             : TempModifier.CODEC.decode(NBTDynamicOps.INSTANCE, modNBT).result().map(Pair::getFirst);
                    if (!modOpt.isPresent()) return;
                    modifier = modOpt.get();
                    if (modHash != 0)
                    {   modifierHashes.put(modHash, modifier);
                    }
                }
                else
                {   modifier = modifierHashes.get(modHash);
                }
                modifiers.computeIfAbsent(trait, t -> new ArrayList<>()).add(modifier);
            });
        }
        // Add the modifiers to the player's modifiers
        this.modifiers.clear();
        this.modifiers.putAll(modifiers);
    }
}
