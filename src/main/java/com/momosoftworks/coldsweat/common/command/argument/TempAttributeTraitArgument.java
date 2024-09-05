package com.momosoftworks.coldsweat.common.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public class TempAttributeTraitArgument extends StringRepresentableArgument<Temperature.Trait>
{
    private static final Codec<Temperature.Trait> TEMPERATURES_CODEC = StringRepresentable.fromEnum(() -> EntityTempManager.VALID_ATTRIBUTE_TRAITS);

    private TempAttributeTraitArgument()
    {   super(TEMPERATURES_CODEC, () -> EntityTempManager.VALID_ATTRIBUTE_TRAITS);
    }

    public static TempAttributeTraitArgument attribute()
    {   return new TempAttributeTraitArgument();
    }

    public static Temperature.Trait getAttribute(CommandContext<CommandSourceStack> context, String argument)
    {   return context.getArgument(argument, Temperature.Trait.class);
    }

    protected String convertId(String id)
    {   return id.toLowerCase(Locale.ROOT);
    }

    public static class Info implements ArgumentTypeInfo<TempAttributeTraitArgument, Info.Template>
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
        public Template unpack(TempAttributeTraitArgument argument)
        {   return new Template();
        }

        public final class Template implements ArgumentTypeInfo.Template<TempAttributeTraitArgument>
        {
            @Override
            public TempAttributeTraitArgument instantiate(CommandBuildContext pContext)
            {   return new TempAttributeTraitArgument();
            }

            @Override
            public ArgumentTypeInfo<TempAttributeTraitArgument, ?> type()
            {   return Info.this;
            }
        }
    }
}
