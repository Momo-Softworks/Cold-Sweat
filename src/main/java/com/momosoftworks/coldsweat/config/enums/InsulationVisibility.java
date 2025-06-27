package com.momosoftworks.coldsweat.config.enums;

import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public enum InsulationVisibility implements StringRepresentable
{
    ALWAYS("always"),
    IF_PRESENT("if_present"),
    ON_SHIFT("on_shift"),
    SHIFT_AND_PRESENT("shift_and_present"),
    NEVER("never");

    private final String name;

    InsulationVisibility(String name)
    {   this.name = name;
    }

    @Override
    public String getSerializedName()
    {   return this.name;
    }

    @Nullable
    public static InsulationVisibility byName(String name)
    {
        for (InsulationVisibility visibility : values())
        {   if (visibility.name.equals(name))
            {   return visibility;
            }
        }
        return null;
    }

    public boolean shouldShow(ItemStack item)
    {
        boolean hasInsulation = !ItemInsulationManager.getAllInsulatorsForStack(item).isEmpty();
        return switch (this)
        {
            case ALWAYS -> true;
            case IF_PRESENT -> hasInsulation;
            case ON_SHIFT -> Screen.hasShiftDown();
            case SHIFT_AND_PRESENT -> Screen.hasShiftDown() && hasInsulation;
            case NEVER -> false;
        };
    }

    public boolean showsIfEmpty()
    {   return this == ALWAYS || this == ON_SHIFT;
    }
}
