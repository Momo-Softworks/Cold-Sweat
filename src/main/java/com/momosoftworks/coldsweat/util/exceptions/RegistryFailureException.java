package com.momosoftworks.coldsweat.util.exceptions;

public class RegistryFailureException extends RuntimeException
{
    public RegistryFailureException(Object object, String registry, String message, Throwable cause)
    {
        super(String.format("Failed to register object %s for registry %s: %s", object, registry, message), cause);
    }
}
