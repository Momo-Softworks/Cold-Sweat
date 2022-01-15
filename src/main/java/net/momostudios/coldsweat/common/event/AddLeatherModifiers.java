package net.momostudios.coldsweat.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.modifier.InsulationTempModifier;
import net.momostudios.coldsweat.config.ItemSettingsConfig;
import net.momostudios.coldsweat.util.ItemEntry;
import net.momostudios.coldsweat.util.PlayerTemp;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AddLeatherModifiers
{
    @SubscribeEvent
    public static void addLeatherModifiers(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            PlayerEntity player = event.player;
            if (player.ticksExisted % 10 == 0)
            {
                ItemStack helmetItem = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
                ItemStack chestplateItem = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
                ItemStack leggingsItem = player.getItemStackFromSlot(EquipmentSlotType.LEGS);
                ItemStack bootsItem = player.getItemStackFromSlot(EquipmentSlotType.FEET);

                int leatherMultiplier = 0;
                leatherMultiplier += (helmetItem.getItem() instanceof ArmorItem ? ((ArmorItem) helmetItem.getItem()).getDamageReduceAmount() : 0) * 2;
                leatherMultiplier += (chestplateItem.getItem() instanceof ArmorItem ? ((ArmorItem) chestplateItem.getItem()).getDamageReduceAmount() : 0) * 2;
                leatherMultiplier += (leggingsItem.getItem() instanceof ArmorItem ? ((ArmorItem) leggingsItem.getItem()).getDamageReduceAmount() : 0) * 2;
                leatherMultiplier += (bootsItem.getItem() instanceof ArmorItem ? ((ArmorItem) bootsItem.getItem()).getDamageReduceAmount() : 0) * 2;

                if (helmetItem != null)
                {
                    int helmetInsulation = getInsulatingArmor(helmetItem).value;

                    if (helmetItem.getOrCreateTag().getBoolean("insulated") || helmetInsulation > 0)
                        leatherMultiplier += helmetInsulation;
                }
                if (chestplateItem != null)
                {
                    int chestInsulation = getInsulatingArmor(chestplateItem).value;

                    if (chestplateItem.getOrCreateTag().getBoolean("insulated") || chestInsulation > 0)
                        leatherMultiplier += chestInsulation;
                }
                if (leggingsItem != null)
                {
                    int legsInsulation = getInsulatingArmor(leggingsItem).value;

                    if (leggingsItem.getOrCreateTag().getBoolean("insulated") || legsInsulation > 0)
                        leatherMultiplier += legsInsulation;
                }
                if (bootsItem != null)
                {
                    int bootsInsulation = getInsulatingArmor(bootsItem).value;

                    if (leggingsItem.getOrCreateTag().getBoolean("insulated") || bootsInsulation > 0)
                        leatherMultiplier += bootsInsulation;
                }

                if (leatherMultiplier > 0)
                {
                    if (PlayerTemp.hasModifier(player, InsulationTempModifier.class, PlayerTemp.Types.RATE))
                    {
                        AtomicBoolean shouldRemove = new AtomicBoolean(false);
                        int multiplier = leatherMultiplier;
                        PlayerTemp.forEachModifier(player, PlayerTemp.Types.RATE, modifier ->
                        {
                            if (modifier instanceof InsulationTempModifier)
                            {
                                try
                                {
                                    modifier.setArgument("amount", multiplier);
                                }
                                catch (Exception e)
                                {
                                    shouldRemove.set(true);
                                }
                            }
                        });
                        // Reset the modifier if it throws an error
                        if (shouldRemove.get())
                        {
                            PlayerTemp.removeModifiers(player, PlayerTemp.Types.RATE, 1, modifier -> modifier instanceof InsulationTempModifier);
                        }
                    }
                    else PlayerTemp.addModifier(player, new InsulationTempModifier(leatherMultiplier), PlayerTemp.Types.RATE, false);
                }
                else PlayerTemp.removeModifiers(player, PlayerTemp.Types.RATE, Integer.MAX_VALUE, modifier -> modifier instanceof InsulationTempModifier);
            }
        }
    }

    public static ItemEntry getInsulatingArmor(ItemStack stack)
    {
        String id = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        for (List<String> s : ItemSettingsConfig.getInstance().insulatingArmor())
        {
            if (s.get(0).equals(id))
            {
                return new ItemEntry(id, Integer.parseInt(s.get(1)));
            }
        }
        return new ItemEntry(id, 0);
    }
}