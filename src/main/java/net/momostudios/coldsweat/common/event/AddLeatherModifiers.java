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
import net.momostudios.coldsweat.util.PlayerHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AddLeatherModifiers
{
    private static int leatherBootsInsulation = -1;
    private static int leatherPantsInsulation = -1;
    private static int leatherChestInsulation = -1;
    private static int leatherHelmetInsulation = -1;

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

                    if (bootsItem.getOrCreateTag().getBoolean("insulated"))
                    {
                        if (leatherHelmetInsulation == -1)
                        {
                            for (List<String> list : ItemSettingsConfig.getInstance().insulatingArmor())
                            {
                                if (list.get(0).equals("leather_boots"))
                                {
                                    leatherMultiplier += Integer.parseInt(list.get(1));
                                    break;
                                }
                            }
                            if (leatherHelmetInsulation == -1)
                                leatherHelmetInsulation = 4;
                        }
                        leatherMultiplier += leatherHelmetInsulation;
                    }
                    else if (helmetInsulation > 0)
                        leatherMultiplier += helmetInsulation;
                }
                if (chestplateItem != null)
                {
                    int chestInsulation = getInsulatingArmor(chestplateItem).value;

                    if (bootsItem.getOrCreateTag().getBoolean("insulated"))
                    {
                        if (leatherChestInsulation == -1)
                        {
                            for (List<String> list : ItemSettingsConfig.getInstance().insulatingArmor())
                            {
                                if (list.get(0).equals("leather_boots"))
                                {
                                    leatherMultiplier += Integer.parseInt(list.get(1));
                                    break;
                                }
                            }
                            if (leatherChestInsulation == -1)
                                leatherChestInsulation = 4;
                        }
                        leatherMultiplier += leatherChestInsulation;
                    }
                    else if (chestInsulation > 0)
                        leatherMultiplier += chestInsulation;
                }
                if (leggingsItem != null)
                {
                    int legsInsulation = getInsulatingArmor(leggingsItem).value;

                    if (bootsItem.getOrCreateTag().getBoolean("insulated"))
                    {
                        if (leatherPantsInsulation == -1)
                        {
                            for (List<String> list : ItemSettingsConfig.getInstance().insulatingArmor())
                            {
                                if (list.get(0).equals("leather_boots"))
                                {
                                    leatherMultiplier += Integer.parseInt(list.get(1));
                                    break;
                                }
                            }
                            if (leatherPantsInsulation == -1)
                                leatherPantsInsulation = 4;
                        }
                        leatherMultiplier += leatherPantsInsulation;
                    }
                    else if (legsInsulation > 0)
                        leatherMultiplier += legsInsulation;
                }
                if (bootsItem != null)
                {
                    int bootsInsulation = getInsulatingArmor(bootsItem).value;

                    if (bootsItem.getOrCreateTag().getBoolean("insulated"))
                    {
                        if (leatherBootsInsulation == -1)
                        {
                            for (List<String> list : ItemSettingsConfig.getInstance().insulatingArmor())
                            {
                                if (list.get(0).equals("leather_boots"))
                                {
                                    leatherMultiplier += Integer.parseInt(list.get(1));
                                    break;
                                }
                            }
                            if (leatherBootsInsulation == -1)
                                leatherBootsInsulation = 4;
                        }
                        leatherMultiplier += leatherBootsInsulation;
                    }
                    else if (bootsInsulation > 0)
                        leatherMultiplier += bootsInsulation;
                }

                if (leatherMultiplier > 0)
                {
                    if (PlayerHelper.hasModifier(player, InsulationTempModifier.class, PlayerHelper.Types.RATE))
                    {
                        AtomicBoolean shouldRemove = new AtomicBoolean(false);
                        int multiplier = leatherMultiplier;
                        PlayerHelper.forEachModifier(player, PlayerHelper.Types.RATE, modifier ->
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
                            PlayerHelper.removeModifiers(player, PlayerHelper.Types.RATE, 1, modifier -> modifier instanceof InsulationTempModifier);
                        }
                    }
                    else PlayerHelper.addModifier(player, new InsulationTempModifier(leatherMultiplier), PlayerHelper.Types.RATE, false);
                }
                else PlayerHelper.removeModifiers(player, PlayerHelper.Types.RATE, Integer.MAX_VALUE, modifier -> modifier instanceof InsulationTempModifier);
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