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

public class TempModifierTypeArgument extends StringRepresentableArgument<Temperature.Type>
{
    private static final Codec<Temperature.Type> MODIFIERS_CODEC = StringRepresentable.fromEnumWithMapping(() -> EntityTempManager.VALID_MODIFIER_TYPES, (p_275334_) ->
    {   return p_275334_.toLowerCase(Locale.ROOT);
    });

    private TempModifierTypeArgument()
    {   super(MODIFIERS_CODEC, () -> EntityTempManager.VALID_MODIFIER_TYPES);
    }

    public static TempModifierTypeArgument temperature()
    {   return new TempModifierTypeArgument();
    }

    public static Temperature.Type getTemperature(CommandContext<CommandSourceStack> context, String argument)
    {   return context.getArgument(argument, Temperature.Type.class);
    }

    protected String convertId(String Id)
    {   return Id.toLowerCase(Locale.ROOT);
    }

    public static class Info implements ArgumentTypeInfo<TempModifierTypeArgument, Info.Template>
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
        public Template unpack(TempModifierTypeArgument argument)
        {   return new Template();
        }

        public final class Template implements ArgumentTypeInfo.Template<TempModifierTypeArgument>
        {
            @Override
            public TempModifierTypeArgument instantiate(CommandBuildContext pContext)
            {   return new TempModifierTypeArgument();
            }

            @Override
            public ArgumentTypeInfo<TempModifierTypeArgument, ?> type()
            {   return Info.this;
            }
        }
    }
}
