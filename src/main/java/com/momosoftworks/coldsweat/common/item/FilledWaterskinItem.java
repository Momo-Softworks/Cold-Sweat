package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.api.temperature.modifier.WaterskinTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.serialization.ChatColors;
import com.momosoftworks.coldsweat.util.world.ItemHelper;
import com.momosoftworks.coldsweat.util.world.TaskScheduler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class FilledWaterskinItem extends Item
{
    public FilledWaterskinItem()
    {}

    private static final double EFFECT_RATE = 0.5;

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected)
    {
        super.onUpdate(stack, world, entity, slot, isSelected);
        if (entity.ticksExisted % 5 == 0 && entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;
            double itemTemp = ItemHelper.getOrCrateTag(stack).getDouble("temperature");
            if (itemTemp != 0 && slot <= 8)
            {
                double temp = (EFFECT_RATE / 20) * ConfigSettings.TEMP_RATE.get() * CSMath.getSign(itemTemp);
                double newTemp = itemTemp - temp * 2;
                if (CSMath.withinRange(newTemp, -1, 1)) newTemp = 0;

                ItemHelper.getOrCrateTag(stack).setDouble("temperature", newTemp);

                Temperature.addModifier(player, new WaterskinTempModifier(temp).expires(5), Temperature.Type.CORE, true);
            }
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        double amount = ItemHelper.getOrCrateTag(stack).getDouble("temperature") * (ConfigSettings.WATERSKIN_STRENGTH.get() / 50d);
        Temperature.addModifier(player, new WaterskinTempModifier(amount).expires(0), Temperature.Type.CORE, true);

        // Play empty sound
        world.playSoundEffect(player.posX, player.posY, player.posZ, "random.drink", 1, 1);

        // Create empty waterskin item
        ItemStack emptyStack = getEmpty(stack);

        // Add the item to the player's inventory
        if (player.inventory.hasItemStack(emptyStack))
        {   player.inventory.addItemStackToInventory(emptyStack);
            // clear the player's hand
            player.setCurrentItemOrArmor(0, ItemHelper.EMPTY_STACK);
        }
        else
        {   player.setCurrentItemOrArmor(0, emptyStack);
        }

        player.swingItem();

        // spawn falling water particles
        Random rand = new Random();
        for (int i = 0; i < 6; i++)
        {
            TaskScheduler.scheduleClient(() ->
            {
                for (int p = 0; p < rand.nextInt(5) + 5; p++)
                {
                    world.spawnParticle("splash",
                            player.posX + rand.nextFloat() * player.width - (player.width / 2),
                            player.posY + rand.nextFloat() * 0.5,
                            player.posZ + rand.nextFloat() * player.height - (player.height / 2), 0.3, 0.3, 0.3);
                }
            }, i);
        }
        player.extinguish();

        return emptyStack;
    }

    public static ItemStack getEmpty(ItemStack stack)
    {
        if (stack.getItem() instanceof FilledWaterskinItem)
        {
            ItemStack emptyWaterskin = new ItemStack(ModItems.WATERSKIN);

            // Preserve NBT (except temperature)
            emptyWaterskin.setTagCompound(ItemHelper.getOrCrateTag(stack));
            emptyWaterskin.getTagCompound().removeTag("temperature");
            return emptyWaterskin;
        }
        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced)
    {
        double temp = ItemHelper.getOrCrateTag(stack).getDouble("temperature");
        // Info tooltip for hotbar functionality
        tooltip.add("");
        tooltip.add(new ChatComponentTranslation("tooltip.cold_sweat.hotbar").setChatStyle(ChatColors.GRAY).getFormattedText());
        ChatStyle tempColor = temp > 0 ? ChatColors.RED : temp < 0 ? ChatColors.BLUE : ChatColors.WHITE;
        tooltip.add(new ChatComponentTranslation("tooltip.cold_sweat.temperature_effect",
                                                 (CSMath.getSign(temp) >= 0 ? "+" : "-")
                                               + (temp != 0 ? EFFECT_RATE * ConfigSettings.TEMP_RATE.get() : 0))
                            .setChatStyle(tempColor).getFormattedText());

        // Tooltip to display temperature
        boolean celsius = false;//ClientSettingsConfig.getInstance().isCelsius();
        String tempUnits = celsius ? "C" : "F";
        temp = temp / 2 + 95;
        if (celsius) temp = Temperature.convertUnits(temp, Temperature.Units.F, Temperature.Units.C, true);
        temp += 0;//ClientSettingsConfig.getInstance().getTempOffset() / 2.0;

        tooltip.add(1, new ChatComponentTranslation("item.cold_sweat.waterskin.filled").setChatStyle(ChatColors.GRAY).getFormattedText()
                     + " ("
                     + new ChatComponentText((int) temp + " \u00B0" + tempUnits).setChatStyle(tempColor).getFormattedText()
                     + ")");
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {   return super.getItemStackDisplayName(new ItemStack(ModItems.WATERSKIN));
    }
}
