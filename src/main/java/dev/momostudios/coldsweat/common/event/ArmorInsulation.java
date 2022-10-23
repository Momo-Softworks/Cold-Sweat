package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.InsulationTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.TempHelper;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorInsulation
{

    @SubscribeEvent
    public static void addLeatherModifiers(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level.isClientSide() && player.tickCount % 10 == 0)
        {
            Map<Item, Double> insulatingArmors = ConfigSettings.INSULATING_ARMORS.get();

            int insulation = 0;
            for (EquipmentSlot slot : EquipmentSlot.values())
            {
                if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) continue;

                ItemStack armorStack = player.getItemBySlot(slot);
                if (armorStack.getItem() instanceof ArmorItem armorItem)
                {
                    // Add the armor's defense value to the insulation value.
                    insulation += armorItem.getDefense();

                    // Add the armor's intrinsic insulation value (defined in configs)
                    // Mutually exclusive with Sewing Table insulation
                    Number insulationValue = insulatingArmors.get(armorStack.getItem());
                    if (insulationValue != null)
                    {
                        insulation += insulationValue.intValue();
                    }
                    // Add the armor's insulation value from the Sewing Table
                    else if (armorStack.getOrCreateTag().getBoolean("insulated"))
                    {
                        insulation += getSlotWeight(slot);
                    }
                }
            }

            TempModifier currentMod = TempHelper.getModifier(player, Temperature.Type.RATE, InsulationTempModifier.class);
            if (currentMod == null || (int) currentMod.getArgument("warmth") != insulation)
            {
                if (insulation == 0 && currentMod != null)
                    TempHelper.removeModifiers(player, Temperature.Type.RATE, (mod) -> mod instanceof InsulationTempModifier);
                else
                    TempHelper.replaceModifier(player, new InsulationTempModifier(insulation).tickRate(10), Temperature.Type.RATE);
            }
        }
    }

    static int getSlotWeight(EquipmentSlot slot)
    {
        return switch (slot)
        {
            case HEAD -> 4;
            case CHEST -> 7;
            case LEGS -> 6;
            case FEET -> 3;
            default -> 0;
        };
    }
}