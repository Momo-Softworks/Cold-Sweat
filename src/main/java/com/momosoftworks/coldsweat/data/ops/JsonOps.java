package com.momosoftworks.coldsweat.data.ops;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class JsonOps extends CodecOps<JsonElement>
{
    public static final JsonOps INSTANCE = new JsonOps();

    @Override
    public Optional<Byte> toByte(JsonElement obj)
    {   return toPrimitive(obj, JsonElement::getAsByte);
    }

    @Override
    public Optional<Integer> toInt(JsonElement obj)
    {   return toPrimitive(obj, JsonElement::getAsInt);
    }

    @Override
    public Optional<Short> toShort(JsonElement obj)
    {   return toPrimitive(obj, JsonElement::getAsShort);
    }

    @Override
    public Optional<Long> toLong(JsonElement obj)
    {   return toPrimitive(obj, JsonElement::getAsLong);
    }

    @Override
    public Optional<Double> toDouble(JsonElement obj)
    {   return toPrimitive(obj, JsonElement::getAsDouble);
    }

    @Override
    public Optional<Number> toNumber(JsonElement obj)
    {   return toPrimitive(obj, JsonElement::getAsNumber);
    }

    @Override
    public Optional<Boolean> toBool(JsonElement obj)
    {   return toPrimitive(obj, JsonElement::getAsBoolean);
    }

    @Override
    public Optional<String> toString(JsonElement obj)
    {   return toPrimitive(obj, JsonElement::getAsString);
    }

    @Override
    public Optional<JsonElement> fromByte(byte b)
    {   return Optional.of(new JsonPrimitive(b));
    }

    @Override
    public Optional<JsonElement> fromInt(int i)
    {   return Optional.of(new JsonPrimitive(i));
    }

    @Override
    public Optional<JsonElement> fromShort(short s)
    {   return Optional.of(new JsonPrimitive(s));
    }

    @Override
    public Optional<JsonElement> fromLong(long l)
    {   return Optional.of(new JsonPrimitive(l));
    }

    @Override
    public Optional<JsonElement> fromDouble(double d)
    {   return Optional.of(new JsonPrimitive(d));
    }

    @Override
    public Optional<JsonElement> fromNumber(Number n)
    {   return Optional.of(new JsonPrimitive(n));
    }

    @Override
    public Optional<JsonElement> fromBool(boolean b)
    {   return Optional.of(new JsonPrimitive(b));
    }

    @Override
    public Optional<JsonElement> fromString(String s)
    {   return Optional.of(new JsonPrimitive(s));
    }

    @Override
    public JsonElement createMap()
    {   return new JsonObject();
    }

    @Override
    public void put(JsonElement map, String key, JsonElement value)
    {
        if (map.isJsonObject())
        {   map.getAsJsonObject().add(key, value);
        }
    }

    @Override
    public JsonElement get(JsonElement map, String key)
    {
        if (map != null && map.isJsonObject())
        {   return map.getAsJsonObject().get(key);
        }
        else return null;
    }

    @Override
    public JsonElement createList()
    {   return new JsonArray();
    }

    @Override
    public void add(JsonElement list, JsonElement value)
    {
        if (list.isJsonArray())
        {   list.getAsJsonArray().add(value);
        }
    }

    @Override
    public List<JsonElement> getList(JsonElement list)
    {
        if (list == null || !list.isJsonArray()) return Collections.emptyList();
        List<JsonElement> result = new ArrayList<JsonElement>();
        for (JsonElement element : list.getAsJsonArray())
        {   result.add(element);
        }
        return result;
    }

    protected static <T> Optional<T> toPrimitive(JsonElement obj, Function<JsonElement, T> extractor)
    {
        if (obj != null && obj.isJsonPrimitive())
        {   return Optional.of(extractor.apply(obj));
        }
        else
        {   return Optional.empty();
        }
    }
}
