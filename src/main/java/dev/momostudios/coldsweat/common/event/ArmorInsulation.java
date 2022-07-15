package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.InsulationTempModifier;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.config.LoadedValue;
import dev.momostudios.coldsweat.util.entity.TempHelper;
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
        if (event.phase == TickEvent.Phase.END)
        {
            Player player = event.player;
            if (player.tickCount % 10 == 0)
            {
                Map<Item, Number> insulatingArmors = INSULATING_ARMORS.get();

                ItemStack headItem = player.getItemBySlot(EquipmentSlot.HEAD);
                ItemStack bodyItem = player.getItemBySlot(EquipmentSlot.CHEST);
                ItemStack legsItem = player.getItemBySlot(EquipmentSlot.LEGS);
                ItemStack feetItem = player.getItemBySlot(EquipmentSlot.FEET);

                int leatherMultiplier = 0;
                // The defense of armor pieces adds to the insulation
                if (headItem.getItem() instanceof ArmorItem helmet)     leatherMultiplier += helmet.getDefense();
                if (bodyItem.getItem() instanceof ArmorItem chestplate) leatherMultiplier += chestplate.getDefense();
                if (legsItem.getItem() instanceof ArmorItem leggings)   leatherMultiplier += leggings.getDefense();
                if (feetItem.getItem() instanceof ArmorItem boots)      leatherMultiplier += boots.getDefense();

                /* Helmet */
                int headInsulation = insulatingArmors.getOrDefault(headItem.getItem(), 0).intValue();

                if (headInsulation > 0) leatherMultiplier += headInsulation;
                else if (headItem.getOrCreateTag().getBoolean("insulated"))
                {
                    leatherMultiplier += 3;
                }

                /* Chestplate */
                int chestInsulation = insulatingArmors.getOrDefault(bodyItem.getItem(), 0).intValue();

                if (chestInsulation > 0) leatherMultiplier += chestInsulation;
                else if (bodyItem.getOrCreateTag().getBoolean("insulated"))
                {
                    leatherMultiplier += 5;
                }

                /* Leggings */
                int legsInsulation = insulatingArmors.getOrDefault(legsItem.getItem(), 0).intValue();

                if (legsInsulation > 0) leatherMultiplier += legsInsulation;
                else if (legsItem.getOrCreateTag().getBoolean("insulated"))
                {
                    leatherMultiplier += 5;
                }

                /* Boots */
                int feetInsulation = insulatingArmors.getOrDefault(feetItem.getItem(), 0).intValue();

                if (feetInsulation > 0) leatherMultiplier += feetInsulation;
                else if (feetItem.getOrCreateTag().getBoolean("insulated"))
                {
                    leatherMultiplier += 5;
                }

                if (leatherMultiplier > 0)
                {
                    TempHelper.addModifier(player, new InsulationTempModifier(leatherMultiplier).expires(10), Temperature.Types.RATE, false);
                }
            }
        }
    }
}