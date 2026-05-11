package com.momosoftworks.coldsweat.client.gui.tooltip.util;

public enum RequirementCheck
{
    PASSED,
    FAILED,
    UNKNOWN;

    public boolean unknown()
    {
        return this == UNKNOWN;
    }

    public boolean passed()
    {
        return this == PASSED;
    }

    public boolean failed()
    {
        return this == FAILED;
    }
}
