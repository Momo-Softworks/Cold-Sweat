package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.event.common.TempModifierEvent;
import dev.momostudios.coldsweat.api.event.core.TempModifierRegisterEvent;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.core.init.TempModifierInit;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * TempModifiers are applied to entities to dynamically change their temperature.<br>
 * For example, biome temperature, time of day, depth, and waterskins are all TempModifiers<br>
 *<br>
 * It is up to you to apply and remove these modifiers manually.<br>
 * To make an instant modifier that does not persist on the player, you can call {@code PlayerTemp.removeModifier()} to remove it in {@code calculate()}.<br>
 *<br>
 * TempModifiers must be REGISTERED using {@link TempModifierRegisterEvent}<br>
 * (see {@link TempModifierInit} for an example)<br>
 */
public abstract class TempModifier
{
    ConcurrentHashMap<String, Object> args = new ConcurrentHashMap<>();
    int expireTicks = -1;
    int ticksExisted = 0;
    int tickRate = 1;
    Temperature lastInput = new Temperature();
    Temperature lastOutput = new Temperature();
    Function<Temperature, Temperature> function = temp -> temp;


    /**
     * Default constructor.<br>
     */
    public TempModifier() {}

    /**
     * Adds a new argument to this TempModifier.<br>
     * @param name is the name of the argument. Used to retrieve the argument in {@link #getArgument(String)}
     * @param arg is value of the argument. It is stored in the {@link Player}'s NBT.
     */
    public void addArgument(String name, Object arg)
    {
        args.put(name, arg);
    }

    /**
     * @param name The name of the argument
     * @return A generic object with the value of the requested argument.
     */
    public <T> T getArgument(String name)
    {
        return (T) args.get(name);
    }

    /**
     * Sets the specified argument of the TempModifier instance to the given value.<br>
     * @param name The name of the argument
     * @param arg The new value of the argument
     */
    public void setArgument(String name, Object arg)
    {
        try
        {
            args.put(name, arg);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(
                    "Argument type mismatch trying to set argument \"" + name + "\" of " + this.getID() + " to " + arg
                    + " (expected " + args.get(name).getClass().getName() + ")");
        }
    }

    public void clearArgument(String name)
    {
        args.remove(name);
    }

    public final Map<String, Object> getArguments() {
        return args;
    }

    /**
     * Determines what the provided temperature would be, given the player it is being applied to.<br>
     * This is basically a simple in-out system. It is given a {@link Temperature}, and returns a new Temperature based on the PlayerEntity.<br>
     * <br>
     * @param player the player that is being affected by the modifier.<br>
     * @return the new {@link Temperature}.<br>
     */
    protected abstract Function<Temperature, Temperature> calculate(Player player);

    /**
     * Posts this TempModifier's {@link #calculate(Player)} to the Forge event bus.<br>
     * Returns the stored value if this TempModifier has a tickRate set, and it is not the right tick.<br>
     * <br>
     * @param temp the Temperature being fed into the {@link #calculate(Player)} method.
     * @param player the player that is being affected by the modifier.
     */
    public Temperature update(Temperature temp, Player player)
    {
        TempModifierEvent.Calculate.Pre pre = new TempModifierEvent.Calculate.Pre(this, player, temp);
        MinecraftForge.EVENT_BUS.post(pre);

        if (pre.isCanceled()) return temp;

        this.function = this.calculate(player);

        TempModifierEvent.Calculate.Post post = new TempModifierEvent.Calculate.Post(this, player, this.function.apply(pre.getTemperature()));
        MinecraftForge.EVENT_BUS.post(post);

        return post.getTemperature();
    }

    /**
     * @param temp the Temperature to calculate with
     * @return The result of this TempModifier's unique stored function. Stores the input and output.
     */
    public Temperature getResult(Temperature temp)
    {
        lastInput = temp.copy();
        return lastOutput = function.apply(temp).copy();
    }

    /**
     * Sets the number of ticks this TempModifier will exist before it is automatically removed.<br>
     * @param ticks the number of ticks this modifier will last.
     * @return this TempModifier instance (allows for in-line building).
     */
    public TempModifier expires(int ticks)
    {
        expireTicks = ticks;
        return this;
    }
    public int getExpireTime()
    {
        return expireTicks;
    }
    public int getTicksExisted()
    {
        return ticksExisted;
    }
    public int setTicksExisted(int ticks)
    {
        return ticksExisted = ticks;
    }

    /**
     * TempModifiers can be configured to run {@link TempModifier#calculate(Player)} at a specified interval.<br>
     * This is useful if the TempModifier is expensive to calculate, and you want to avoid it being called each tick.<br>
     * <br>
     * Every X ticks, the TempModifier's {@code getResult()} function will be called, then stored internally.<br>
     * Every other time {@code calculate()} is called, the stored value will be returned until X ticks have passed.<br>
     * (new TempModifiers ALWAYS run {@code getResult()} when they are called for the first time).<br>
     * <br>
     * @param ticks the number of ticks between each call to {@code getResult()}.
     * @return this TempModifier instance (allows for in-line building).
     */
    public TempModifier tickRate(int ticks)
    {
        tickRate = Math.max(1, ticks);
        return this;
    }

    public int getTickRate()
    {
        return tickRate;
    }

    /**
     * @return The Temperature this TempModifier was last given
     */
    public Temperature getLastInput()
    {
        return lastInput;
    }

    /**
     * @return The Temperature this TempModifier's function last returned
     */
    public Temperature getLastOutput()
    {
        return lastOutput;
    }

    /**
     * The ID is used to mark the TempModifier when it is stored in NBT
     * @return the String ID of the TempModifier. You should include your mod's ID to prevent duplicate IDs.<br>
     */
    public abstract String getID();
}
