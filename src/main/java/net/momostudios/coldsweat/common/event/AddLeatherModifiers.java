package net.momostudios.coldsweat.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntNBT;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.modifier.LeatherTempModifier;
import net.momostudios.coldsweat.config.ItemSettingsConfig;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.List;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AddLeatherModifiers
{
    @SubscribeEvent
    public static void addLeatherModifiers(TickEvent.PlayerTickEvent event)
    {
        PlayerEntity player = event.player;

        ItemStack helmetItem = player.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, 3));
        ItemStack chestplateItem = player.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, 2));
        ItemStack leggingsItem = player.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, 1));
        ItemStack bootsItem = player.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, 0));

        int leatherMultiplier = 0;
        leatherMultiplier += (helmetItem.getItem() instanceof ArmorItem ? ((ArmorItem) helmetItem.getItem()).getDamageReduceAmount() : 0) * 2;
        leatherMultiplier += (chestplateItem.getItem() instanceof ArmorItem ? ((ArmorItem) chestplateItem.getItem()).getDamageReduceAmount() : 0) * 2;
        leatherMultiplier += (leggingsItem.getItem() instanceof ArmorItem ? ((ArmorItem) leggingsItem.getItem()).getDamageReduceAmount() : 0) * 2;
        leatherMultiplier += (bootsItem.getItem() instanceof ArmorItem ? ((ArmorItem) bootsItem.getItem()).getDamageReduceAmount() : 0) * 2;

        int helmetInsulation = getInsulatingArmor(helmetItem).insulationAmount;
        int chestInsulation = getInsulatingArmor(chestplateItem).insulationAmount;
        int legsInsulation = getInsulatingArmor(leggingsItem).insulationAmount;
        int bootsInsulation = getInsulatingArmor(bootsItem).insulationAmount;

        if (helmetItem.getOrCreateTag().getBoolean("insulated") || helmetInsulation > 0) {
            leatherMultiplier += helmetInsulation;
        }
        if (chestplateItem.getOrCreateTag().getBoolean("insulated") || chestInsulation > 0) {
            leatherMultiplier += chestInsulation;
        }
        if (leggingsItem.getOrCreateTag().getBoolean("insulated") || legsInsulation > 0) {
            leatherMultiplier += legsInsulation;
        }
        if (bootsItem.getOrCreateTag().getBoolean("insulated") || bootsInsulation > 0) {
            leatherMultiplier += bootsInsulation;
        }

        PlayerTemp.removeModifier(player, LeatherTempModifier.class, PlayerTemp.Types.RATE, 1);
        if (leatherMultiplier > 0)
            PlayerTemp.applyModifier(player, new LeatherTempModifier(), PlayerTemp.Types.RATE, false, IntNBT.valueOf(leatherMultiplier));
    }

    public static InsulatingItem getInsulatingArmor(ItemStack stack)
    {
        String id = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        for (List<String> s : ItemSettingsConfig.getInstance().insulatingArmor())
        {
            if (s.get(0).equals(id))
            {
                return new InsulatingItem(id, Integer.parseInt(s.get(1)));
            }
        }
        return new InsulatingItem(id, 0);
    }

    private static class InsulatingItem
    {
        public String id;
        public int insulationAmount;

        public InsulatingItem(String id, int insulationAmount)
        {
            this.id = id;
            this.insulationAmount = insulationAmount;
        }
    }
}