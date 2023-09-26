package com.momosoftworks.coldsweat.util.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class JsonHelper
{
    public static int getAsInt(JsonObject json, String key, int fallback)
    {   return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    public static int getAsInt(JsonObject json, String key)
    {   return json.get(key).getAsInt();
    }

    public static float getAsFloat(JsonObject json, String key, float fallback)
    {   return json.has(key) ? json.get(key).getAsFloat() : fallback;
    }

    public static float getAsFloat(JsonObject json, String key)
    {   return json.get(key).getAsFloat();
    }

    public static double getAsDouble(JsonObject json, String key, double fallback)
    {   return json.has(key) ? json.get(key).getAsDouble() : fallback;
    }

    public static double getAsDouble(JsonObject json, String key)
    {   return json.get(key).getAsDouble();
    }

    public static boolean getAsBoolean(JsonObject json, String key, boolean fallback)
    {   return json.has(key) ? json.get(key).getAsBoolean() : fallback;
    }

    public static boolean getAsBoolean(JsonObject json, String key)
    {   return json.get(key).getAsBoolean();
    }

    public static String getAsString(JsonObject json, String key, String fallback)
    {   return json.has(key) ? json.get(key).getAsString() : fallback;
    }

    public static String getAsString(JsonObject json, String key)
    {   return json.get(key).getAsString();
    }

    public static JsonObject getAsJsonObject(JsonObject json, String key, JsonObject fallback)
    {   return json.has(key) ? json.get(key).getAsJsonObject() : fallback;
    }

    public static JsonObject getAsJsonObject(JsonObject json, String key)
    {   return json.get(key).getAsJsonObject();
    }

    public static JsonArray getAsJsonArray(JsonObject json, String key, JsonArray fallback)
    {   return json.has(key) ? json.get(key).getAsJsonArray() : fallback;
    }

    public static JsonArray getAsJsonArray(JsonObject json, String key)
    {   return json.get(key).getAsJsonArray();
    }
}
