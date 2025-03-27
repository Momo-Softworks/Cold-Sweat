package com.momosoftworks.coldsweat.common.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NicerEnumArgument<T extends Enum<T>> implements ArgumentType<T>
{
    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType(
            (found, constants) -> new TranslationTextComponent("commands.forge.arguments.enum.invalid", constants, found));
    private final Class<T> enumClass;

    public static <R extends Enum<R>> NicerEnumArgument<R> enumArgument(Class<R> enumClass)
    {   return new NicerEnumArgument<>(enumClass);
    }

    public NicerEnumArgument(final Class<T> enumClass)
    {   this.enumClass = enumClass;
    }

    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException
    {
        String name = reader.readUnquotedString();
        try
        {   return Enum.valueOf(enumClass, name.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {   throw INVALID_ENUM.createWithContext(reader, name, Arrays.toString(Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray()));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
    {   return ISuggestionProvider.suggest(Stream.of(enumClass.getEnumConstants()).map(en -> en.name().toLowerCase(Locale.ROOT)), builder);
    }

    @Override
    public Collection<String> getExamples()
    {   return Stream.of(enumClass.getEnumConstants()).map(en -> en.name().toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    }

    public static class Serializer<T extends Enum<T>> implements IArgumentSerializer<NicerEnumArgument<T>>
    {
        @Override
        public void serializeToNetwork(NicerEnumArgument template, PacketBuffer buffer)
        {   buffer.writeUtf(template.enumClass.getName());
        }

        @Override
        public NicerEnumArgument<T> deserializeFromNetwork(PacketBuffer buffer)
        {
            try
            {   String name = buffer.readUtf();
                return new NicerEnumArgument<>((Class<T>) Class.forName(name));
            }
            catch (ClassNotFoundException e)
            {   return null;
            }
        }

        @Override
        public void serializeToJson(NicerEnumArgument argument, JsonObject json)
        {   json.addProperty("enum", argument.enumClass.getName());
        }
    }
}
