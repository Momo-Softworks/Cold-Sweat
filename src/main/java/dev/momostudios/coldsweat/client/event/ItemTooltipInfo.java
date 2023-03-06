package dev.momostudios.coldsweat.client.event;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.tooltip.InsulationTooltip;
import dev.momostudios.coldsweat.client.gui.tooltip.InsulatorTooltip;
import dev.momostudios.coldsweat.client.gui.tooltip.SoulspringTooltip;
import dev.momostudios.coldsweat.common.item.SoulspringLampItem;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
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
    public static void addSimpleTooltips(ItemTooltipEvent event)
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

            event.getToolTip().add(1, new TextComponent("§7" + new TranslatableComponent(
                "item.cold_sweat.waterskin.filled").getString() + " (§" + color + (int) temp + " °" + tempUnits + "§7)§r"));
        }
        else if (stack.getItem() == ModItems.SOULSPRING_LAMP)
        {
            if (event.getFlags().isAdvanced())
            {
                event.getToolTip().add(Math.max(event.getToolTip().size() - 2, 1), new TextComponent("§fFuel: " + (int) event.getItemStack().getOrCreateTag().getDouble("fuel") + " / " + 64));
            }
        }
    }

    @SubscribeEvent
    public static void addCustomTooltips(RenderTooltipEvent.GatherComponents event)
    {
        ItemStack stack = event.getItemStack();
        // Add the armor insulation tooltip if the armor has insulation
        if (stack.getItem() instanceof SoulspringLampItem)
        {
            event.getTooltipElements().add(1, Either.right(new SoulspringTooltip(stack.getOrCreateTag().getDouble("fuel"))));
        }
        else if (stack.getItem() instanceof ArmorItem && stack.getOrCreateTag().getBoolean("Insulated"))
        {
            // Create the list of insulation pairs from NBT
            List<Pair<Double, Double>> insulation = stack.getOrCreateTag().getList("Insulation", 10).stream()
            .map(nbt ->
            {
                CompoundTag compound = (CompoundTag) nbt;
                return Pair.of(compound.getDouble("Cold"), compound.getDouble("Hot"));
            }).toList();
            event.getTooltipElements().add(1, Either.right(new InsulationTooltip(insulation, stack)));
        }
        // If the item is an insulator, add the tooltip
        else
        {
            Pair<Double, Double> itemInsul = ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem());
            if (itemInsul != null)
            {
                event.getTooltipElements().add(1, Either.right(new InsulatorTooltip(itemInsul)));
            }
        }
    }
}
