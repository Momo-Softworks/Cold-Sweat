package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.util.registries.ModBlocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.ForgeMod;

public class MinecartInsulationItem extends Item
{
    public MinecartInsulationItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack itemStack = player.getItemInHand(hand);
        double reachDistance = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();

        Vec3 eye = player.getEyePosition();
        Vec3 look = eye.add(player.getLookAngle().scale(reachDistance));
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(level, player, eye, look, (new AABB(eye, look)).inflate(1.0D), (entity) -> {
            return entity instanceof Minecart;
        }, 0.0F);
        if (entityHitResult != null && entityHitResult.getType() == EntityHitResult.Type.ENTITY)
        {
            if (entityHitResult.getEntity() instanceof Minecart minecart && minecart.getDisplayBlockState().getBlock() != ModBlocks.MINECART_INSULATION)
            {
                if (!player.isCreative())
                {
                    player.getMainHandItem().shrink(1);
                }
                player.swing(InteractionHand.MAIN_HAND, true);
                level.playSound(null, minecart.blockPosition(), SoundEvents.LLAMA_SWAG, SoundSource.PLAYERS, 1f, (float) ((Math.random() / 5) + 0.9));
                minecart.setDisplayBlockState(ModBlocks.MINECART_INSULATION.defaultBlockState());
                minecart.setDisplayOffset(5);
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            }
        }

        return InteractionResultHolder.pass(itemStack);
    }
}
