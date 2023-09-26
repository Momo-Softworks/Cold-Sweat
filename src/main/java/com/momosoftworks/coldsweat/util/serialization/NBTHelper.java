package com.momosoftworks.coldsweat.util.serialization;

import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import net.minecraft.tileentity.TileEntity;

import java.util.Optional;
import java.util.function.Predicate;

public class NBTHelper
{
    private NBTHelper() {}

    public static CompoundNBT modifierToTag(TempModifier modifier)
    {
        // Write the modifier's data to a CompoundNBT
        CompoundNBT modifierTag = new CompoundNBT();
        modifierTag.putString("Id", modifier.getID());

        // Add the modifier's arguments
        modifierTag.put("ModifierData", modifier.getNBT());

        // Read the modifier's expiration time
        if (modifier.getExpireTime() != -1)
            modifierTag.putInt("ExpireTicks", modifier.getExpireTime());

        // Read the modifier's tick rate
        if (modifier.getTickRate() > 1)
            modifierTag.putInt("TickRate", modifier.getTickRate());

        // Read the modifier's ticks existed
        modifierTag.putInt("TicksExisted", modifier.getTicksExisted());

        return modifierTag;
    }

    public static Optional<TempModifier> tagToModifier(CompoundNBT modifierTag)
    {
        // Create a new modifier from the CompoundNBT
        Optional<TempModifier> optional = TempModifierRegistry.getEntryFor(modifierTag.getString("Id"));
        optional.ifPresent(modifier ->
        {
            modifier.setNBT(modifierTag.getCompound("ModifierData"));

            // Set the modifier's expiration time
            if (modifierTag.contains("ExpireTicks"))
            {   modifier.expires(modifierTag.getInt("ExpireTicks"));
            }

            // Set the modifier's tick rate
            if (modifierTag.contains("TickRate"))
            {   modifier.tickRate(modifierTag.getInt("TickRate"));
            }

            // Set the modifier's ticks existed
            modifier.setTicksExisted(modifierTag.getInt("TicksExisted"));
        });

        return optional;
    }

    public static void incrementTag(Object owner, String key, int amount)
    {
        incrementTag(owner, key, amount, (tag) -> true);
    }

    public static int incrementTag(Object owner, String key, int amount, Predicate<Integer> predicate)
    {
        CompoundNBT tag;
        if (owner instanceof LivingEntity)
        {
            tag = ((LivingEntity) owner).getPersistentData();
        }
        else if (owner instanceof ItemStack)
        {
            tag = ((ItemStack) owner).getOrCreateTag();
        }
        else if (owner instanceof TileEntity)
        {
            tag = ((TileEntity) owner).getTileData();
        }
        else return 0;

        int value = tag.getInt(key);
        if (predicate.test(value))
        {
            tag.putInt(key, value + amount);
        }
        return value + amount;
    }

    /**
     * Used for storing TempModifiers in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of TempModifier to be stored
     * @return The NBT tag name for the given type
     */
    public static String getModifierTag(Temperature.Type type)
    {
        switch (type)
        {
            case CORE  : return "coreTempModifiers";
            case WORLD : return "worldTempModifiers";
            case BASE  : return "baseTempModifiers";
            case RATE  : return "rateTempModifiers";
            case FREEZING_POINT : return "maxTempModifiers";
            case BURNING_POINT  : return "minTempModifiers";
            default : throw new IllegalArgumentException("PlayerTempHandler.getModifierTag(): \"" + type + "\" is not a valid type!");
        }
    }

    /**
     * Used for storing Temperature values in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of Temperature to be stored. ({@link Temperature.Type#WORLD} should only be stored when needed to prevent lag)
     * @return The NBT tag name for the given type
     */
    public static String getTemperatureTag(Temperature.Type type)
    {
        switch (type)
        {
            case CORE  : return "coreTemp";
            case WORLD : return "worldTemp";
            case BASE  : return "baseTemp";
            case FREEZING_POINT : return "maxWorldTemp";
            case BURNING_POINT  : return "minWorldTemp";
            default : throw new IllegalArgumentException("PlayerTempHandler.getTempTag(): \"" + type + "\" is not a valid type!");
        }
    }
}
