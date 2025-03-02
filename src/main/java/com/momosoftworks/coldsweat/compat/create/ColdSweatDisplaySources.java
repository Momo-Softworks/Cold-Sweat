package com.momosoftworks.coldsweat.compat.create;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.blockentity.ThermolithBlockEntity;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ColdSweatDisplaySources
{
    public static final DeferredRegister<DisplaySource> DISPLAY_SOURCES = DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, ColdSweat.MOD_ID);
    public static final RegistryObject<DisplaySource> THERMOLITH = DISPLAY_SOURCES.register("cold_sweat_thermolith", () ->
    {
        DisplaySource source = new Thermolith();
        DisplaySource.BY_BLOCK.add(ModBlocks.THERMOLITH, source);
        return source;
    });

    public static class Thermolith extends SingleLineDisplaySource
    {
        @Override
        protected MutableComponent provideLine(DisplayLinkContext displayLinkContext, DisplayTargetStats displayTargetStats)
        {
            if (displayLinkContext.getSourceBlockEntity() instanceof ThermolithBlockEntity thermolith)
            {
                double temperature = WorldHelper.getTemperatureAt(thermolith.getLevel(), thermolith.getBlockPos());

                Temperature.Units units = Temperature.Units.values()[displayLinkContext.sourceConfig().getInt("Units")];

                double convertedTemp = Temperature.convert(temperature, Temperature.Units.MC, units, true);

                String places = units == Temperature.Units.MC ? "%.2f" : "%.0f";
                String text = String.format(places+"%s", convertedTemp, units.getFormattedName());
                return Component.literal(text);
            }
            return Component.empty();
        }

        @Override
        protected boolean allowsLabeling(DisplayLinkContext displayLinkContext)
        {   return true;
        }

        @Override
        public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine)
        {
            super.initConfigurationWidgets(context, builder, isFirstLine);
            if (!isFirstLine)
            {
                builder.addSelectionScrollInput(0, 137, (input, label) -> {
                    input.forOptions(List.of(Component.literal(Temperature.Units.F.getFormattedName()),
                                             Component.literal(Temperature.Units.C.getFormattedName()),
                                             Component.literal(Temperature.Units.MC.getFormattedName())))
                         .titled(Component.translatable("cold_sweat.config.units.name"));
                }, "Units");
            }
        }

        @Override
        public Component getName()
        {   return Component.translatable("block.cold_sweat.thermolith");
        }
    }
}
