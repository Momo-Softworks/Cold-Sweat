package com.momosoftworks.coldsweat.client.event;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.client.gui.tooltip.InsulationTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.InsulatorTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.SoulspringTooltip;
import com.momosoftworks.coldsweat.common.capability.ItemInsulationCap;
import com.momosoftworks.coldsweat.common.capability.ItemInsulationCap.Insulation;
import com.momosoftworks.coldsweat.common.capability.ItemInsulationCap.InsulationPair;
import com.momosoftworks.coldsweat.common.capability.ItemInsulationManager;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Wearable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TooltipHandler
{
    public static final ChatFormatting COLD = ChatFormatting.BLUE;
    public static final ChatFormatting HOT = ChatFormatting.RED;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addCustomTooltips(RenderTooltipEvent.GatherComponents event)
    {
        ItemStack stack = event.getItemStack();
        Pair<Double, Double> emptyInsul = Pair.of(0d, 0d);
        if (stack.isEmpty()) return;

        Pair<Double, Double> itemInsul = null;
        // Add the armor insulation tooltip if the armor has insulation
        if (stack.getItem() instanceof SoulspringLampItem)
        {   if (!Screen.hasShiftDown())
            {   event.getTooltipElements().add(1, Either.left(Component.literal("§9? §8'Shift'")));
            }
            event.getTooltipElements().add(1, Either.right(new SoulspringTooltip(stack.getOrCreateTag().getDouble("fuel"))));
        }
        else if (stack.getUseAnimation() == UseAnim.DRINK || stack.getUseAnimation() == UseAnim.EAT)
        {
            ConfigSettings.FOOD_TEMPERATURES.get().computeIfPresent(event.getItemStack().getItem(), (item, temp) ->
            {
                int index = Minecraft.getInstance().options.advancedItemTooltips ? event.getTooltipElements().size() - 1 : event.getTooltipElements().size();
                event.getTooltipElements().add(index, Either.left(
                        temp > 0 ? Component.translatable("tooltip.cold_sweat.temperature_effect", "+" + temp).withStyle(HOT)
                                 : Component.translatable("tooltip.cold_sweat.temperature_effect", temp).withStyle(COLD)
                        ));
                event.getTooltipElements().add(index, Either.left(Component.translatable("tooltip.cold_sweat.consumed").withStyle(ChatFormatting.GRAY)));
                event.getTooltipElements().add(index, Either.left(Component.empty()));
                return temp;
            });
        }
        // If the item is an insulation ingredient, add the tooltip
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem())) != null && !itemInsul.equals(emptyInsul))
        {   event.getTooltipElements().add(1, Either.right(new InsulatorTooltip(ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()), false)));
        }
        else if ((itemInsul = ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem())) != null && !itemInsul.equals(emptyInsul))
        {   event.getTooltipElements().add(1, Either.right(new InsulatorTooltip(ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem()), true)));
        }
        // If the item is insulated armor
        Pair<Double, Double> armorInsul;
        if (stack.getItem() instanceof Wearable && (!Objects.equals((armorInsul = ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem())), itemInsul) || armorInsul == null))
        {
            // Create the list of insulation pairs from NBT
            List<InsulationPair> insulation = ItemInsulationManager.getInsulationCap(stack)
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
