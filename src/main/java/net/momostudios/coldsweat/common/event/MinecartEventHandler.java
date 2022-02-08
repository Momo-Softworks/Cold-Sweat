package net.momostudios.coldsweat.common.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.common.temperature.modifier.MinecartTempModifier;
import net.momostudios.coldsweat.core.init.BlockInit;
import net.momostudios.coldsweat.util.PlayerHelper;
import net.momostudios.coldsweat.util.registrylists.ModItems;

@Mod.EventBusSubscriber
public class MinecartEventHandler
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
    public static void onMinecartBroken(EntityLeaveWorldEvent event)
    {
        if (event != null && event.getEntity() != null)
        {
            Entity entity = event.getEntity();
            double x = entity.getPosX();
            double y = entity.getPosY();
            double z = entity.getPosZ();
            World world = entity.world;
            if (entity instanceof MinecartEntity && ((MinecartEntity) entity).getDisplayTile().getBlock() == BlockInit.MINECART_INSULATION.get())
            {
                if (world instanceof World && !world.isRemote())
                {
                    ItemEntity entityToSpawn = new ItemEntity(world, x, y, z, new ItemStack(ModItems.MINECART_INSULATION, 1));
                    entityToSpawn.setPickupDelay(10);
                    world.addEntity(entityToSpawn);
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerRidingMinecart(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            PlayerEntity player = event.player;
            if (player.getRidingEntity() instanceof MinecartEntity && ((MinecartEntity) player.getRidingEntity()).getDisplayTile().getBlock() == BlockInit.MINECART_INSULATION.get())
            {
                PlayerHelper.addModifier(player, new MinecartTempModifier(), PlayerHelper.Types.RATE, false);
            }
        }
    }
}
