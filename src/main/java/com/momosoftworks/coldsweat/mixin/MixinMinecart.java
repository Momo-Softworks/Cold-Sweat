package com.momosoftworks.coldsweat.mixin;

import net.minecraft.server.level.ServerLevel;
import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.core.init.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VehicleEntity.class)
public class MixinMinecart
{
    VehicleEntity vehicle = (VehicleEntity) (Object) this;

    @Inject(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At
            (
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/vehicle/VehicleEntity;destroy(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V"
            ), cancellable = true)
    public void hurt(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci)
    {
        if (vehicle instanceof Minecart minecart)
        {
            ItemStack carryStack = minecart.getDisplayBlockState().getBlock().asItem().getDefaultInstance();
            if (!carryStack.isEmpty())
            {
                if (level.getGameRules().get(GameRules.ENTITY_DROPS))
                {
                    if (minecart.getDisplayBlockState().getBlock() == ModBlocks.MINECART_INSULATION.value())
                    {
                        ItemStack itemstack = new ItemStack(ModItems.INSULATED_MINECART.value());
                        if (minecart.hasCustomName())
                        {   itemstack.set(DataComponents.CUSTOM_NAME, minecart.getCustomName());
                        }
                        minecart.spawnAtLocation(level, itemstack);
                    }
                    else
                    {
                        ItemStack itemstack = new ItemStack(Items.MINECART);
                        if (minecart.hasCustomName())
                        {   itemstack.set(DataComponents.CUSTOM_NAME, minecart.getCustomName());
                        }
                        minecart.spawnAtLocation(level, itemstack);
                        minecart.spawnAtLocation(level, carryStack);
                    }
                }
                minecart.remove(Entity.RemovalReason.KILLED);
                ci.cancel();
            }
        }
    }
}
