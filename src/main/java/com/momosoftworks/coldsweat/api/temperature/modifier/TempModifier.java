package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.event.common.temperautre.TempModifierEvent;
import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.core.init.TempModifierInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;

import static com.momosoftworks.coldsweat.api.util.Temperature.Trait;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * TempModifiers are applied to entities to dynamically change their temperature.<br>
 * For example, biome temperature, time of day, depth, and waterskins are all TempModifiers<br>
 *<br>
 * It is up to you to apply and remove these modifiers manually.<br>
 *<br>
 * TempModifiers must be REGISTERED using {@link TempModifierRegisterEvent}<br>
 * (see {@link TempModifierInit} for an example)<br>
 */
public abstract class TempModifier
{
    private CompoundTag nbt = new CompoundTag();
    private int expireTicks = -1;
    private int ticksExisted = 0;
    private int tickRate = 1;
    private final Map<Trait, Double> lastInput = new EnumMap<>(Trait.class);
    private final Map<Trait, Double> lastOutput = new EnumMap<>(Trait.class);
    private final Map<Trait, Function<Double, Double>> function = new EnumMap<>(Trait.class);
    private boolean changed = false;

    /**
     * Default constructor (REQUIRED for proper registration).<br>
     */
    public TempModifier() {}

    /**
     * TempModifiers can be configured to run {@link TempModifier#calculate(LivingEntity, Temperature.Trait)} at a specified interval.<br>
     * This is useful if the TempModifier is expensive to calculate, and you want to avoid it being called each tick.<br>
     * <br>
     * Every X ticks, the TempModifier's {@code calculate()} function will be called, then stored internally.<br>
     * Every other time {@code update()} is called, the stored value will be returned until X ticks have passed.<br>
     * (new TempModifiers always run {@code calculate()} when they are called for the first time).<br>
     * @param interval the number of ticks between each call to {@code getResult()}.
     * @return this TempModifier instance (allows for in-line building).
     */
    public final <T extends TempModifier> T tickRate(int interval)
    {   tickRate = Math.max(1, interval);
        return (T) this;
    }

    /**
     * Sets the number of ticks this TempModifier will exist before it is automatically removed.
     * @param ticks the number of ticks this modifier will last.
     * @return this TempModifier instance (allows for in-line building).
     */
    public final <T extends TempModifier> T expires(int ticks)
    {
        expireTicks = ticks;
        return (T) this;
    }

    /**
     * Determines what the provided temperature would be, given the player it is being applied to.<br>
     * This is basically a simple in-out system. It is given a temperature, and returns a new temperature based on the PlayerEntity.<br>
     * @param entity the entity that is being affected by the modifier.
     * @return the new temperature.
     */
    protected abstract Function<Double, Double> calculate(LivingEntity entity, Trait trait);

    /**
     * Called every tick on the temperature modifier.<br>
     * Use this to handle calculations that aren't trait-specific.
     */
    public void tick(LivingEntity entity) {}

    /**
     * Posts this TempModifier's {@link #calculate(LivingEntity, Trait)} to the Forge event bus.<br>
     * Returns the stored value if this TempModifier has a tickRate set, and it is not the right tick.<br>
     * <br>
     * @param temp the Temperature being fed into the {@link #calculate(LivingEntity, Trait)} method.
     * @param entity the entity that is being affected by the modifier.
     */
    public final double update(double temp, LivingEntity entity, Trait trait)
    {
        TempModifierEvent.Calculate.Pre pre = new TempModifierEvent.Calculate.Pre(this, entity, temp, trait);
        NeoForge.EVENT_BUS.post(pre);
        if (pre.isCanceled())
        {
            this.function.put(trait, pre.getFunction());
            return this.apply(trait, pre.getTemperature());
        }

        TempModifierEvent.Calculate.Post post = new TempModifierEvent.Calculate.Post(this, entity, pre.getTemperature(), this.calculate(entity, trait), trait);
        NeoForge.EVENT_BUS.post(post);

        this.function.put(trait, post.getFunction());

        return this.apply(trait, post.getTemperature());
    }

    /**
     * @param temp the Temperature to calculate with
     * @return The result of this TempModifier's unique stored function. Stores the input and output.
     */
    public double apply(Trait trait, double temp)
    {
        lastInput.put(trait, temp);
        double output = this.getFunction(trait).apply(temp);
        lastOutput.put(trait, output);
        return output;
    }

    public final int getExpireTime()
    {   return expireTicks;
    }

    public final int getTicksExisted()
    {   return ticksExisted;
    }

    public final int setTicksExisted(int ticks)
    {   return ticksExisted = ticks;
    }

    public final int getTickRate()
    {   return tickRate;
    }

    public final Function<Double, Double> getFunction(Trait trait)
    {   return function.computeIfAbsent(trait, t -> (temp -> temp));
    }

    /**
     * @return The Temperature this TempModifier was last given
     */
    public final double getLastInput(Trait trait)
    {   return lastInput.getOrDefault(trait, 0.0);
    }

    /**
     * @return The Temperature this TempModifier's function last returned
     */
    public final double getLastOutput(Trait trait)
    {   return lastOutput.getOrDefault(trait, 0.0);
    }

    public final CompoundTag getNBT()
    {
        return nbt;
    }

    public void setNBT(CompoundTag data)
    {
        this.nbt = data;
    }

    @Override
    public String toString()
    {   return TempModifierRegistry.getKey(this).toString();
    }

    public void markDirty()
    {   this.changed = true;
    }

    public boolean isDirty()
    {   return this.changed;
    }

    public void markClean()
    {   this.changed = false;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof TempModifier mod
            && this.getClass().equals(mod.getClass())
            && mod.getNBT().equals(this.getNBT());
    }
}
