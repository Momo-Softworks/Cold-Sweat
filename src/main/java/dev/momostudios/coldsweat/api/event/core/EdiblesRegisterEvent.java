package dev.momostudios.coldsweat.api.event.core;

import dev.momostudios.coldsweat.common.entity.data.edible.ChameleonEdibles;
import dev.momostudios.coldsweat.common.entity.data.edible.Edible;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.Optional;

public class EdiblesRegisterEvent extends Event
{
    public final void registerEdible(Edible edible)
    {
        ChameleonEdibles.addEdible(edible);
    }
}
