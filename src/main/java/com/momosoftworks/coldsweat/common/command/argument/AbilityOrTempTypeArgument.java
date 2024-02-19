package com.momosoftworks.coldsweat.common.command.argument;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.EitherCodec;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.EntityTempManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbilityOrTempTypeArgument implements ArgumentType<Either<Temperature.Type, Temperature.Ability>>
{
    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType(
            (found, constants) -> new TranslatableComponent("commands.forge.arguments.enum.invalid", constants, found));

    public static AbilityOrTempTypeArgument modifier()
    {   return new AbilityOrTempTypeArgument();
    }

    public static Temperature.Type getModifier(CommandContext<CommandSourceStack> context, String argument)
    {   return context.getArgument(argument, Temperature.Type.class);
    }

    @Override
    public Either<Temperature.Type, Temperature.Ability> parse(final StringReader reader) throws CommandSyntaxException
    {
        String name = reader.readUnquotedString();
        try
        {   return CSMath.orElse(Temperature.Type.fromID(name), Temperature.Ability.fromID(name));
        }
        catch (IllegalArgumentException e)
        {   throw INVALID_ENUM.createWithContext(reader, name, Arrays.toString(this.getExamples().toArray()));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
    {
        return SharedSuggestionProvider.suggest(Stream.of(EntityTempManager.VALID_MODIFIER_TYPES).map(StringRepresentable::getSerializedName), builder);
    }

    @Override
    public Collection<String> getExamples()
    {
        return Stream.of(EntityTempManager.VALID_MODIFIER_TYPES).map(StringRepresentable::getSerializedName).collect(Collectors.toList());
    }

    public static class Serializer implements ArgumentSerializer<AbilityOrTempTypeArgument>
    {
        @Override
        public void serializeToNetwork(AbilityOrTempTypeArgument argument, FriendlyByteBuf buffer)
        {
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public AbilityOrTempTypeArgument deserializeFromNetwork(FriendlyByteBuf buffer)
        {   return new AbilityOrTempTypeArgument();
        }

        @Override
        public void serializeToJson(AbilityOrTempTypeArgument argument, JsonObject json)
        {
        }
    }
}
