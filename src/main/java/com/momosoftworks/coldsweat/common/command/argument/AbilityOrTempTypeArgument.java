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
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.EitherCodec;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.EntityTempManager;
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

public class AbilityOrTempTypeArgument implements ArgumentType<Either<Temperature.Type, Temperature.Ability>>
{
    private static final Codec<Either<Temperature.Type, Temperature.Ability>> TEMPERATURES_CODEC = new EitherCodec<>(
    StringRepresentable.fromEnum(() -> new Temperature.Type[] {Temperature.Type.WORLD, Temperature.Type.BASE}),
    StringRepresentable.fromEnum(() -> Temperature.Ability.values()));

    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((p_234071_) -> {
        return Component.translatable("argument.enum.invalid", p_234071_);
    });

    private final Codec<Either<Temperature.Type, Temperature.Ability>> codec;
    private final Supplier<Either<Temperature.Type, Temperature.Ability>[]> values;

    private AbilityOrTempTypeArgument()
    {   this.codec = TEMPERATURES_CODEC;
        this.values = () -> (Either<Temperature.Type, Temperature.Ability>[]) EntityTempManager.VALID_ATTRIBUTES;
    }

    public static AbilityOrTempTypeArgument type()
    {   return new AbilityOrTempTypeArgument();
    }

    public static Either<Temperature.Type, Temperature.Ability> getAttribute(CommandContext<CommandSourceStack> context, String argument)
    {   return context.getArgument(argument, Either.class);
    }


    public Either<Temperature.Type, Temperature.Ability> parse(StringReader stringReader) throws CommandSyntaxException
    {   String s = stringReader.readUnquotedString();
        return this.codec.parse(JsonOps.INSTANCE, new JsonPrimitive(s)).result().orElseThrow(() ->
        {   return ERROR_INVALID_VALUE.create(s);
        });
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder)
    {
        return SharedSuggestionProvider.suggest(Arrays.stream(this.values.get()).map((either) ->
        {   return either.left().map(StringRepresentable::getSerializedName)
                   .orElse(either.right().map(StringRepresentable::getSerializedName)
                   .orElse(""));
        }).toList(), pBuilder);
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
