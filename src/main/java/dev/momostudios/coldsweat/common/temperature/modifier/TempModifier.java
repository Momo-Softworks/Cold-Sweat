package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.core.event.InitTempModifiers;
import dev.momostudios.coldsweat.core.event.csevents.TempModifierEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import dev.momostudios.coldsweat.common.temperature.Temperature;

import java.util.HashMap;
import java.util.Map;

/**
 * TempModifiers are applied to entities to dynamically change their temperature.<br>
 * For example, biome temperature, time of day, depth, and waterskins are all TempModifiers<br>
 *<br>
 * It is up to you to apply and remove these modifiers manually.<br>
 * To make an instant modifier that does not persist on the player, you can call {@code PlayerTemp.removeModifier()} to remove it in {@code calculate()}.<br>
 *<br>
 * TempModifiers must be REGISTERED using {@link TempModifierEvent.Init}<br>
 * (see {@link InitTempModifiers} for an example)<br>
 */
public abstract class TempModifier
{
    Map<String, Object> args = new HashMap<>();
    int expireTicks = -1;
    int ticksExisted = 0;

    /**
     * Default constructor.<br>
     * REQUIRED
     */
    public TempModifier() {}

    /**
     * Adds a new argument to this TempModifier.<br>
     * @param name is the name of the argument. Used to retrieve the argument in {@link #getArgument(String)}
     * @param arg is value of the argument. It is stored in the {@link PlayerEntity} NBT.
     */
    public void addArgument(String name, Object arg)
    {
        args.put(name, arg);
    }

    /**
     * @param name The name of the argument
     * @return A generic object with the value of the requested argument.
     */
    public Object getArgument(String name)
    {
        return args.get(name);
    }

    /**
     *
     * @param name The name of the argument
     * @param clazz The class of the argument
     * @return The value of the requested argument for this TempModifier instance, cast to the specified class.
     */
    public <T> T getArgument(String name, Class<T> clazz)
    {
        if (!clazz.equals(args.get(name).getClass()))
        {
            throw new IllegalArgumentException(
                    "Argument type mismatch trying to get argument \"" + name + "\" of " + this.getID() +
                    " (expected " + clazz.getName() + " but got " + args.get(name).getClass().getName() + ")");
        }
        return clazz.cast(args.get(name));
    }

    /**
     * Sets the specified argument of the TempModifier instance to the given value.<br>
     * @param name The name of the argument
     * @param arg The new value of the argument
     */
    public void setArgument(String name, Object arg)
    {
        if (arg.getClass().equals(args.get(name).getClass()))
        {
            args.put(name, arg);
        }
        else throw new IllegalArgumentException(
                "Argument type mismatch trying to set argument \"" + name + "\" of " + this.getID() + " to " + arg +
                " (expected " + args.get(name).getClass().getName() + ")");
    }

    public final Map<String, Object> getArguments() {
        return args;
    }

    /**
     * Determines what the provided temperature would be, given the player it is being applied to.<br>
     * This is basically a simple in-out system. It is given a {@link Temperature}, and returns a new Temperature based on the PlayerEntity.<br>
     * <br>
     * Do not call this method directly. Use {@link #calculate(Temperature, PlayerEntity)} instead.<br>
     * <br>
     * @param temp should usually represent the player's body temperature or ambient temperature.<br>
     * @param player the player that is being affected by the modifier.<br>
     * @return the new {@link Temperature}.<br>
     */
    public abstract double getResult(Temperature temp, PlayerEntity player);

    public final double calculate(Temperature temp, PlayerEntity player)
    {
        TempModifierEvent.Tick.Pre pre = new TempModifierEvent.Tick.Pre(this, player, temp);
        MinecraftForge.EVENT_BUS.post(pre);
        if (!pre.isCanceled())
        {
            double value = getResult(pre.getTemperature(), player);

            TempModifierEvent.Tick.Post post = new TempModifierEvent.Tick.Post(this, player, new Temperature(value));
            MinecraftForge.EVENT_BUS.post(post);

            return post.getTemperature().get();
        }
        else return pre.getTemperature().get();
    }

    /**
     * Sets the amount of ticks this TempModifier will last before it is automatically removed.<br>
     * @param ticks the number of ticks this modifier will last.
     * @return this TempModifier instance.
     */
    public TempModifier expires(int ticks)
    {
        expireTicks = ticks;
        return this;
    }
    public int getExpireTicks()
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
     * The ID is used to mark the TempModifier when it is stored in NBT
     * @return the string ID of the TempModifier. You should include your mod's ID to prevent duplicate IDs.<br>
     */
    public abstract String getID();
}
