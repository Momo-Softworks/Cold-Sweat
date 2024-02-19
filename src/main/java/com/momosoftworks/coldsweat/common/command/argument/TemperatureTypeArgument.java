package com.momosoftworks.coldsweat.common.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.EntityTempManager;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemperatureTypeArgument implements ArgumentType<Temperature.Type>
{
    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType(
            (found, constants) -> new TranslationTextComponent("commands.forge.arguments.enum.invalid", constants, found));

    public static TemperatureTypeArgument type()
    {   return new TemperatureTypeArgument();
    }

    public static Temperature.Type getTemperature(CommandContext<CommandSource> context, String argument)
    {   return context.getArgument(argument, Temperature.Type.class);
    }

    @Override
    public Temperature.Type parse(final StringReader reader) throws CommandSyntaxException
    {
        String name = reader.readUnquotedString();
        try
        {   return Temperature.Type.fromID(name);
        }
        catch (IllegalArgumentException e)
        {   throw INVALID_ENUM.createWithContext(reader, name, Arrays.toString(this.getExamples().toArray()));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
    {
        return ISuggestionProvider.suggest(Stream.of(EntityTempManager.VALID_TEMPERATURE_TYPES).map(StringRepresentable::getSerializedName), builder);
    }

    @Override
    public Collection<String> getExamples()
    {
        return Stream.of(EntityTempManager.VALID_TEMPERATURE_TYPES).map(StringRepresentable::getSerializedName).collect(Collectors.toList());
    }

    public static class Serializer implements IArgumentSerializer<TemperatureTypeArgument>
    {
        @Override
        public void serializeToNetwork(TemperatureTypeArgument argument, PacketBuffer buffer)
        {
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public TemperatureTypeArgument deserializeFromNetwork(PacketBuffer buffer)
        {   return new TemperatureTypeArgument();
        }

        @Override
        public void serializeToJson(TemperatureTypeArgument argument, JsonObject json)
        {
        }
    }
}
