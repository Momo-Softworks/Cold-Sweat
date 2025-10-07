package com.momosoftworks.coldsweat.config.enums;

import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import net.minecraft.client.gui.screen.Screen;

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

    public static InsulationVisibility byName(String name)
    {   return EnumHelper.byName(values(), name);
    }

    public boolean canShow()
    {
        switch (this)
        {
            case ALWAYS : return true;
            case IF_PRESENT : return true;
            case ON_SHIFT : return Screen.hasShiftDown();
            case SHIFT_AND_PRESENT : return Screen.hasShiftDown();
            case NEVER : return false;
        }
        return false;
    }

    public boolean showsIfEmpty()
    {   return this == ALWAYS || this == ON_SHIFT;
    }
}
