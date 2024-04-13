package com.momosoftworks.coldsweat.common.command.argument;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.event.capability.EntityTempManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AbilityOrTempTypeArgument implements ArgumentType<Temperature.Trait>
{
    private static final Codec<Temperature.Trait> TEMPERATURES_CODEC = Temperature.Trait.CODEC;

    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((p_234071_) -> {
        return Component.translatable("argument.enum.invalid", p_234071_);
    });

    private final Codec<Temperature.Trait> codec;
    private final Supplier<Temperature.Trait[]> values;

    private AbilityOrTempTypeArgument()
    {   this.codec = TEMPERATURES_CODEC;
        this.values = () -> (Temperature.Trait[]) EntityTempManager.VALID_ATTRIBUTE_TYPES;
    }

    public static AbilityOrTempTypeArgument attribute()
    {   return new AbilityOrTempTypeArgument();
    }

    public static Temperature.Trait getAttribute(CommandContext<CommandSourceStack> context, String argument)
    {   return context.getArgument(argument, Temperature.Trait.class);
    }


    public Temperature.Trait parse(StringReader stringReader) throws CommandSyntaxException
    {   String s = stringReader.readUnquotedString();
        return this.codec.parse(JsonOps.INSTANCE, new JsonPrimitive(s)).result().orElseThrow(() ->
        {   return ERROR_INVALID_VALUE.create(s);
        });
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder)
    {
        return SharedSuggestionProvider.suggest(Arrays.stream(this.values.get()).map(Temperature.Trait::getSerializedName).toList(), pBuilder);
    }

    public Collection<String> getExamples()
    {
        return Arrays.stream(this.values.get()).map((either) ->
        {   return ((StringRepresentable)either).getSerializedName();
        }).map(this::convertId).limit(2L).collect(Collectors.toList());
    }

    protected String convertId(String p_275436_) {
        return p_275436_;
    }

    public static class Info implements ArgumentTypeInfo<AbilityOrTempTypeArgument, Info.Template>
    {
        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf buffer)
        {
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf buffer)
        {   return new Template();
        }

        @Override
        public void serializeToJson(Template template, JsonObject json)
        {
        }

        @Override
        public Template unpack(AbilityOrTempTypeArgument argument)
        {   return new Template();
        }

        public final class Template implements ArgumentTypeInfo.Template<AbilityOrTempTypeArgument>
        {
            @Override
            public AbilityOrTempTypeArgument instantiate(CommandBuildContext pContext)
            {   return new AbilityOrTempTypeArgument();
            }

            @Override
            public ArgumentTypeInfo<AbilityOrTempTypeArgument, ?> type()
            {   return Info.this;
            }
        }
    }
}
