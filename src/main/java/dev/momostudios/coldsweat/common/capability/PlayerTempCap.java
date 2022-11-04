package dev.momostudios.coldsweat.common.capability;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.TempHelper;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.entity.ModDamageSources;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all the information regarding the entity's temperature. This should very rarely be used directly.
 */
public class PlayerTempCap implements ITemperatureCap
{
    static Temperature.Type[] VALID_MODIFIER_TYPES = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.RATE, Temperature.Type.MAX, Temperature.Type.MIN, Temperature.Type.WORLD};
    static Temperature.Type[] VALID_TEMPERATURE_TYPES = {Temperature.Type.CORE, Temperature.Type.BASE, Temperature.Type.MAX, Temperature.Type.MIN, Temperature.Type.WORLD};

    private double[] syncedValues = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
    double ticksSinceSync = 0;

    double worldTemp = 1;
    double coreTemp = 0;
    double baseTemp = 0;
    double maxOffset = 0;
    double minOffset = 0;

    List<TempModifier> worldModifiers = new ArrayList<>();
    List<TempModifier> coreModifiers  = new ArrayList<>();
    List<TempModifier> baseModifiers  = new ArrayList<>();
    List<TempModifier> rateModifiers  = new ArrayList<>();
    List<TempModifier> maxModifiers   = new ArrayList<>();
    List<TempModifier> minModifiers   = new ArrayList<>();

    public boolean showBodyTemp;
    public boolean showWorldTemp;

    public double getTemp(Temperature.Type type)
    {
        return switch (type)
        {
            case WORLD -> worldTemp;
            case CORE  -> coreTemp;
            case BASE  -> baseTemp;
            case BODY  -> baseTemp + coreTemp;
            case MAX   -> maxOffset;
            case MIN   -> minOffset;
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.getValue(): " + type);
        };
    }

    public void setTemp(Temperature.Type type, double value)
    {
        switch (type)
        {
            case WORLD -> this.worldTemp = value;
            case CORE  -> this.coreTemp  = value;
            case BASE  -> this.baseTemp  = value;
            case MAX   -> this.maxOffset = value;
            case MIN   -> this.minOffset = value;
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.setValue(): " + type);
        }
    }

    public List<TempModifier> getModifiers(Temperature.Type type)
    {
        return switch (type)
        {
            case WORLD -> worldModifiers;
            case CORE  -> coreModifiers;
            case BASE  -> baseModifiers;
            case RATE  -> rateModifiers;
            case MAX   -> maxModifiers;
            case MIN   -> minModifiers;
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.getModifiers(): " + type);
        };
    }

    public boolean hasModifier(Temperature.Type type, Class<? extends TempModifier> mod)
    {
        return switch (type)
        {
            case WORLD -> this.worldModifiers.stream().anyMatch(mod::isInstance);
            case CORE  -> this.coreModifiers.stream().anyMatch(mod::isInstance);
            case BASE  -> this.baseModifiers.stream().anyMatch(mod::isInstance);
            case RATE  -> this.rateModifiers.stream().anyMatch(mod::isInstance);
            case MAX   -> this.maxModifiers.stream().anyMatch(mod::isInstance);
            case MIN   -> this.minModifiers.stream().anyMatch(mod::isInstance);
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.hasModifier(): " + type);
        };
    }

    public void clearModifiers(Temperature.Type type)
    {
        switch (type)
        {
            case WORLD -> this.worldModifiers.clear();
            case CORE  -> this.coreModifiers.clear();
            case BASE  -> this.baseModifiers.clear();
            case RATE  -> this.rateModifiers.clear();
            case MAX   -> this.maxModifiers.clear();
            case MIN   -> this.minModifiers.clear();
            default -> throw new IllegalArgumentException("Illegal type for PlayerTempCapability.clearModifiers(): " + type);
        }
    }

    public void tickDummy(Player player)
    {
        for (Temperature.Type type : VALID_MODIFIER_TYPES)
        {
            new Temperature().with(player, getModifiers(type));
        }
    }

    public void tick(Player player)
    {
        ConfigSettings config = ConfigSettings.getInstance();

        // Tick expiration time for world modifiers
        double newWorldTemp = new Temperature().with(player, getModifiers(Temperature.Type.WORLD)).get();
        double newCoreTemp  = new Temperature(this.coreTemp).with(player, getModifiers(Temperature.Type.CORE)).get();
        double newBaseTemp  = new Temperature().with(player, getModifiers(Temperature.Type.BASE)).get();
        double newMaxOffset = new Temperature().with(player, getModifiers(Temperature.Type.MAX)).get();
        double newMinOffset = new Temperature().with(player, getModifiers(Temperature.Type.MIN)).get();

        double maxTemp = config.maxTemp + newMaxOffset;
        double minTemp = config.minTemp + newMinOffset;

        double tempRate = 7.0d;

        // 1 if newWorldTemp is above max, -1 if below min, 0 if between the values (safe)
        int magnitude = CSMath.getSignForRange(newWorldTemp, minTemp, maxTemp);

        // Don't make the player's temperature worse if they're in creative/spectator mode
        if (magnitude != 0 && !(player.isCreative() || player.isSpectator()))
        {
            double difference = Math.abs(newWorldTemp - CSMath.clamp(newWorldTemp, minTemp, maxTemp));
            Temperature changeBy = new Temperature(Math.max((difference / tempRate) * (float)config.rate, Math.abs((float)config.rate / 50)) * magnitude);
            newCoreTemp = changeBy.with(player, getModifiers(Temperature.Type.RATE)).add(newCoreTemp).get();
        }
        // If the player's temperature and world temperature are not both hot or both cold
        if (magnitude != CSMath.getSign(newCoreTemp))
        {
            // Return the player's body temperature to 0
            newCoreTemp += getBodyReturnRate(newWorldTemp, newCoreTemp > 0 ? maxTemp : minTemp, config.rate, newCoreTemp);
        }

        // Update whether certain UI elements are being displayed (temp isn't synced if the UI element isn't showing)
        if (player.tickCount % 20 == 0)
        {
            showWorldTemp = !ConfigSettings.getInstance().requireThermometer
                         || player.getInventory().items.stream().limit(9).anyMatch(stack -> stack.getItem() == ModItems.THERMOMETER)
                         || player.getOffhandItem().getItem() == ModItems.THERMOMETER;
            showBodyTemp = !player.isCreative() && !player.isSpectator();
        }

        // Write the new temperature values
        setTemp(Temperature.Type.BASE, newBaseTemp);
        setTemp(Temperature.Type.CORE, CSMath.clamp(newCoreTemp, -150f, 150f));
        setTemp(Temperature.Type.WORLD, newWorldTemp);
        setTemp(Temperature.Type.MAX, newMaxOffset);
        setTemp(Temperature.Type.MIN, newMinOffset);

        // Sync the temperature values to the client
        if (ticksSinceSync++ >= 5
        && (((Math.abs(syncedValues[0] - newCoreTemp) >= 1 && showBodyTemp))
        ||  ((Math.abs(syncedValues[1] - newBaseTemp) >= 1 && showBodyTemp))
        ||  ((Math.abs(syncedValues[2] - newWorldTemp) >= 0.02 && showWorldTemp))
        ||  ((Math.abs(syncedValues[3] - newMaxOffset) >= 0.02 && showWorldTemp))
        ||  ((Math.abs(syncedValues[4] - newMinOffset) >= 0.02 && showWorldTemp))))
        {
            TempHelper.updateTemperature(player, this, false);
            syncedValues = new double[] { newCoreTemp, newBaseTemp, newWorldTemp, newMaxOffset, newMinOffset };
            ticksSinceSync = 0;
        }

        // Calculate body/base temperatures with modifiers
        double bodyTemp = getTemp(Temperature.Type.BODY);

        //Deal damage to the player if temperature is critical
        if (player.tickCount % 40 == 0 && !player.hasEffect(ModEffects.GRACE))
        {
            boolean damageScaling = config.damageScaling;

            if (bodyTemp >= 100 && !(player.hasEffect(MobEffects.FIRE_RESISTANCE) && config.fireRes))
            {
                player.hurt(damageScaling ? ModDamageSources.HOT.setScalesWithDifficulty() : ModDamageSources.HOT, 2f);
            }
            else if (bodyTemp <= -100 && !(player.hasEffect(ModEffects.ICE_RESISTANCE) && config.iceRes))
            {
                player.hurt(damageScaling ? ModDamageSources.COLD.setScalesWithDifficulty() : ModDamageSources.COLD, 2f);
            }
        }
    }

    // Used for returning the player's temperature back to 0
    private double getBodyReturnRate(double worldTemp, double tempLimit, double tempRate, double bodyTemp)
    {
        double staticRate = 3.0d;
        // Get the difference between the world temp and the threshold to determine the speed of return (closer to the threshold = slower)
        // Divide it by the staticRate (3) because it feels nice
        // Multiply it by the configured tempRate Rate Modifier
        // If it's too slow, default to tempRate / 10 instead
        // Multiply it by -CSMath.getSign(bodyTemp) to make it go toward 0
        double changeBy = Math.max((Math.abs(worldTemp - tempLimit) / staticRate) * tempRate, tempRate / 10) * -CSMath.getSign(bodyTemp);
        return CSMath.getLeastExtreme(changeBy, -bodyTemp);
    }

    @Override
    public void copy(ITemperatureCap cap)
    {
        // Copy temperature values
        for (Temperature.Type type : VALID_TEMPERATURE_TYPES)
        {
            if (type == Temperature.Type.BODY || type == Temperature.Type.RATE) continue;
            this.setTemp(type, cap.getTemp(type));
        }

        // Copy the modifiers
        for (Temperature.Type type : VALID_MODIFIER_TYPES)
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

    public CompoundTag serializeTemps()
    {
        CompoundTag nbt = new CompoundTag();

        // Save the player's temperature data
        for (Temperature.Type type : VALID_TEMPERATURE_TYPES)
        {
            nbt.putDouble(TempHelper.getTempTag(type), this.getTemp(type));
        }
        return nbt;
    }

    public CompoundTag serializeModifiers()
    {
        CompoundTag nbt = new CompoundTag();

        // Save the player's modifiers
        for (Temperature.Type type : VALID_MODIFIER_TYPES)
        {
            ListTag modifiers = new ListTag();
            for (TempModifier modifier : this.getModifiers(type))
            {
                modifiers.add(NBTHelper.modifierToTag(modifier));
            }

            // Write the list of modifiers to the player's persistent data
            nbt.put(TempHelper.getModifierTag(type), modifiers);
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

    public void deserializeTemps(CompoundTag nbt)
    {
        for (Temperature.Type type : VALID_TEMPERATURE_TYPES)
        {
            setTemp(type, nbt.getDouble(TempHelper.getTempTag(type)));
        }
    }

    public void deserializeModifiers(CompoundTag nbt)
    {
        for (Temperature.Type type : VALID_MODIFIER_TYPES)
        {
            getModifiers(type).clear();

            // Get the list of modifiers from the player's persistent data
            ListTag modifiers = nbt.getList(TempHelper.getModifierTag(type), 10);

            // For each modifier in the list
            modifiers.forEach(modNBT ->
            {
                TempModifier modifier = NBTHelper.tagToModifier((CompoundTag) modNBT);

                // Add the modifier to the player's temperature
                if (modifier != null)
                    getModifiers(type).add(modifier);
                else
                    ColdSweat.LOGGER.error("Failed to load modifier of type {}", type);
            });
        }
    }
}
