package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public class MixinMinecart
{
    AbstractMinecart minecart = (AbstractMinecart) (Object) this;

    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At
            (
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;destroy(Lnet/minecraft/world/damagesource/DamageSource;)V"
            ), cancellable = true)
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci)
    {
        if (minecart instanceof Minecart)
        {
            ItemStack carryStack = minecart.getDisplayBlockState().getBlock().asItem().getDefaultInstance();
            if (!carryStack.isEmpty())
            {
                if (minecart.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
                {
                    if (minecart.getDisplayBlockState().getBlock() == ModBlocks.MINECART_INSULATION)
                    {
                        ItemStack itemstack = new ItemStack(ModItems.INSULATED_MINECART);
                        if (minecart.hasCustomName())
                        {   itemstack.setHoverName(minecart.getCustomName());
                        }
                        minecart.spawnAtLocation(itemstack);
                    }
                    else
                    {
                        ItemStack itemstack = new ItemStack(Items.MINECART);
                        if (minecart.hasCustomName())
                        {
                            itemstack.setHoverName(minecart.getCustomName());
                        }
                        minecart.spawnAtLocation(itemstack);
                        minecart.spawnAtLocation(carryStack);
                    }
                }
                minecart.remove(Entity.RemovalReason.KILLED);
                ci.cancel();
            }
        }
    }
}
