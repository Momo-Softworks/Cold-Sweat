package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.HellLampTempModifier;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import dev.momostudios.coldsweat.util.entity.TempHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class HellspringLampItem extends Item
{
    public HellspringLampItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (entityIn instanceof Player && !worldIn.isClientSide)
        {
            Player player = (Player) entityIn;
            double max = ConfigCache.getInstance().maxTemp;
            double temp = TempHelper.getTemperature(player, Temperature.Types.WORLD).get();

            boolean validDimension = false;
            for (String id : ItemSettingsConfig.getInstance().hellLampDimensions())
            {
                if (worldIn.dimension().location().toString().equals(id))
                {
                    validDimension = true;
                    break;
                }
            }

            if ((isSelected || player.getOffhandItem() == stack) && validDimension && temp > max && getFuel(stack) > 0)
            {
                // Drain fuel
                if (player.tickCount % 10 == 0 && !(player.isCreative() || player.isSpectator()))
                    addFuel(stack, -0.02d * CSMath.clamp(temp - ConfigCache.getInstance().maxTemp, 1d, 3d));

                // Give effect to nearby players
                AABB bb = new AABB(player.getX() - 3.5, player.getY() - 3.5, player.getZ() - 3.5, player.getX() + 3.5, player.getY() + 3.5, player.getZ() + 3.5);
                worldIn.getEntitiesOfClass(Player.class, bb).forEach(e ->
                {
                    TempHelper.insertModifier(e, new HellLampTempModifier().expires(5), Temperature.Types.MAX);
                });

                // If the conditions are met, turn on the lamp
                if (stack.getOrCreateTag().getInt("stateChangeTimer") <= 0 && !stack.getOrCreateTag().getBoolean("isOn"))
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", true);

                    WorldHelper.playEntitySound(ModSounds.NETHER_LAMP_ON, SoundSource.PLAYERS, player, 1.5f, (float) Math.random() / 5f + 0.9f);
                }
            }
            // If the conditions are not met, turn off the lamp
            else
            {
                if (stack.getOrCreateTag().getInt("stateChangeTimer") <= 0 && stack.getOrCreateTag().getBoolean("isOn"))
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", false);

                    if (getFuel(stack) < 0.5)
                        setFuel(stack, 0);

                    WorldHelper.playEntitySound(ModSounds.NETHER_LAMP_OFF, SoundSource.PLAYERS, player, 1.5f, (float) Math.random() / 5f + 0.9f);
                }
            }

            // Decrement the state change timer
            NBTHelper.incrementTag(stack, "stateChangeTimer", -1, tag -> tag > 0);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }

    private void setFuel(ItemStack stack, double fuel)
    {
        stack.getOrCreateTag().putDouble("fuel", fuel);
    }
    private void addFuel(ItemStack stack, double fuel)
    {
        setFuel(stack, getFuel(stack) + fuel);
    }
    private double getFuel(ItemStack stack)
    {
        return stack.getOrCreateTag().getDouble("fuel");
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> itemList) {
        if (this.allowdedIn(tab))
        {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().putBoolean("isOn", true);
            stack.getOrCreateTag().putDouble("fuel", 64);
            itemList.add(stack);
        }

    }
}
