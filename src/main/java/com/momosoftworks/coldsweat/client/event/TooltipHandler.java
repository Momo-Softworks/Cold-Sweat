package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.client.gui.tooltip.InsulationTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.InsulatorTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.SoulspringTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.Tooltip;
import com.momosoftworks.coldsweat.common.capability.ItemInsulationCap;
import com.momosoftworks.coldsweat.common.capability.ItemInsulationCap.Insulation;
import com.momosoftworks.coldsweat.common.capability.ItemInsulationCap.InsulationPair;
import com.momosoftworks.coldsweat.common.capability.ItemInsulationManager;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.enchantment.IArmorVanishable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TooltipHandler
{
    public static final TextFormatting COLD = TextFormatting.BLUE;
    public static final TextFormatting HOT  = TextFormatting.RED;

    @SubscribeEvent
    public static void addSimpleTooltips(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();

        // Get the index at which the tooltip should be inserted
        int tooltipIndex = Math.min(1, event.getToolTip().size() - 1);
        while (!event.getToolTip().get(tooltipIndex).equals(stack.getDisplayName()))
        {   tooltipIndex++;
            if (tooltipIndex >= event.getToolTip().size())
            {   tooltipIndex = Math.min(1, event.getToolTip().size());
                break;
            }
        }

        Pair<Double, Double> itemInsul;
        if (stack.getItem() == ModItems.SOULSPRING_LAMP)
        {
            if (!Screen.hasShiftDown())
            {   event.getToolTip().add(tooltipIndex, new StringTextComponent("? ").withStyle(TextFormatting.BLUE).append(new StringTextComponent("'Shift'").withStyle(TextFormatting.DARK_GRAY)));
            }
            else for (int i = 0; i < CSMath.ceil(ConfigSettings.LAMP_FUEL_ITEMS.get().size() / 6d) + 1; i++)
                 {   event.getToolTip().add(tooltipIndex, new StringTextComponent(""));
                 }
            event.getToolTip().add(tooltipIndex, new StringTextComponent(TOOLTIPS.get(SoulspringTooltip.class)).withStyle(TextFormatting.BLACK));
        }
        else if (stack.getUseAnimation() == UseAction.DRINK || stack.getUseAnimation() == UseAction.EAT)
        {
            ConfigSettings.FOOD_TEMPERATURES.get().computeIfPresent(event.getItemStack().getItem(), (item, temp) ->
            {
                int index = Minecraft.getInstance().options.advancedItemTooltips ? event.getToolTip().size() - 1 : event.getToolTip().size();
                event.getToolTip().add(index,
                        temp > 0 ? new TranslationTextComponent("tooltip.cold_sweat.temperature_effect", "+" + temp).withStyle(HOT)
                                 : new TranslationTextComponent("tooltip.cold_sweat.temperature_effect", temp).withStyle(COLD)
                );
                event.getToolTip().add(index, new TranslationTextComponent("tooltip.cold_sweat.consumed").withStyle(TextFormatting.GRAY));
                event.getToolTip().add(index, new StringTextComponent(""));
                return temp;
            });
        }
        // Is insulation item
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().getOrDefault(stack.getItem(),
                              ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem()))) != null
        && (itemInsul.getFirst() > 0 || itemInsul.getSecond() > 0))
        {   event.getToolTip().add(tooltipIndex, new StringTextComponent(TOOLTIPS.get(InsulatorTooltip.class)).withStyle(TextFormatting.BLACK));
        }
        // Has insulation (armor)
        else if (stack.getItem() instanceof IArmorVanishable && ItemInsulationManager.getInsulationCap(stack).map(c -> !c.getInsulation().isEmpty()).orElse(false))
        {   event.getToolTip().add(tooltipIndex, new StringTextComponent(TOOLTIPS.get(InsulationTooltip.class)).withStyle(TextFormatting.BLACK));
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
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem())) != null && !itemInsul.equals(emptyInsul))
        {   tooltip = new InsulatorTooltip(ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()), InsulatorTooltip.InsulationType.NORMAL);
        }
        else if ((itemInsul = ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem())) != null && !itemInsul.equals(emptyInsul))
        {   tooltip = new InsulatorTooltip(ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(stack.getItem()), InsulatorTooltip.InsulationType.ADAPTIVE);
        }
        else if (CompatManager.isCuriosLoaded() && (itemInsul = ConfigSettings.INSULATING_CURIOS.get().get(stack.getItem())) != null && !itemInsul.equals(emptyInsul))
        {   tooltip = new InsulatorTooltip(ConfigSettings.INSULATING_CURIOS.get().get(stack.getItem()), InsulatorTooltip.InsulationType.CURIO);
        }

        // If the item is insulated armor
        Pair<Double, Double> armorInsul;
        if (stack.getItem() instanceof IArmorVanishable && (!Objects.deepEquals((armorInsul = ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem())), itemInsul) || armorInsul == null))
        {
            // Create the list of insulation pairs from NBT
            List<InsulationPair> insulation = ((ItemInsulationCap) ItemInsulationManager.getInsulationCap(stack).orElse(new ItemInsulationCap())).deserializeSimple(stack);

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
                    insulation.add(new Insulation(coldInsul, 0d));
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
            if (!insulation.isEmpty())
            {   tooltip = new InsulationTooltip(insulation, stack);
            }
        }
        // Find the empty line that this tooltip should fill
        if (tooltip != null)
        {
            String lineToReplace = TOOLTIPS.get(tooltip.getClass());

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
            tooltip.renderImage(Minecraft.getInstance().font, event.getX(), y, event.getMatrixStack(), Minecraft.getInstance().getItemRenderer(), 0);
            tooltip.renderText(Minecraft.getInstance().font, event.getX(), y, event.getMatrixStack(), Minecraft.getInstance().getItemRenderer(), 0);
        }
    }

    static int FUEL_FADE_TIMER = 0;

    @SubscribeEvent
    public static void renderSoulLampInsertTooltip(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        if (event.getGui() instanceof ContainerScreen)
        {
            ContainerScreen<?> inventoryScreen = (ContainerScreen<?>) event.getGui();
            PlayerEntity player = Minecraft.getInstance().player;

            if (player != null && inventoryScreen.getSlotUnderMouse() != null
                    && inventoryScreen.getSlotUnderMouse().getItem().getItem() == ModItems.SOULSPRING_LAMP)
            {
                float fuel = inventoryScreen.getSlotUnderMouse().getItem().getOrCreateTag().getFloat("fuel");
                ItemStack carriedStack = player.inventory.getCarried();

                if (!carriedStack.isEmpty() && ConfigSettings.LAMP_FUEL_ITEMS.get().containsKey(carriedStack.getItem()))
                {
                    int fuelValue = carriedStack.getCount();
                    int slotX = inventoryScreen.getSlotUnderMouse().x + ((ContainerScreen<?>) event.getGui()).getGuiLeft();
                    int slotY = inventoryScreen.getSlotUnderMouse().y + ((ContainerScreen<?>) event.getGui()).getGuiTop();

                    MatrixStack ms = event.getMatrixStack();

                    // If the mouse is above the slot, move the box to the bottom
                    if (event.getMouseY() < slotY + 8)
                        ms.translate(0, 32, 0);

                    event.getGui().renderTooltip(ms, new StringTextComponent("       "), slotX - 18, slotY);

                    RenderSystem.defaultBlendFunc();

                    // Render background
                    Minecraft.getInstance().textureManager.bind(SoulspringTooltip.TOOLTIP_LOCATION.get());
                    AbstractGui.blit(ms, slotX - 7, slotY - 12, 401, 0, 0, 30, 8, 34, 30);

                    // Render ghost overlay
                    RenderSystem.enableBlend();
                    RenderSystem.color4f(1f, 1f, 1f, 0.15f + (float) ((Math.sin(FUEL_FADE_TIMER / 5f) + 1f) / 2f) * 0.4f);
                    AbstractGui.blit(ms, slotX - 7, slotY - 12, 401, 0, 8, Math.min(30, (int) ((fuel + fuelValue) / 2.1333f)), 8, 34, 30);
                    RenderSystem.disableBlend();

                    // Render current fuel
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1f);
                    AbstractGui.blit(ms, slotX - 7, slotY - 12, 401, 0, 16, (int) (fuel / 2.1333f), 8, 34, 30);
                }
            }
        }
    }

    @SubscribeEvent
    public static void tickSoulLampInsertTooltip(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {   FUEL_FADE_TIMER++;
        }
    }

    private static final Map<Class<? extends Tooltip>, String> TOOLTIPS = new HashMap<>();
    private static int TOOLTIP_REGISTRY_SIZE = 0;

    private static void registerTooltip(Class<? extends Tooltip> tooltip)
    {
        if (!TOOLTIPS.containsKey(tooltip))
        {
            if (TOOLTIP_REGISTRY_SIZE >= 63)
            {   throw new RuntimeException("Too many tooltips registered!");
            }
            String code = Integer.toBinaryString(TOOLTIP_REGISTRY_SIZE);
            while (code.length() < 5)
            {   code = "0" + code;
            }
            code = code.replace("0", "-");
            code = code.replace("1", "+");
            TOOLTIPS.put(tooltip, code);
            TOOLTIP_REGISTRY_SIZE++;
        }
    }

    static
    {
        registerTooltip(InsulatorTooltip.class);
        registerTooltip(InsulationTooltip.class);
        registerTooltip(SoulspringTooltip.class);
    }
}
