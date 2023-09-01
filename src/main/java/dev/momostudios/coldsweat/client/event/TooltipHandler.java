package dev.momostudios.coldsweat.client.event;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.client.gui.tooltip.*;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap.Insulation;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap.InsulationPair;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.item.SoulspringLampItem;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.IArmorVanishable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TooltipHandler
{
    @SubscribeEvent
    public static void addSimpleTooltips(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        Pair<Double, Double> itemInsul;
        Pair<Double, Double> emptyInsul = Pair.of(0d, 0d);
        if (stack.getItem() == ModItems.SOULSPRING_LAMP)
        {
            if (!Screen.hasShiftDown())
            {   event.getToolTip().add(1, new StringTextComponent("§9? §8'Shift'"));
            }
            else
            {
                for (int i = 0; i < CSMath.ceil(ConfigSettings.LAMP_FUEL_ITEMS.get().size() / 6d) + 1; i++)
                {   event.getToolTip().add(1, new StringTextComponent(""));
                }
            }
            event.getToolTip().add(1, new StringTextComponent(" §0---§r"));
        }
        // Is insulation item
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().getOrDefault(stack.getItem(), ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem()))) != null && !itemInsul.equals(emptyInsul))
        {   event.getToolTip().add(1, new StringTextComponent(" §0--§r"));
        }
        // Has insulation (armor)
        else if (stack.getItem() instanceof IArmorVanishable && stack.getCapability(ModCapabilities.ITEM_INSULATION).map(c -> c.getInsulation().size() > 0).orElse(false))
        {   event.getToolTip().add(1, new StringTextComponent(" §0-§r"));
        }
    }

    @SubscribeEvent
    public static void renderCustomTooltips(RenderTooltipEvent.PostText event)
    {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.inventory.getCarried().isEmpty()) return;
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        Tooltip tooltip = null;

        Pair<Double, Double> itemInsul = null;
        Pair<Double, Double> emptyInsul = Pair.of(0d, 0d);
        // Add the armor insulation tooltip if the armor has insulation
        if (stack.getItem() instanceof SoulspringLampItem)
        {   tooltip = new SoulspringTooltip(stack.getOrCreateTag().getDouble("fuel"));
        }
        // If the item is an insulation ingredient, add the tooltip
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem())) != null && !emptyInsul.equals(itemInsul))
        {   tooltip = new InsulatorTooltip(ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()), false);
        }
        else if ((itemInsul = ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem())) != null && !emptyInsul.equals(itemInsul))
        {   tooltip = new InsulatorTooltip(ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem()), true);
        }


        // If the item is insulated armor
        Pair<Double, Double> armorInsul = null;
        if (stack.getItem() instanceof IArmorVanishable && (!Objects.deepEquals((armorInsul = ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem())), itemInsul) || armorInsul == null))
        {
            // Create the list of insulation pairs from NBT
            List<InsulationPair> insulation = stack.getCapability(ModCapabilities.ITEM_INSULATION)
            .map(c ->
            {
                if (c instanceof ItemInsulationCap)
                {   return ((ItemInsulationCap) c);
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
            {   tooltip = new InsulationTooltip(insulation, stack);
            }
        }
        // Find the empty line that this tooltip should fill
        String lineToReplace = tooltip instanceof SoulspringTooltip ? " ---"
                             : tooltip instanceof InsulatorTooltip ? " --"
                             : tooltip instanceof InsulationTooltip ? " -" : null;
        int y = event.getY();
        if (lineToReplace != null)
        {
            List<? extends ITextProperties> tooltipLines = event.getLines();
            for (int i = 0; i < tooltipLines.size(); i++)
            {
                if (lineToReplace.equals(TextFormatting.stripFormatting(tooltipLines.get(i).getString())))
                {   y += 10 * (i - 1) + 1;
                    break;
                }
            }
        }

        if (tooltip != null)
        {   tooltip.renderImage(Minecraft.getInstance().font, event.getX(), y, event.getMatrixStack(), Minecraft.getInstance().getItemRenderer(), 0);
            tooltip.renderText(Minecraft.getInstance().font, event.getX(), y, event.getMatrixStack(), Minecraft.getInstance().getItemRenderer(), 0);
        }
    }
}
