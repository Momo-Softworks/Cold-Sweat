package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.InsulationTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.config.LoadedValue;
import dev.momostudios.coldsweat.api.util.TempHelper;
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
    public static LoadedValue<Map<Item, Number>> INSULATING_ARMORS = LoadedValue.of(() ->
            ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().insulatingArmor()));

    @SubscribeEvent
    public static void addLeatherModifiers(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level.isClientSide() && player.tickCount % 10 == 0)
        {
            Map<Item, Number> insulatingArmors = INSULATING_ARMORS.get();

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

            if (insulation > 0)
            {
                TempModifier modifier = TempHelper.getModifier(player, Temperature.Type.RATE, InsulationTempModifier.class);

                if (modifier != null)
                    modifier.setArgument("warmth", insulation);
                else
                    TempHelper.replaceModifier(player, new InsulationTempModifier(insulation).expires(10).tickRate(10), Temperature.Type.RATE);
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