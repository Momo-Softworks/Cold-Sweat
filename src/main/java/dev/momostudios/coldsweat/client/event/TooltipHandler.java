package dev.momostudios.coldsweat.client.event;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.client.gui.tooltip.*;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap.Insulation;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap.InsulationPair;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.item.SoulspringLampItem;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TooltipHandler
{
    @SubscribeEvent
    public static void addCustomTooltips(RenderTooltipEvent.GatherComponents event)
    {
        ItemStack stack = event.getItemStack();
        Pair<Double, Double> emptyInsul = Pair.of(0d, 0d);
        if (stack.isEmpty()) return;

        Pair<Double, Double> itemInsul = null;
        // Add the armor insulation tooltip if the armor has insulation
        if (stack.getItem() instanceof SoulspringLampItem)
        {   if (!Screen.hasShiftDown())
            {   event.getTooltipElements().add(1, Either.left(new TextComponent("§9? §8'Shift'")));
            }
            event.getTooltipElements().add(1, Either.right(new SoulspringTooltip(stack.getOrCreateTag().getDouble("fuel"))));
        }
        // If the item is an insulation ingredient, add the tooltip
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem())) != null && !itemInsul.equals(emptyInsul))
        {   event.getTooltipElements().add(1, Either.right(new InsulatorTooltip(ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()), false)));
        }
        else if ((itemInsul = ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem())) != null && !itemInsul.equals(emptyInsul))
        {   event.getTooltipElements().add(1, Either.right(new InsulatorTooltip(ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem()), true)));
        }
        // If the item is insulated armor
        if (stack.getItem() instanceof Wearable && !Objects.equals(ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem()), itemInsul))
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
