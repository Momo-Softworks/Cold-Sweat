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
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class TempAttributeTraitArgument implements ArgumentType<Temperature.Trait>
{
    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType(
            (found, constants) -> new TranslatableComponent("commands.forge.arguments.enum.invalid", constants, found));

    public static TempAttributeTraitArgument attribute()
    {   return new TempAttributeTraitArgument();
    }

    public static Temperature.Trait getAttribute(CommandContext<CommandSourceStack> context, String argument)
    {   return context.getArgument(argument, Temperature.Trait.class);
    }

    @Override
    public Temperature.Trait parse(final StringReader reader) throws CommandSyntaxException
    {
        String name = reader.readUnquotedString();
        try
        {   return Temperature.Trait.fromID(name);
        }
        catch (IllegalArgumentException e)
        {   throw INVALID_ENUM.createWithContext(reader, name, Arrays.toString(this.getExamples().toArray()));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
    {
        return SharedSuggestionProvider.suggest(this.getExamples(), builder);
    }

    @Override
    public Collection<String> getExamples()
    {
        return Stream.of(EntityTempManager.VALID_ATTRIBUTE_TYPES).map(Temperature.Trait::getSerializedName).toList();
    }

    public static class Serializer implements ArgumentSerializer<TempAttributeTraitArgument>
    {
        @Override
        public void serializeToNetwork(TempAttributeTraitArgument argument, FriendlyByteBuf buffer)
        {
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public TempAttributeTraitArgument deserializeFromNetwork(FriendlyByteBuf buffer)
        {   return new TempAttributeTraitArgument();
        }

        @Override
        public void serializeToJson(TempAttributeTraitArgument argument, JsonObject json)
        {
        }
    }
}
