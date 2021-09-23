package net.momostudios.coldsweat.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.IntNBT;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.core.util.PlayerTemp;
import net.momostudios.coldsweat.common.temperature.modifier.LeatherTempModifier;

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
        if (helmetItem.getOrCreateTag().getBoolean("insulated") || helmetItem.getItem() == Items.LEATHER_HELMET ||
            chestplateItem.getOrCreateTag().getBoolean("insulated") || chestplateItem.getItem() == Items.LEATHER_CHESTPLATE ||
            leggingsItem.getOrCreateTag().getBoolean("insulated") || leggingsItem.getItem() == Items.LEATHER_LEGGINGS ||
            bootsItem.getOrCreateTag().getBoolean("insulated") || bootsItem.getItem() == Items.LEATHER_BOOTS)
        {
            leatherMultiplier += 20;
        }

        PlayerTemp.removeModifier(player, LeatherTempModifier.class, PlayerTemp.Types.RATE, 1);
        if (leatherMultiplier > 0)
            PlayerTemp.applyModifier(player, new LeatherTempModifier(), PlayerTemp.Types.RATE, false, IntNBT.valueOf(leatherMultiplier));
    }
}