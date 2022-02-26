package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.core.network.message.PlaySoundMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import dev.momostudios.coldsweat.common.temperature.modifier.HellLampTempModifier;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.util.CSMath;
import dev.momostudios.coldsweat.util.PlayerHelper;
import net.minecraftforge.network.PacketDistributor;

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
            double temp = PlayerHelper.hasModifier(player, HellLampTempModifier.class, PlayerHelper.Types.AMBIENT) ?
                    player.getPersistentData().getDouble("preLampTemp") : PlayerHelper.getTemperature(player, PlayerHelper.Types.AMBIENT).get();

            // Fuel the item on creation
            if (!stack.getOrCreateTag().getBoolean("hasTicked"))
            {
                stack.getOrCreateTag().putBoolean("hasTicked", true);
                setFuel(stack, 64);
            }

            boolean validDimension = false;
            for (String id : ItemSettingsConfig.getInstance().hellLampDimensions())
            {
                if (worldIn.dimension().toString().equals(id))
                {
                    validDimension = true;
                    break;
                }
            }

            if ((isSelected || player.getOffhandItem() == stack) && validDimension && temp > max)
            {
                if (getFuel(stack) > 0)
                {
                    // Drain fuel
                    if (player.tickCount % 10 == 0 && !(player.isCreative() || player.isSpectator()))
                        addFuel(stack, -0.02f * (float) CSMath.clamp(temp - ConfigCache.getInstance().maxTemp, 1, 3));

                    // Give effect to nearby players
                    AABB bb = new AABB(player.getX() - 2, player.getY() - 2, player.getZ() - 2, player.getX() + 2, player.getY() + 2, player.getZ() + 2);
                    worldIn.getEntitiesOfClass(Player.class, bb).forEach(e ->
                    {
                        PlayerHelper.addModifier(e, new HellLampTempModifier(), PlayerHelper.Types.AMBIENT, false);

                        e.getPersistentData().putInt("soulLampTimeout", 5);
                    });
                }
            }

            // Handle state changes & sounds
            if (stack.getOrCreateTag().getInt("stateChangeTimer") > 0)
            {
                stack.getOrCreateTag().putInt("stateChangeTimer", stack.getOrCreateTag().getInt("stateChangeTimer") - 1);
            }

            if (stack.getOrCreateTag().getInt("fuel") > 0 && validDimension && temp > max &&
            (isSelected || player.getOffhandItem() == stack))
            {
                if (stack.getOrCreateTag().getInt("stateChangeTimer") <= 0 && !stack.getOrCreateTag().getBoolean("isOn"))
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", true);

                    ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new PlaySoundMessage(1, 1.5f, (float) Math.random() / 5f + 0.9f, player.getUUID()));
                }
            }
            else
            {
                if (stack.getOrCreateTag().getInt("stateChangeTimer") <= 0 && stack.getOrCreateTag().getBoolean("isOn"))
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", false);

                    if (getFuel(stack) < 0.5)
                        setFuel(stack, 0);

                    ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new PlaySoundMessage(2, 1.5f, (float) Math.random() / 5f + 0.9f, player.getUUID()));
                }
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }

    private void setFuel(ItemStack stack, float fuel)
    {
        stack.getOrCreateTag().putFloat("fuel", fuel);
    }
    private void addFuel(ItemStack stack, float fuel)
    {
        setFuel(stack, getFuel(stack) + fuel);
    }
    private float getFuel(ItemStack stack)
    {
        return stack.getOrCreateTag().getFloat("fuel");
    }
}
