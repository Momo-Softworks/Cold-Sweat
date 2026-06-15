package com.momosoftworks.coldsweat.util.serialization;

/**
 * Minimal 1.7 stand-in for Mojang's {@code StringRepresentable} interface.<br>
 * Used by enums that need a stable, serializable string identity.
 */
public interface StringRepresentable
{
    String getSerializedName();
}
