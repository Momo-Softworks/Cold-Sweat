package net.momostudios.coldsweat.client.event;

import net.minecraft.item.ArmorItem;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.init.ItemInit;
import net.momostudios.coldsweat.core.util.ModItems;

@Mod.EventBusSubscriber
public class ItemTooltipInfo
{
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void addTTInfo(ItemTooltipEvent event)
    {
        if (event.getItemStack().getItem() == ModItems.FILLED_WATERSKIN && event.getPlayer() != null)
        {
            boolean celsius = ColdSweatConfig.getInstance().celsius();
            double temp = event.getItemStack().getOrCreateTag().getDouble("temperature");
            String color = temp == 0 ? "7" : (temp < 0 ? "9" : "c");
            String tempUnits = celsius ? "C" : "F";
            temp = celsius ? (int) (22 + ((temp - 32) * 5/8) / 2) : (int) (75 + temp / 2);

            event.getToolTip().add(1, new StringTextComponent("\u00a77" + new TranslationTextComponent(
                "item." + ColdSweat.MOD_ID + ".waterskin.filled").getString() + " (\u00a7" + color + (int) temp + " \u00b0" + tempUnits + "\u00a77)"));
        }
        else if (event.getItemStack().getItem() instanceof ArmorItem && event.getItemStack().getOrCreateTag().getBoolean("insulated"))
        {
            event.getToolTip().add(1, new StringTextComponent("\u00a7d" +
                new TranslationTextComponent("modifier." + ColdSweat.MOD_ID + ".insulated").getString() + "\u00a7r"));
        }
    }
}
