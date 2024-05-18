package com.momosoftworks.coldsweat.util.exceptions;

public class ArgumentCountException extends IllegalArgumentException
{
    public ArgumentCountException(int received, int expected, String message)
    {
        super(String.format("%s: Expected %s arguments, but only got %s", message, expected, received));
    }
}
