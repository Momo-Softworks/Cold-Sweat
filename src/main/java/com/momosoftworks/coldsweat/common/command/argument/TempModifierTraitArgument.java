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

public class TempModifierTraitArgument extends StringRepresentableArgument<Temperature.Trait>
{
    private static final Codec<Temperature.Trait> MODIFIERS_CODEC = StringRepresentable.fromEnum(() -> EntityTempManager.VALID_MODIFIER_TRAITS);

    private TempModifierTraitArgument()
    {   super(MODIFIERS_CODEC, () -> EntityTempManager.VALID_MODIFIER_TRAITS);
    }

    public static TempModifierTraitArgument modifier()
    {   return new TempModifierTraitArgument();
    }

    public static Temperature.Trait getModifier(CommandContext<CommandSourceStack> context, String argument)
    {   return context.getArgument(argument, Temperature.Trait.class);
    }

    protected String convertId(String Id)
    {   return Id.toLowerCase(Locale.ROOT);
    }

    public static class Info implements ArgumentTypeInfo<TempModifierTraitArgument, Info.Template>
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
        public Template unpack(TempModifierTraitArgument argument)
        {   return new Template();
        }

        public final class Template implements ArgumentTypeInfo.Template<TempModifierTraitArgument>
        {
            @Override
            public TempModifierTraitArgument instantiate(CommandBuildContext pContext)
            {   return new TempModifierTraitArgument();
            }

            @Override
            public ArgumentTypeInfo<TempModifierTraitArgument, ?> type()
            {   return Info.this;
            }
        }
    }
}
