package dev.momostudios.coldsweat.core.capabilities;

import dev.momostudios.coldsweat.util.CSDamageTypes;
import dev.momostudios.coldsweat.util.registrylists.ModEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.util.CSMath;
import dev.momostudios.coldsweat.util.PlayerHelper;

import java.util.ArrayList;
import java.util.List;

public class PlayerTempCapability
{
    @CapabilityInject(PlayerTempCapability.class)
    public static Capability<PlayerTempCapability> TEMPERATURE = null;

    double ambiTemp;
    double bodyTemp;
    double baseTemp;
    double compTemp;
    List<TempModifier> ambientModifiers = new ArrayList<>();
    List<TempModifier> bodyModifiers = new ArrayList<>();
    List<TempModifier> baseModifiers = new ArrayList<>();
    List<TempModifier> rateModifiers = new ArrayList<>();

    public double get(PlayerHelper.Types type)
    {
        switch (type)
        {
            case AMBIENT:  return ambiTemp;
            case BODY:     return bodyTemp;
            case BASE:     return baseTemp;
            case COMPOSITE:return compTemp;
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.getValue(): " + type);
        }
    }

    public void set(PlayerHelper.Types type, double value)
    {
        switch (type)
        {
            case AMBIENT:  { this.ambiTemp = value; break; }
            case BODY:     { this.bodyTemp = value; break; }
            case BASE:     { this.baseTemp = value; break; }
            case COMPOSITE:{ this.compTemp = value; break; }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.setValue(): " + type);
        }
    }

    public List<TempModifier> getModifiers(PlayerHelper.Types type)
    {
        switch (type)
        {
            case AMBIENT:  { return ambientModifiers; }
            case BODY:     { return bodyModifiers; }
            case BASE:     { return baseModifiers; }
            case RATE:     { return rateModifiers; }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.getModifiers(): " + type);
        }
    }

    public boolean hasModifier(PlayerHelper.Types type, Class<? extends TempModifier> mod)
    {
        switch (type)
        {
            case AMBIENT:  { return this.ambientModifiers.stream().anyMatch(mod::isInstance); }
            case BODY:     { return this.bodyModifiers.stream().anyMatch(mod::isInstance); }
            case BASE:     { return this.baseModifiers.stream().anyMatch(mod::isInstance); }
            case RATE:     { return this.rateModifiers.stream().anyMatch(mod::isInstance); }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.hasModifier(): " + type);
        }
    }


    /**
     * Do NOT use this! <br>
     */
    public void clearModifiers(PlayerHelper.Types type)
    {
        switch (type)
        {
            case AMBIENT:  { this.ambientModifiers.clear(); break; }
            case BODY:     { this.bodyModifiers.clear(); break; }
            case BASE:     { this.baseModifiers.clear(); break; }
            case RATE:     { this.rateModifiers.clear(); break; }
            default: throw new IllegalArgumentException("Illegal type for PlayerTempCapability.clearModifiers(): " + type);
        }
    }

    public void tickClient(PlayerEntity player)
    {
        tickModifiers(new Temperature(), player, getModifiers(PlayerHelper.Types.AMBIENT));
        tickModifiers(new Temperature(), player, getModifiers(PlayerHelper.Types.BODY));
        tickModifiers(new Temperature(), player, getModifiers(PlayerHelper.Types.BASE));
        tickModifiers(new Temperature(), player, getModifiers(PlayerHelper.Types.RATE));
    }

