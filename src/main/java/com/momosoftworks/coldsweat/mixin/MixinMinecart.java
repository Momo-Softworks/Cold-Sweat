package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public class MixinMinecart
{
    AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;

    @Inject(method = "hurt(Lnet/minecraft/util/DamageSource;F)Z",
            at = @At
            (
                value = "INVOKE",
                target = "Lnet/minecraft/entity/item/minecart/AbstractMinecartEntity;destroy(Lnet/minecraft/util/DamageSource;)V"
            ), cancellable = true)
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci)
    {
        if (minecart instanceof MinecartEntity)
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
                minecart.remove();
                ci.cancel();
            }
        }
    }
}
