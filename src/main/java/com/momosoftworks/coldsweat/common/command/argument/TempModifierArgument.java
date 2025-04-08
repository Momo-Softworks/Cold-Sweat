package com.momosoftworks.coldsweat.common.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class TempModifierArgument implements ArgumentType<ResourceLocation>
{
    public static TempModifierArgument modifier()
    {   return new TempModifierArgument();
    }

    public static ResourceLocation getModifier(CommandContext<CommandSourceStack> context, String argument)
    {   return context.getArgument(argument, ResourceLocation.class);
    }

    @Override
    public ResourceLocation parse(StringReader stringReader) throws CommandSyntaxException
    {   ResourceLocation location = ResourceLocation.read(stringReader);
        if (location.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
        {   location = new ResourceLocation(ColdSweat.MOD_ID, location.getPath());
        }
        return location;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        Stream<String> ids = TempModifierRegistry.getEntries().keySet().stream().map(ResourceLocation::toString);
        String input = builder.getRemaining().toLowerCase(Locale.ROOT);
        ids.filter((id) -> id.toLowerCase(Locale.ROOT).contains(input))
           .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples()
    {
        return ArgumentType.super.getExamples();
    }

    public static class Info implements ArgumentTypeInfo<TempModifierArgument, Info.Template>
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
        public Template unpack(TempModifierArgument argument)
        {   return new Template();
        }

        public final class Template implements ArgumentTypeInfo.Template<TempModifierArgument>
        {
            @Override
            public TempModifierArgument instantiate(CommandBuildContext pContext)
            {   return new TempModifierArgument();
            }

            @Override
            public ArgumentTypeInfo<TempModifierArgument, ?> type()
            {   return Info.this;
            }
        }
    }
}
