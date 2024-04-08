package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.util.InsulationType;
import com.momosoftworks.coldsweat.client.gui.tooltip.InsulationTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.SoulspringTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.Tooltip;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.common.event.capability.ItemInsulationManager;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.util.ItemData;
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
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TooltipHandler
{
    public static final TextFormatting COLD = TextFormatting.BLUE;
    public static final TextFormatting HOT  = TextFormatting.RED;

    @SubscribeEvent
    public static void addSimpleTooltips(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        ItemData itemData = ItemData.of(stack);
        List<ITextComponent> elements = event.getToolTip();
        if (stack.isEmpty()) return;

        String hoverName = stack.getHoverName().getString().trim();

        // Get the index at which the tooltip should be inserted
        // Insert the tooltip at the first non-blank line under the item's name
        int tooltipIndex;
        for (tooltipIndex = 0; tooltipIndex < elements.size(); tooltipIndex++)
        {
            if (elements.get(tooltipIndex).getString().trim().equals(hoverName))
            {
                do
                {   tooltipIndex++;
                }
                while (tooltipIndex < elements.size()
                   && (elements.get(tooltipIndex).getString().isEmpty()));
                break;
            }
        }

        Insulation itemInsul = null;
        PlayerEntity player = Minecraft.getInstance().player;
        if (stack.getItem() == ModItems.SOULSPRING_LAMP)
        {
            if (!Screen.hasShiftDown())
            {   elements.add(tooltipIndex, new StringTextComponent("? ").withStyle(TextFormatting.BLUE).append(new StringTextComponent("'Shift'").withStyle(TextFormatting.DARK_GRAY)));
            }
            else for (int i = 0; i < CSMath.ceil(ConfigSettings.SOULSPRING_LAMP_FUEL.get().size() / 6d) + 1; i++)
                 {   elements.add(tooltipIndex, new StringTextComponent(""));
                 }
            elements.add(tooltipIndex, new StringTextComponent(TOOLTIPS.get(SoulspringTooltip.class)).withStyle(TextFormatting.BLACK));
        }
        else if (stack.getUseAnimation() == UseAction.DRINK || stack.getUseAnimation() == UseAction.EAT)
        {
            ConfigSettings.FOOD_TEMPERATURES.get().computeIfPresent(itemData, (item, temp) ->
            {
                int index = Minecraft.getInstance().options.advancedItemTooltips ? elements.size() - 1 : elements.size();
                elements.add(index,
                        temp > 0 ? new TranslationTextComponent("tooltip.cold_sweat.temperature_effect", "+" + temp).withStyle(HOT)
                                 : new TranslationTextComponent("tooltip.cold_sweat.temperature_effect", temp).withStyle(COLD)
                );
                elements.add(index, new TranslationTextComponent("tooltip.cold_sweat.consumed").withStyle(TextFormatting.GRAY));
                elements.add(index, new StringTextComponent(""));
                return temp;
            });
        }
        // Is insulation item
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().get(itemData)) != null
        && !itemInsul.isEmpty())
        {
            itemData = CSMath.orElse(CSMath.getExactKey(ConfigSettings.INSULATION_ITEMS.get(), itemData), itemData);
            if (itemData.testEntity(player))
            {   elements.add(tooltipIndex, new StringTextComponent(TOOLTIPS.get(InsulationTooltip.class)).withStyle(TextFormatting.BLACK));
            }
        }
        // Has insulation (armor)
        else if (stack.getItem() instanceof IArmorVanishable && ItemInsulationManager.getInsulationCap(stack).map(c -> !c.getInsulation().isEmpty()).orElse(false))
        {
            itemData = CSMath.orElse(CSMath.getExactKey(ConfigSettings.INSULATING_ARMORS.get(), itemData), itemData);
            if (itemData.testEntity(player))
            {   elements.add(tooltipIndex, new StringTextComponent(TOOLTIPS.get(InsulationTooltip.class)).withStyle(TextFormatting.BLACK));
            }
        }
    }

    @SubscribeEvent
    public static void renderCustomTooltips(RenderTooltipEvent.PostText event)
    {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.inventory.getCarried().isEmpty()) return;
        ItemStack stack = event.getStack();
        ItemData itemData = ItemData.of(stack);
        if (stack.isEmpty()) return;

        Tooltip tooltip = null;
        PlayerEntity player = Minecraft.getInstance().player;

        Insulation itemInsul = null;

        if (stack.getItem() instanceof SoulspringLampItem)
        {   tooltip = new SoulspringTooltip(stack.getOrCreateTag().getDouble("fuel"));
        }
        // If the item is an insulation ingredient, add the tooltip
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().get(itemData)) != null && !itemInsul.isEmpty())
        {
            itemData = CSMath.orElse(CSMath.getExactKey(ConfigSettings.INSULATION_ITEMS.get(), itemData), itemData);
            if (itemData.testEntity(player))
            {   tooltip = new InsulationTooltip(ConfigSettings.INSULATION_ITEMS.get().get(itemData).split(), InsulationType.ITEM);
            }
        }
        else if (CompatManager.isCuriosLoaded() && (itemInsul = ConfigSettings.INSULATING_CURIOS.get().get(itemData)) != null && !itemInsul.isEmpty())
        {
            itemData = CSMath.orElse(CSMath.getExactKey(ConfigSettings.INSULATING_CURIOS.get(), itemData), itemData);
            if (itemData.testEntity(player))
            {   tooltip = new InsulationTooltip(ConfigSettings.INSULATING_CURIOS.get().get(itemData).split(), InsulationType.CURIO);
            }
        }

        // If the item is insulated armor
        Insulation armorInsul;
        if (stack.getItem() instanceof IArmorVanishable && (!Objects.equals((armorInsul = ConfigSettings.INSULATING_ARMORS.get().get(itemData)), itemInsul) || armorInsul == null))
        {
            // Create the list of insulation pairs from NBT
            List<Insulation> insulation = ItemInsulationManager.getInsulationCap(stack)
                                          // Get insulation values
                                          .map(cap -> cap.getInsulation().stream()
                                          // Filter out insulation that doesn't match the player's predicate
                                          .filter(pair -> CSMath.getExactKey(ConfigSettings.INSULATION_ITEMS.get(), ItemData.of(pair.getFirst()))
                                                                  .testEntity(player))
                                          // Flat map the insulation values
                                          .map(pair -> pair.getSecond()).reduce(new ArrayList<>(), (list, insul) ->
                                          {   list.addAll(insul);
                                              return list;
                                          })).orElse(new ArrayList<>());
            itemData = CSMath.orElse(CSMath.getExactKey(ConfigSettings.INSULATING_ARMORS.get(), itemData), itemData);

            // If the armor has intrinsic insulation due to configs, add it to the list
            ConfigSettings.INSULATING_ARMORS.get().computeIfPresent(itemData, (item, pair) ->
            {   ItemData data = CSMath.getExactKey(ConfigSettings.INSULATING_ARMORS.get(), item);
                if (data != null && data.testEntity(player))
                {   insulation.addAll(pair.split());
                }
                return pair;
            });

            // Sort the insulation values from cold to hot
            Insulation.sort(insulation);

            // Calculate the number of slots and render the insulation bar
            if (!insulation.isEmpty())
            {   tooltip = new InsulationTooltip(insulation, InsulationType.ARMOR);
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
                double fuel = inventoryScreen.getSlotUnderMouse().getItem().getOrCreateTag().getDouble("Fuel");
                ItemStack carriedStack = player.inventory.getCarried();

                Double itemFuel;
                if (!carriedStack.isEmpty() && (itemFuel = ConfigSettings.SOULSPRING_LAMP_FUEL.get().get(ItemData.of(carriedStack))) != null)
                {
                    double fuelValue = carriedStack.getCount() * itemFuel;
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
        registerTooltip(InsulationTooltip.class);
        registerTooltip(SoulspringTooltip.class);
    }
}
