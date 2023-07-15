package dev.momostudios.coldsweat.client.event;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.tooltip.InsulationTooltip;
import dev.momostudios.coldsweat.client.gui.tooltip.InsulatorTooltip;
import dev.momostudios.coldsweat.client.gui.tooltip.SoulspringTooltip;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap.Insulation;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap.InsulationPair;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.item.SoulspringLampItem;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ItemTooltipInfo
{
    @SubscribeEvent
    public static void addSimpleTooltips(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == ModItems.FILLED_WATERSKIN && event.getEntity() != null)
        {
            boolean celsius = ClientSettingsConfig.getInstance().celsius();
            double temp = stack.getOrCreateTag().getDouble("temperature");
            String color = temp == 0 ? "7" : (temp < 0 ? "9" : "c");
            String tempUnits = celsius ? "C" : "F";
            temp = temp / 2 + 95;
            if (celsius) temp = CSMath.convertTemp(temp, Temperature.Units.F, Temperature.Units.C, true);
            temp += ClientSettingsConfig.getInstance().tempOffset() / 2.0;

            event.getToolTip().add(1, Component.literal("§7" + Component.translatable(
                    "item.cold_sweat.waterskin.filled").getString() + " (§" + color + (int) temp + " °" + tempUnits + "§7)§r"));
        }
        else if (stack.getItem() == ModItems.SOULSPRING_LAMP)
        {
            if (event.getFlags().isAdvanced())
            {
                event.getToolTip().add(Math.max(event.getToolTip().size() - 2, 1), Component.literal("§fFuel: " + (int) event.getItemStack().getOrCreateTag().getDouble("fuel") + " / " + 64));
            }
        }
    }

    @SubscribeEvent
    public static void addCustomTooltips(RenderTooltipEvent.GatherComponents event)
    {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        Pair<Double, Double> itemInsul = null;
        // Add the armor insulation tooltip if the armor has insulation
        if (stack.getItem() instanceof SoulspringLampItem)
        {   if (!Screen.hasShiftDown())
            {   event.getTooltipElements().add(1, Either.left(Component.literal("§9? §8'Shift'")));
            }
            event.getTooltipElements().add(1, Either.right(new SoulspringTooltip(stack.getOrCreateTag().getDouble("fuel"))));
        }
        // If the item is an insulation ingredient, add the tooltip
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem())) != null)
        {   event.getTooltipElements().add(1, Either.right(new InsulatorTooltip(ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()), false)));
        }
        else if ((itemInsul = ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem())) != null)
        {   event.getTooltipElements().add(1, Either.right(new InsulatorTooltip(ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem()), true)));
        }
        // If the item is insulated armor
        if (stack.getItem() instanceof ArmorItem && (itemInsul == null || !ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem()).equals(itemInsul)))
        {
            // Create the list of insulation pairs from NBT
            List<InsulationPair> insulation = stack.getCapability(ModCapabilities.ITEM_INSULATION)
            .map(c ->
            {
                if (c instanceof ItemInsulationCap cap)
                {   return cap;
                }
                return new ItemInsulationCap();
            }).map(cap -> cap.deserializeSimple(stack)).orElse(new ArrayList<>());

            // If the armor has intrinsic insulation due to configs, add it to the list
            ConfigSettings.INSULATING_ARMORS.get().computeIfPresent(stack.getItem(), (item, pair) ->
            {
                double cold = pair.getFirst();
                double hot = pair.getSecond();
                double neutral = cold > 0 == hot > 0 ? CSMath.minAbs(cold, hot) : 0;
                if (cold == neutral) cold = 0;
                if (hot == neutral) hot = 0;
                // Cold insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(cold)) / 2; i++)
                {
                    double coldInsul = CSMath.minAbs(CSMath.shrink(cold, i * 2), 2);
                    insulation.add(new ItemInsulationCap.Insulation(coldInsul, 0d));
                }

                // Neutral insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(neutral)); i++)
                {
                    double neutralInsul = CSMath.minAbs(CSMath.shrink(neutral, i), 1);
                    insulation.add(new Insulation(neutralInsul, neutralInsul));
                }

                // Hot insulation
                for (int i = 0; i < CSMath.ceil(Math.abs(hot)) / 2; i++)
                {
                    double hotInsul = CSMath.minAbs(CSMath.shrink(hot, i * 2), 2);
                    insulation.add(new Insulation(0d, hotInsul));
                }
                return pair;
            });

            // Sort the insulation values from cold to hot
            ItemInsulationCap.sortInsulationList(insulation);

            // Calculate the number of slots and render the insulation bar
            if (insulation.size() > 0)
            {   event.getTooltipElements().add(1, Either.right(new InsulationTooltip(insulation, stack)));
            }
        }
    }
}
