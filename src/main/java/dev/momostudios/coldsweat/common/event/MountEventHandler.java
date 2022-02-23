package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.util.registrylists.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.common.temperature.modifier.MountTempModifier;
import dev.momostudios.coldsweat.config.EntitySettingsConfig;
import dev.momostudios.coldsweat.core.init.BlockInit;
import dev.momostudios.coldsweat.util.PlayerHelper;

import java.util.List;

@Mod.EventBusSubscriber
public class MountEventHandler
{
    @SubscribeEvent
    public static void onMinecartRightclick(PlayerInteractEvent.EntityInteract event)
    {
        Entity entity = event.getTarget();
        PlayerEntity sourceentity = event.getPlayer();
        if (event.getHand() != sourceentity.getActiveHand())
        {
            return;
        }
        double x = event.getPos().getX();
        double y = event.getPos().getY();
        double z = event.getPos().getZ();
        World world = event.getWorld();
        if (entity instanceof MinecartEntity && sourceentity.getHeldItemMainhand().getItem() == ModItems.MINECART_INSULATION)
        {
            event.setCanceled(true);
            if (!sourceentity.abilities.isCreativeMode)
            {
                sourceentity.getHeldItemMainhand().shrink(1);
            }
            sourceentity.swing(Hand.MAIN_HAND, true);
            world.playSound(null, new BlockPos(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.llama.swag")),
                SoundCategory.NEUTRAL, 1f, (float) ((Math.random() / 5) + 0.9));
            ((MinecartEntity) entity).setDisplayTile(BlockInit.MINECART_INSULATION.get().getDefaultState());
            ((MinecartEntity) entity).setDisplayTileOffset(5);
        }
    }

    @SubscribeEvent
    public static void playerRiding(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            PlayerEntity player = event.player;
            if (player.getRidingEntity() != null)
            {
                if (player.getRidingEntity() instanceof MinecartEntity && ((MinecartEntity) player.getRidingEntity()).getDisplayTile().getBlock() == BlockInit.MINECART_INSULATION.get())
                {
                    PlayerHelper.addModifier(player, new MountTempModifier(1).expires(1), PlayerHelper.Types.RATE, false);
                }
                else
                {
                    for (List<Object> entity : EntitySettingsConfig.INSTANCE.insulatedEntities())
                    {
                        if (ForgeRegistries.ENTITIES.getKey(player.getRidingEntity().getType()).toString().equals(entity.get(0)))
                        {
                            Number number = (Number) entity.get(1);
                            double value = number.doubleValue();
                            PlayerHelper.addModifier(player, new MountTempModifier(value).expires(1), PlayerHelper.Types.RATE, false);
                        }
                    }
                }
            }
        }
    }
}
