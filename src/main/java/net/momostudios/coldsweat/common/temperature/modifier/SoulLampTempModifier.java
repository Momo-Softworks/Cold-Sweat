package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.HandSide;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.util.registrylists.ModItems;
import net.momostudios.coldsweat.util.PlayerHelper;

public class SoulLampTempModifier extends TempModifier
{

    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        player.getPersistentData().putDouble("preLampTemp", temp.get());

        double max = ConfigCache.getInstance().maxTemp;
        double min = ConfigCache.getInstance().minTemp;

        if (holdingFueledLamp(player) && player.world.getDimensionKey().getLocation().getPath().equals("the_nether") && temp.get() > max)
        {
            if (!player.world.isRemote || (player != Minecraft.getInstance().player || Minecraft.getInstance().gameSettings.getPointOfView() != PointOfView.FIRST_PERSON))
            {
                if (PlayerHelper.holdingLamp(player, HandSide.RIGHT) && Math.random() < 0.2 + totalPlayerMotion(player) * 2)
                {
                    double randx = player.getPosX() + (player.getRNG().nextDouble() - 0.5) * 0.7;
                    double randy = player.getPosY() + (player.getRNG().nextDouble() - 0.5) * 0.7;
                    double randz = player.getPosZ() + (player.getRNG().nextDouble() - 0.5) * 0.7;
                    player.world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, randx + Math.cos(Math.toRadians(player.renderYawOffset + 120)) * 0.7,
                            randy + 0.5, randz + Math.sin(Math.toRadians(player.renderYawOffset + 120)) * 0.7, 0, 0.02 - Math.random() * 0.01, 0);
                }
                if (PlayerHelper.holdingLamp(player, HandSide.LEFT) && Math.random() < 0.2 + totalPlayerMotion(player) * 2)
                {
                    double randx = player.getPosX() + (player.getRNG().nextDouble() - 0.5) * 0.7;
                    double randy = player.getPosY() + (player.getRNG().nextDouble() - 0.5) * 0.7;
                    double randz = player.getPosZ() + (player.getRNG().nextDouble() - 0.5) * 0.7;
                    player.world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, randx + Math.cos(Math.toRadians(player.renderYawOffset + 50)) * 0.7,
                            randy + 0.5, randz + Math.sin(Math.toRadians(player.renderYawOffset + 50)) * 0.7, 0, 0.02 - Math.random() * 0.01, 0);
                }
            }
        }
        double mid = (max + min) / 2;
        return mid + (temp.get() - mid) * 0.4;
    }

    private double totalPlayerMotion(PlayerEntity player)
    {
        return (player.getMotion().x + player.getMotion().y + player.getMotion().z + Math.abs(player.renderYawOffset - player.prevRenderYawOffset) / 30) / 3;
    }

    private boolean holdingFueledLamp(PlayerEntity player)
    {
        ItemStack heldItem = player.getHeldItemMainhand();
        ItemStack offHandItem = player.getHeldItemOffhand();
        return (heldItem.getItem() == ModItems.SOULFIRE_LAMP && heldItem.getOrCreateTag().getFloat("fuel") > 0) ||
                (offHandItem.getItem() == ModItems.SOULFIRE_LAMP && offHandItem.getOrCreateTag().getFloat("fuel") > 0);
    }

    @Override
    public String getID() {
        return "cold_sweat:soulfire_lamp";
    }
}
