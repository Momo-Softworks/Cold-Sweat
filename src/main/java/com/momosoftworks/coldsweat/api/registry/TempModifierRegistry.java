package com.momosoftworks.coldsweat.api.registry;

import com.google.common.collect.ImmutableMap;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.util.exceptions.RegistryFailureException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class TempModifierRegistry
{
    static Map<ResourceLocation, Supplier<TempModifier>> TEMP_MODIFIERS = new HashMap<>();

    public static ImmutableMap<ResourceLocation, Supplier<TempModifier>> getEntries()
    {   return ImmutableMap.copyOf(TEMP_MODIFIERS);
    }

    public static void register(ResourceLocation id, Supplier<TempModifier> supplier)
    {
        if (TEMP_MODIFIERS.containsKey(id))
        {
            throw new RegistryFailureException(id, "TempModifier", String.format("Found duplicate TempModifier entries: %s (%s) %s (%s)", supplier.get().getClass().getName(), id,
                                                             TEMP_MODIFIERS.get(id).getClass().getName(), id), null);
        }
        TEMP_MODIFIERS.put(id, supplier);
    }

    /**
     * Clears the registry of all items. This effectively "un-registers" all TempModifiers.
     */
    public static void flush()
    {
        TEMP_MODIFIERS.clear();
    }

    /**
     * Returns a new instance of the TempModifier with the given ID.<br>
     * If a TempModifier with this ID is not in the registry, this method returns null and logs an error.<br>
     */
    public static Optional<TempModifier> getValue(ResourceLocation id)
    {
        Supplier<TempModifier> mod = TEMP_MODIFIERS.get(id);
        if (mod != null)
        {   return Optional.of(mod.get());
        }
        else
        {   return Optional.empty();
        }
    }

    @Nullable
    public static ResourceLocation getKey(TempModifier modifier)
    {
        for (Map.Entry<ResourceLocation, Supplier<TempModifier>> entry : TEMP_MODIFIERS.entrySet())
        {
            if (entry.getValue().get().getClass() == modifier.getClass())
            {   return entry.getKey();
            }
        }
        return null;
    }
}
