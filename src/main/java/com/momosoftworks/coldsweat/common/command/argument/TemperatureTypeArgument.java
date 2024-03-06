package com.momosoftworks.coldsweat.common.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.event.capability.EntityTempManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public class TemperatureTypeArgument extends StringRepresentableArgument<Temperature.Type>
{
    private static final Codec<Temperature.Type> TEMPERATURES_CODEC = StringRepresentable.fromEnum(() -> EntityTempManager.VALID_TEMPERATURE_TYPES);

    private TemperatureTypeArgument()
    {   super(TEMPERATURES_CODEC, () -> EntityTempManager.VALID_TEMPERATURE_TYPES);
    }

    public static TemperatureTypeArgument temperature()
    {   return new TemperatureTypeArgument();
    }

    public static Temperature.Type getTemperature(CommandContext<CommandSourceStack> context, String argument)
    {   return context.getArgument(argument, Temperature.Type.class);
    }

    protected String convertId(String Id)
    {   return Id.toLowerCase(Locale.ROOT);
    }

    public static class Info implements ArgumentTypeInfo<TemperatureTypeArgument, Info.Template>
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
        public Template unpack(TemperatureTypeArgument argument)
        {   return new Template();
        }

        public final class Template implements ArgumentTypeInfo.Template<TemperatureTypeArgument>
        {
            @Override
            public TemperatureTypeArgument instantiate(CommandBuildContext pContext)
            {   return new TemperatureTypeArgument();
            }

            @Override
            public ArgumentTypeInfo<TemperatureTypeArgument, ?> type()
            {   return Info.this;
            }
        }
    }
}
