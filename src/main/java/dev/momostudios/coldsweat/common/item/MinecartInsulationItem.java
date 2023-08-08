package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.util.registries.ModBlocks;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class MinecartInsulationItem extends Item
{
    public MinecartInsulationItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
    {
        ItemStack itemStack = player.getItemInHand(hand);
        double reachDistance = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();

        Vector3d eye = player.getEyePosition(0);
        Vector3d look = eye.add(player.getLookAngle().scale(reachDistance));
        EntityRayTraceResult entityHitResult = ProjectileHelper.getEntityHitResult(world, player, eye, look,
                                                                                   new AxisAlignedBB(eye, look).inflate(1.0D),
                                                                                   (entity) -> {
                                                                                       return entity instanceof MinecartEntity;
                                                                                   });
        if (entityHitResult != null && entityHitResult.getType() == EntityRayTraceResult.Type.ENTITY)
        {
            if (entityHitResult.getEntity() instanceof MinecartEntity && ((MinecartEntity) entityHitResult.getEntity()).getDisplayBlockState().getBlock() != ModBlocks.MINECART_INSULATION)
            {
                MinecartEntity minecart = (MinecartEntity) entityHitResult.getEntity();
                if (!player.isCreative())
                {   player.getMainHandItem().shrink(1);
                }
                player.swing(Hand.MAIN_HAND, true);
                world.playSound(null, minecart.blockPosition(), SoundEvents.LLAMA_SWAG, SoundCategory.PLAYERS, 1f, (float) ((Math.random() / 5) + 0.9));
                minecart.setDisplayBlockState(ModBlocks.MINECART_INSULATION.defaultBlockState());
                minecart.setDisplayOffset(5);
                return ActionResult.sidedSuccess(itemStack, world.isClientSide());
            }
        }

        return ActionResult.pass(itemStack);
    }
}
