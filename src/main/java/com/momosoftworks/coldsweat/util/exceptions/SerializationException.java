package com.momosoftworks.coldsweat.util.exceptions;

import javax.annotation.Nullable;

public class SerializationException extends RuntimeException
{
    public SerializationException(String message, Throwable cause)
    {   super(message, cause);
    }

    public SerializationException(String message)
    {   super(message);
    }

    public static SerializationException serialize(Object object, String message, @Nullable Throwable cause)
    {
        String overallMessage = String.format("Failed to serialize object %s: %s", object, message);
        return cause == null
               ? new SerializationException(overallMessage)
               : new SerializationException(overallMessage, cause);
    }

    public static SerializationException deserialize(Object object, String message, @Nullable Throwable cause)
    {
        String overallMessage = String.format("Failed to deserialize object %s: %s", object, message);
        return cause == null
               ? new SerializationException(overallMessage)
               : new SerializationException(overallMessage, cause);
    }
}
