package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.event.common.temperautre.TempModifierEvent;
import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.core.init.TempModifierInit;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;

import static com.momosoftworks.coldsweat.api.util.Temperature.Trait;

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
    private final Double[] lastInput = new Double[Trait.values().length];
    private final Double[] lastOutput = new Double[Trait.values().length];
    private final Function<Double, Double>[] function = new Function[Trait.values().length];
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
            this.setFunction(trait, pre.getFunction());
            return this.apply(trait, pre.getTemperature());
        }

        TempModifierEvent.Calculate.Post post = new TempModifierEvent.Calculate.Post(this, entity, pre.getTemperature(), this.calculate(entity, trait), trait);
        NeoForge.EVENT_BUS.post(post);

        this.setFunction(trait, post.getFunction());

        return this.apply(trait, post.getTemperature());
    }

    /**
     * @param temp the Temperature to calculate with
     * @return The result of this TempModifier's unique stored function. Stores the input and output.
     */
    public double apply(Trait trait, double temp)
    {
        this.setLastInput(trait, temp);
        double output = this.getFunction(trait).apply(temp);
        this.setLastOutput(trait, output);
        return output;
    }

    /**
     * Called when this TempModifier is added to the entity.<br>
     */
    public void onAdded(LivingEntity entity, Trait trait) {}

    /**
     * Called when this TempModifier is removed from the entity.<br>
     */
    public void onRemoved(LivingEntity entity, Trait trait) {}

    /**
     * Called when a TempModifier is added to the same trait as this one.<br>
     */
    public void onSiblingAdded(LivingEntity entity, Trait trait, TempModifier sibling) {}

    /**
     * Called when a TempModifier is removed from the same trait as this one.<br>
     */
    public void onSiblingRemoved(LivingEntity entity, Trait trait, TempModifier sibling) {}

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

    /**
     * @return The current function that has been calculated for the given trait via {@link #calculate(LivingEntity, Trait)}.<br>
     * Returns a default (no-op) function if one hasn't been calculated for the given trait.
     */
    public final Function<Double, Double> getFunction(Trait trait)
    {
        Function<Double, Double> func = function[trait.ordinal()];
        if (func == null)
        {   this.setFunction(trait, func = temp -> temp);
        }
        return func;
    }

    protected final void setFunction(Trait trait, Function<Double, Double> func)
    {   function[trait.ordinal()] = func;
    }

    /**
     * @return The Temperature this TempModifier was last given
     */
    public final double getLastInput(Trait trait)
    {   return CSMath.orElse(lastInput[trait.ordinal()], 0.0);
    }

    protected final void setLastInput(Trait trait, double temp)
    {   lastInput[trait.ordinal()] = temp;
    }

    /**
     * @return The Temperature this TempModifier's function last returned
     */
    public final double getLastOutput(Trait trait)
    {   return CSMath.orElse(lastOutput[trait.ordinal()], 0.0);
    }

    protected final void setLastOutput(Trait trait, double temp)
    {   lastOutput[trait.ordinal()] = temp;
    }

    public final CompoundTag getNBT()
    {   return nbt;
    }

    public void setNBT(CompoundTag data)
    {   this.nbt = data;
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

    public ResourceLocation getID()
    {   return TempModifierRegistry.getKey(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof TempModifier mod
            && this.getClass().equals(mod.getClass())
            && mod.getNBT().equals(this.getNBT());
    }

    @Override
    public String toString()
    {   return this.getID().toString();
    }
}
