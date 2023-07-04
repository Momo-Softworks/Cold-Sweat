package dev.momostudios.coldsweat.util.entity;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.nbt.*;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
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

    @Nullable
    public static TempModifier tagToModifier(CompoundTag modifierTag)
    {
        // Create a new modifier from the CompoundTag
        TempModifier newModifier = TempModifierRegistry.getEntryFor(modifierTag.getString("Id"));
        if (newModifier == null)
        {   ColdSweat.LOGGER.error("Tried to instantiate TempModifier \"" + modifierTag.getString("Id") + "\", but it is not registered!");
            return null;
        }

        newModifier.setNBT(modifierTag.getCompound("ModifierData"));

        // Set the modifier's expiration time
        if (modifierTag.contains("ExpireTicks"))
            newModifier.expires(modifierTag.getInt("ExpireTicks"));

        // Set the modifier's tick rate
        if (modifierTag.contains("TickRate"))
            newModifier.tickRate(modifierTag.getInt("TickRate"));

        // Set the modifier's ticks existed
        newModifier.setTicksExisted(modifierTag.getInt("TicksExisted"));

        return newModifier;
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
     * Used for storing TempModifiers in the player's persistent data (NBT). <br>
     * <br>
     * @param type The type of TempModifier to be stored
     * @return The NBT tag name for the given type
     */
    public static String getModifierTag(Temperature.Type type)
    {
        return switch (type)
        {
            case CORE  -> "coreTempModifiers";
            case WORLD -> "worldTempModifiers";
            case BASE  -> "baseTempModifiers";
            case RATE  -> "rateTempModifiers";
            case FREEZING_POINT -> "maxTempModifiers";
            case BURNING_POINT -> "minTempModifiers";
            default -> throw new IllegalArgumentException("PlayerTempHandler.getModifierTag(): \"" + type + "\" is not a valid type!");
        };
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
            case CORE  -> "coreTemp";
            case WORLD -> "worldTemp";
            case BASE  -> "baseTemp";
            case FREEZING_POINT -> "maxWorldTemp";
            case BURNING_POINT -> "minWorldTemp";
            default -> throw new IllegalArgumentException("PlayerTempHandler.getTempTag(): \"" + type + "\" is not a valid type!");
        };
    }
}
