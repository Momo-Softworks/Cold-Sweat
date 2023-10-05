package com.momosoftworks.coldsweat.util.serialization;

import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.tileentity.ITileEntityDataHolder;
import com.momosoftworks.coldsweat.util.world.ItemHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Optional;
import java.util.function.Predicate;

public class NBTHelper
{
    private NBTHelper() {}

    public static NBTTagCompound modifierToNBT(TempModifier modifier)
    {
        // Write the modifier's data to a NBTTagCompound
        NBTTagCompound modifierTag = new NBTTagCompound();
        modifierTag.setString("Id", modifier.getID());

        // Add the modifier's arguments
        modifierTag.setTag("ModifierData", modifier.getNBT());

        // Read the modifier's expiration time
        if (modifier.getExpireTime() != -1)
            modifierTag.setInteger("ExpireTicks", modifier.getExpireTime());

        // Read the modifier's tick rate
        if (modifier.getTickRate() > 1)
            modifierTag.setInteger("TickRate", modifier.getTickRate());

        // Read the modifier's ticks existed
        modifierTag.setInteger("TicksExisted", modifier.getTicksExisted());

        return modifierTag;
    }

    public static Optional<TempModifier> NBTToModifier(NBTTagCompound modifierTag)
    {
        // Create a new modifier from the NBTTagCompound
        Optional<TempModifier> optional = TempModifierRegistry.getEntryFor(modifierTag.getString("Id"));
        optional.ifPresent(modifier ->
        {
            modifier.setNBT(modifierTag.getCompoundTag("ModifierData"));

            // Set the modifier's expiration time
            if (modifierTag.hasKey("ExpireTicks"))
            {   modifier.expires(modifierTag.getInteger("ExpireTicks"));
            }

            // Set the modifier's tick rate
            if (modifierTag.hasKey("TickRate"))
            {   modifier.tickRate(modifierTag.getInteger("TickRate"));
            }

            // Set the modifier's ticks existed
            modifier.setTicksExisted(modifierTag.getInteger("TicksExisted"));
        });

        return optional;
    }

    public static void incrementTag(Object owner, String key, int amount)
    {
        incrementTag(owner, key, amount, (tag) -> true);
    }

    public static int incrementTag(Object owner, String key, int amount, Predicate<Integer> predicate)
    {
        NBTTagCompound tag;
        if (owner instanceof EntityLivingBase)
        {   tag = ((EntityLivingBase) owner).getEntityData();
        }
        else if (owner instanceof ItemStack)
        {   tag = ItemHelper.getOrCrateTag(((ItemStack) owner));
        }
        else if (owner instanceof ITileEntityDataHolder)
        {   tag = ((ITileEntityDataHolder) owner).getTileData();
        }
        else return 0;

        int value = tag.getInteger(key);
        if (predicate.test(value))
        {
            tag.setInteger(key, value + amount);
        }
        return value + amount;
    }

    /**
     * Used for storing TempModifiers in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of TempModifier to be stored
     * @return The NBT tag name for the given type
     */
    public static String getModifierNBTKey(Temperature.Type type)
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
    public static String getTemperatureNBTKey(Temperature.Type type)
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
