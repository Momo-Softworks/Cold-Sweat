package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.common.temperature.modifier.InsulationTempModifier;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.util.ConfigEntry;
import dev.momostudios.coldsweat.util.entity.PlayerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

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
            Player player = event.player;
            if (player.tickCount % 10 == 0)
            {
                ItemStack helmetItem     = player.getItemBySlot(EquipmentSlot.HEAD);
                ItemStack chestplateItem = player.getItemBySlot(EquipmentSlot.CHEST);
                ItemStack leggingsItem   = player.getItemBySlot(EquipmentSlot.LEGS);
                ItemStack bootsItem      = player.getItemBySlot(EquipmentSlot.FEET);

                int leatherMultiplier = 0;
                leatherMultiplier += (helmetItem.getItem()     instanceof ArmorItem ? ((ArmorItem) helmetItem.getItem()).getDefense() : 0) * 2;
                leatherMultiplier += (chestplateItem.getItem() instanceof ArmorItem ? ((ArmorItem) chestplateItem.getItem()).getDefense() : 0) * 2;
                leatherMultiplier += (leggingsItem.getItem()   instanceof ArmorItem ? ((ArmorItem) leggingsItem.getItem()).getDefense() : 0) * 2;
                leatherMultiplier += (bootsItem.getItem()      instanceof ArmorItem ? ((ArmorItem) bootsItem.getItem()).getDefense() : 0) * 2;

                if (helmetItem != null)
                {
                    int helmetInsulation = getInsulatingArmor(helmetItem).value;

                    if (bootsItem.getOrCreateTag().getBoolean("insulated"))
                    {
                        if (leatherHelmetInsulation == -1)
                        {
                            for (List<?> list : ItemSettingsConfig.getInstance().insulatingArmor())
                            {
                                if (list.get(0).equals("leather_boots"))
                                {
                                    leatherMultiplier += ((Number) list.get(1)).doubleValue();
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
                            for (List<?> list : ItemSettingsConfig.getInstance().insulatingArmor())
                            {
                                if (list.get(0).equals("leather_boots"))
                                {
                                    leatherMultiplier += ((Number) list.get(1)).doubleValue();
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
                            for (List<?> list : ItemSettingsConfig.getInstance().insulatingArmor())
                            {
                                if (list.get(0).equals("leather_boots"))
                                {
                                    leatherMultiplier += ((Number) list.get(1)).doubleValue();
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
                            for (List<?> list : ItemSettingsConfig.getInstance().insulatingArmor())
                            {
                                if (list.get(0).equals("leather_boots"))
                                {
                                    leatherMultiplier += ((Number) list.get(1)).doubleValue();
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
                    PlayerHelper.addModifier(player, new InsulationTempModifier(leatherMultiplier).expires(10), Temperature.Types.RATE, false);
                }
            }
        }
    }

    public static ConfigEntry getInsulatingArmor(ItemStack stack)
    {
        String id = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        for (List<?> s : ItemSettingsConfig.getInstance().insulatingArmor())
        {
            if (s.get(0).equals(id))
            {
                return new ConfigEntry(id, ((Number) s.get(1)).intValue());
            }
        }
        return new ConfigEntry(id, 0);
    }
}