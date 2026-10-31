package com.momosoftworks.coldsweat.compat.kubejs.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.compat.kubejs.util.TempModifierDataJS;
import com.momosoftworks.coldsweat.compat.kubejs.util.TempModifierJS;
import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class TempModifierRegisterEventJS implements KubeEvent
{
    private final TempModifierRegisterEvent event;

    public TempModifierRegisterEventJS(TempModifierRegisterEvent event)
    {   this.event = event;
    }

    public void addTempModifier(String id, Function<TempModifierDataJS, Function<Double, Double>> constructor)
    {
        ResourceLocation key = ResourceLocation.parse(id);
        if (key.getNamespace().equals("minecraft"))
        {   ColdSweat.LOGGER.error("KubeJS: Non-Minecraft namespace required for TempModifier IDs (i.e. mymod:my_modifier)");
            return;
        }
        this.event.register(key, () -> new TempModifierJS(constructor));
    }
}
