package dev.momostudios.coldsweat.client.event;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.client.gui.tooltip.InsulationTooltip;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.event.ArmorInsulation;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModItems;

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ItemTooltipInfo
{
    @SubscribeEvent
    public static void addTTInfo(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == ModItems.FILLED_WATERSKIN && event.getPlayer() != null)
        {
            boolean celsius = ClientSettingsConfig.getInstance().celsius();
            double temp = stack.getOrCreateTag().getDouble("temperature");
            String color = temp == 0 ? "7" : (temp < 0 ? "9" : "c");
            String tempUnits = celsius ? "C" : "F";
            temp = temp / 2 + 95;
            if (celsius) temp = CSMath.convertUnits(temp, Temperature.Units.F, Temperature.Units.C, true);
            temp += ClientSettingsConfig.getInstance().tempOffset() / 2.0;

            event.getToolTip().add(1, new TextComponent("\u00a77" + new TranslatableComponent(
                "item.cold_sweat.waterskin.filled").getString()
                    + " (\u00a7" + color + (int) temp + " \u00b0" + tempUnits + "\u00a77)\u00a7r"));
        }
        else if (stack.getItem() instanceof ArmorItem && stack.getOrCreateTag().getBoolean("Insulated"))
        {
            event.getToolTip().add(1, new TextComponent("                 "));
        }
        else if (stack.getItem() == ModItems.SOULSPRING_LAMP)
        {
            if (Screen.hasShiftDown())
            {
                event.getToolTip().add(1, new TextComponent("                 "));

                int fuelItems = ConfigSettings.LAMP_FUEL_ITEMS.get().size();
                int blankLines = CSMath.ceil(fuelItems / 6d) * 16 / Minecraft.getInstance().font.lineHeight + 1;
                for (int i = 0; i < blankLines; i++)
                {
                    event.getToolTip().add(2 + i, new TextComponent("                        "));
                }
            }
            else event.getToolTip().add(1, new TextComponent("         \u00a79? \u00a77'Shift'"));

            if (event.getFlags().isAdvanced())
            {
                event.getToolTip().add(Math.max(event.getToolTip().size() - 2, 2), new TextComponent("§fFuel: " + (int) event.getItemStack().getOrCreateTag().getDouble("fuel") + " / " + 64));
            }
        }
    }

    @SubscribeEvent
    public static void renderArmorTooltip(RenderTooltipEvent.GatherComponents event)
    {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof ArmorItem && stack.getOrCreateTag().getBoolean("Insulated"))
        {
            List<Pair<Double, Double>> insulation = stack.getOrCreateTag().getList("Insulation", 10).stream()
            .map(nbt ->
            {
                CompoundTag compound = (CompoundTag) nbt;
                return Pair.of(compound.getDouble("Cold"), compound.getDouble("Hot"));
            }).toList();
            event.getTooltipElements().add(1, Either.right(new InsulationTooltip(insulation, stack)));
        }
    }
}