    public void tickUpdate(PlayerEntity player)
    {
        ConfigCache config = ConfigCache.getInstance();

        // Tick expiration time for ambient modifiers
        Temperature ambient = tickModifiers(new Temperature(), player, getModifiers(PlayerHelper.Types.AMBIENT));
        double ambientTemp = ambient.get();

        // Apply ambient temperature modifiers
        set(PlayerHelper.Types.AMBIENT, ambient.get());

        Temperature bodyTemp = tickModifiers(new Temperature(get(PlayerHelper.Types.BODY)), player, getModifiers(PlayerHelper.Types.BODY));

        Temperature base = tickModifiers(new Temperature(), player, getModifiers(PlayerHelper.Types.BASE));

        double maxTemp = config.maxTemp;
        double minTemp = config.minTemp;

        double tempRate = 7.0d;

        if ((ambientTemp > maxTemp && bodyTemp.get() >= 0) ||
                (ambientTemp < minTemp && bodyTemp.get() <= 0))
        {
            boolean isOver = ambientTemp > maxTemp;
            double difference = Math.abs(ambientTemp - (isOver ? maxTemp : minTemp));
            Temperature changeBy = new Temperature(Math.max((difference / tempRate) * config.rate, Math.abs(config.rate / 50)) * (isOver ? 1 : -1));
            set(PlayerHelper.Types.BODY, bodyTemp.add(tickModifiers(changeBy, player, getModifiers(PlayerHelper.Types.RATE))).get());
        }
        else
        {
            // Return the player's body temperature to 0
            Temperature returnRate = new Temperature(getBodyReturnRate(ambientTemp, bodyTemp.get() > 0 ? maxTemp : minTemp, config.rate, bodyTemp.get()));
            set(PlayerHelper.Types.BODY, bodyTemp.add(returnRate).get());
        }

        // Sets the player's base temperature
        set(PlayerHelper.Types.BASE, base.get());

        // Calculate body/base temperatures with modifiers
        Temperature composite = base.add(bodyTemp);

        if (composite.get() != get(PlayerHelper.Types.COMPOSITE) || player.ticksExisted % 3 == 0)
        {
            PlayerHelper.updateTemperature(player,
                    new Temperature(get(PlayerHelper.Types.BODY)),
                    new Temperature(get(PlayerHelper.Types.BASE)),
                    new Temperature(get(PlayerHelper.Types.AMBIENT)));
        }

        // Sets the player's composite temperature to BASE + BODY
        set(PlayerHelper.Types.COMPOSITE, CSMath.clamp(composite.get(), -150, 150));

        //Deal damage to the player if temperature is critical
        boolean hasFireResistance = player.isPotionActive(Effects.FIRE_RESISTANCE) && config.fireRes;
        boolean hasIceResistance = player.isPotionActive(ModEffects.ICE_RESISTANCE) && config.iceRes;
        if (player.ticksExisted % 40 == 0)
        {
            boolean damageScaling = config.damageScaling;

            if (composite.get() >= 100 && !hasFireResistance && !player.isPotionActive(ModEffects.GRACE))
            {
                player.attackEntityFrom(damageScaling ? CSDamageTypes.HOT_SCALED : CSDamageTypes.HOT, 2f);
            }
            if (composite.get() <= -100 && !hasIceResistance && !player.isPotionActive(ModEffects.GRACE))
            {
                player.attackEntityFrom(damageScaling ? CSDamageTypes.COLD_SCALED : CSDamageTypes.COLD, 2f);
            }
        }
    }

    // Used for returning the player's temperature back to 0
    private static double getBodyReturnRate(double ambient, double cap, double rate, double bodyTemp)
    {
        double tempRate = 7.0d;
        double changeBy = Math.max((Math.abs(ambient - cap) / tempRate) * rate, Math.abs(rate / 30));
        return Math.min(Math.abs(bodyTemp), changeBy) * (bodyTemp > 0 ? -1 : 1);
    }

    private static Temperature tickModifiers(Temperature temp, PlayerEntity player, List<TempModifier> modifiers)
    {
        Temperature result = temp.with(modifiers, player);

        modifiers.removeIf(modifier ->
        {
            if (modifier.getExpireTicks() != -1)
            {
                modifier.setTicksExisted(modifier.getTicksExisted() + 1);
                return modifier.getTicksExisted() >= modifier.getExpireTicks();
            }
            return false;
        });

        return result;
    }
}
