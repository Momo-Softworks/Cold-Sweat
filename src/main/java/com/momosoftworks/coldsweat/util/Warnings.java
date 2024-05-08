package com.momosoftworks.coldsweat.util;

public class Warnings
{
    public static String dupConfigKeys(Class<?> keyClass, String configName, String keyName)
    {
        return String.format("Duplicate %s found in %s: %s", keyClass, configName, keyName);
    }
}
