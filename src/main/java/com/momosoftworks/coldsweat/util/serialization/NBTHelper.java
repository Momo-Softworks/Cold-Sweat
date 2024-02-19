package com.momosoftworks.coldsweat.util.serialization;

import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.nbt.*;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.function.Predicate;

public class NBTHelper
{
    private NBTHelper() {}

    public static CompoundTag modifierToTag(TempModifier modifier)
    {
        // Write the modifier's data to a CompoundTag
        CompoundTag modifierTag = new CompoundTag();
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

    public static Optional<TempModifier> tagToModifier(CompoundTag modifierTag)
    {
        // Create a new modifier from the CompoundTag
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
        CompoundTag tag;
        if (owner instanceof LivingEntity entity)
        {
            tag = entity.getPersistentData();
        }
        else if (owner instanceof ItemStack stack)
        {
            tag = stack.getOrCreateTag();
        }
        else if (owner instanceof BlockEntity blockEntity)
        {
            tag = blockEntity.getTileData();
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
     * Used for storing Temperature values in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of Temperature to be stored. ({@link Temperature.Type#WORLD} should only be stored when needed to prevent lag)
     * @return The NBT tag name for the given type
     */
    public static String getTemperatureTag(Temperature.Type type)
    {
        return switch (type)
        {
            case CORE  -> "Core";
            case WORLD -> "World";
            case BASE  -> "Base";
            case RATE  -> "Rate";
            default -> throw new IllegalArgumentException("PlayerTempHandler.getTempTag(): \"" + type + "\" is not a valid type!");
        };
    }

    public static String getAbilityTag(Temperature.Ability ability)
    {
        return switch (ability)
        {
            case FREEZING_POINT -> "FreezingPoint";
            case BURNING_POINT -> "BurningPoint";
            case COLD_RESISTANCE -> "ColdResistance";
            case HEAT_RESISTANCE -> "HeatResistance";
            case COLD_DAMPENING -> "ColdDampening";
            case HEAT_DAMPENING -> "HeatDampening";
        };
    }
}
