package net.momostudios.coldsweat.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.momostudios.coldsweat.common.temperature.modifier.SoulLampTempModifier;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.RequestSoundMessage;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.PlayerHelper;
import net.momostudios.coldsweat.core.util.PlayerTemp;
import net.momostudios.coldsweat.core.util.registrylists.ModSounds;

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
            double max = ConfigCache.getInstance().maxTemp;
            double temp = PlayerTemp.hasModifier(player, SoulLampTempModifier.class, PlayerTemp.Types.AMBIENT) ?
                    player.getPersistentData().getDouble("preLampTemp") : PlayerTemp.getTemperature(player, PlayerTemp.Types.AMBIENT).get();

            // Fuel the item on creation
            if (!stack.getOrCreateTag().getBoolean("hasTicked"))
            {
                stack.getOrCreateTag().putBoolean("hasTicked", true);
                setFuel(stack, 64);
            }

            if ((isSelected || player.getHeldItemOffhand() == stack) && player.world.getDimensionKey().getLocation().getPath().equals("the_nether") &&
            temp > ConfigCache.getInstance().maxTemp)
            {
                if (getFuel(stack) > 0)
                {
                    // Drain fuel
                    if (player.ticksExisted % 10 == 0 && !(player.isCreative() || player.isSpectator()))
                        addFuel(stack, -0.02f * (float) MathHelperCS.clamp(temp - ConfigCache.getInstance().maxTemp, 1, 3));

                    // Give effect to nearby players
                    AxisAlignedBB bb = new AxisAlignedBB(player.getPosX() - 2, player.getPosY() - 2, player.getPosZ() - 2, player.getPosX() + 2, player.getPosY() + 2, player.getPosZ() + 2);
                    worldIn.getEntitiesWithinAABB(PlayerEntity.class, bb).forEach(e ->
                    {
                        PlayerTemp.addModifier(e, new SoulLampTempModifier(), PlayerTemp.Types.AMBIENT, false);

                        e.getPersistentData().putInt("soulLampTimeout", 5);
                    });
                }
            }

            // Handle state changes & sounds
            if (stack.getOrCreateTag().getInt("stateChangeTimer") > 0)
            {
                stack.getOrCreateTag().putInt("stateChangeTimer", stack.getOrCreateTag().getInt("stateChangeTimer") - 1);
            }

            if (stack.getOrCreateTag().getInt("fuel") > 0 && player.world.getDimensionKey().getLocation().getPath().equals("the_nether") && temp > max &&
            (isSelected || player.getHeldItemOffhand() == stack))
            {
                if (stack.getOrCreateTag().getInt("stateChangeTimer") <= 0 && !stack.getOrCreateTag().getBoolean("isOn"))
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
                if (stack.getOrCreateTag().getInt("stateChangeTimer") <= 0 && stack.getOrCreateTag().getBoolean("isOn"))
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", false);
                    player.world.playMovingSound(null, player, ModSounds.SOUL_LAMP_OFF, SoundCategory.PLAYERS, (float) Math.random() / 5f + 0.9f, 2F);

                    if (getFuel(stack) < 0.5)
                        setFuel(stack, 0);

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
        setFuel(stack, getFuel(stack) + fuel);
    }
    private float getFuel(ItemStack stack)
    {
        return stack.getOrCreateTag().getFloat("fuel");
    }
}
