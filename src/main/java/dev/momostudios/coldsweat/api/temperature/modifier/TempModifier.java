package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.event.core.TempModifierRegisterEvent;
import dev.momostudios.coldsweat.core.event.InitTempModifiers;
import dev.momostudios.coldsweat.api.event.common.TempModifierEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import dev.momostudios.coldsweat.api.temperature.Temperature;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TempModifiers are applied to entities to dynamically change their temperature.<br>
 * For example, biome temperature, time of day, depth, and waterskins are all TempModifiers<br>
 *<br>
 * It is up to you to apply and remove these modifiers manually.<br>
 * To make an instant modifier that does not persist on the player, you can call {@code PlayerTemp.removeModifier()} to remove it in {@code calculate()}.<br>
 *<br>
 * TempModifiers must be REGISTERED using {@link TempModifierRegisterEvent}<br>
 * (see {@link InitTempModifiers} for an example)<br>
 */
public abstract class TempModifier
{
    ConcurrentHashMap<String, Object> args = new ConcurrentHashMap<>();
    int expireTicks = -1;
    int ticksExisted = 0;
    int tickRate = 1;
    double storedValue = 0;
    boolean isUnset = true;

    /**
     * Default constructor.<br>
     * REQUIRED
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
     * Do not call this method directly unless you intentionally do not wish to post to the event bus.<br>
     * Instead, use {@link #calculate(Temperature, Player)}.<br>
     * <br>
     * @param temp should usually represent the player's body temperature or world temperature.<br>
     * @param player the player that is being affected by the modifier.<br>
     * @return the new {@link Temperature}.<br>
     */
    public abstract Temperature getResult(Temperature temp, Player player);

    /**
     * Posts this TempModifier's {@link #getResult(Temperature, Player)} to the Forge event bus.<br>
     * Returns the stored value if this TempModifier has a tickRate set, and it is not the right tick.<br>
     * <br>
     * @param temp the Temperature being fed into the {@link #getResult(Temperature, Player)} method.
     * @param player the player that is being affected by the modifier.
     * @return the new {@link Temperature}.
     */
    public Temperature calculate(Temperature temp, Player player)
    {
        TempModifierEvent.Tick.Pre pre = new TempModifierEvent.Tick.Pre(this, player, temp);
        MinecraftForge.EVENT_BUS.post(pre);

        if (pre.isCanceled()) return temp;

        double value;
        if (player.tickCount % tickRate == 0 || isUnset)
        {
            storedValue = value = getResult(pre.getTemperature(), player).get();
            isUnset = false;
        }
        else
        {
            value = storedValue;
        }

        TempModifierEvent.Tick.Post post = new TempModifierEvent.Tick.Post(this, player, new Temperature(value));
        MinecraftForge.EVENT_BUS.post(post);

        return post.getTemperature();
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
    public void setTicksExisted(int ticks)
    {
        ticksExisted = ticks;
    }

    /**
     * TempModifiers can be configured to run {@link TempModifier#getResult(Temperature, Player)} at a specified interval.<br>
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
     * The ID is used to mark the TempModifier when it is stored in NBT
     * @return the String ID of the TempModifier. You should include your mod's ID to prevent duplicate IDs.<br>
     */
    public abstract String getID();
}
