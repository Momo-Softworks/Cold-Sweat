package net.momostudios.coldsweat.common.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.momostudios.coldsweat.client.event.AmbientGaugeDisplay;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.common.temperature.modifier.SoulLampTempModifier;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.RequestSoundMessage;
import net.momostudios.coldsweat.core.util.PlayerTemp;
import net.momostudios.coldsweat.core.util.registrylists.ModSounds;

import java.util.ArrayList;
import java.util.List;

public class SoulfireLampItem extends Item
{
    public SoulfireLampItem()
    {
        super(new Properties().group(ColdSweatGroup.COLD_SWEAT).maxStackSize(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (entityIn instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) entityIn;
            double min = ConfigCache.getInstance().minTemp;
            double max = ConfigCache.getInstance().maxTemp;
            double temp1 = PlayerTemp.getTemperature((PlayerEntity) entityIn, PlayerTemp.Types.AMBIENT).get();
            double temp = -2.5 * (-temp1 - 0.5 * (-min-max) -0.4 * max);

            if (!stack.getOrCreateTag().getBoolean("hasTicked"))
            {
                stack.getOrCreateTag().putBoolean("hasTicked", true);
                setFuel(stack, 64);
            }

            if ((isSelected || player.getHeldItemOffhand() == stack) && player.world.getDimensionKey().getLocation().getPath().equals("the_nether") &&
            temp > ColdSweatConfig.getInstance().getMaxTempHabitable() && player.ticksExisted % 10 == 0 && !(player.isCreative() || player.isSpectator()))
            {
                if (getFuel(stack) > 0)
                {
                    addFuel(stack, -0.02f * (float) Math.min(3, Math.max(1, (temp - ColdSweatConfig.getInstance().getMaxTempHabitable()))));
                }
            }

            if (stack.getOrCreateTag().getInt("stateChangeTimer") > 0)
            {
                stack.getOrCreateTag().putInt("stateChangeTimer", stack.getOrCreateTag().getInt("stateChangeTimer") - 1);
            }

            if (stack.getOrCreateTag().getInt("fuel") > 0 && player.world.getDimensionKey().getLocation().getPath().equals("the_nether") &&
                    -2.5 * (-AmbientGaugeDisplay.clientTemp - 0.5 * (-min - max) -0.4 * max) > max)
            {
                if (stack.getOrCreateTag().getInt("stateChangeTimer") == 0 && !stack.getOrCreateTag().getBoolean("isOn"))
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", true);
                    player.world.playMovingSound(null, player, ModSounds.SOUL_LAMP_ON, SoundCategory.PLAYERS, (float) Math.random() / 5f + 0.9f, 2F);

                    // In case the player is on a server
                    if (player instanceof ServerPlayerEntity)
                        ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new RequestSoundMessage(1));
                }
            }
            else
            {
                if (stack.getOrCreateTag().getInt("stateChangeTimer") == 0 && stack.getOrCreateTag().getBoolean("isOn"))
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", false);
                    player.world.playMovingSound(null, player, ModSounds.SOUL_LAMP_OFF, SoundCategory.PLAYERS, (float) Math.random() / 5f + 0.9f, 2F);

                    // In case the player is on a server
                    if (player instanceof ServerPlayerEntity)
                        ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new RequestSoundMessage(2));
                }
            }
        }
    }

    private void setFuel(ItemStack stack, float fuel)
    {
        stack.getOrCreateTag().putFloat("fuel", fuel);
    }
    private void addFuel(ItemStack stack, float fuel)
    {
        stack.getOrCreateTag().putFloat("fuel", stack.getOrCreateTag().getFloat("fuel") + fuel);
    }
    private float getFuel(ItemStack stack)
    {
        return stack.getOrCreateTag().getFloat("fuel");
    }
}
