package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public class MixinMinecart
{
    AbstractMinecart minecart = (AbstractMinecart) (Object) this;

    @Inject(method = "destroy(Lnet/minecraft/world/damagesource/DamageSource;)V",
            at = @At("HEAD"), remap = ColdSweat.REMAP_MIXINS, cancellable = true)
    public void destroy(DamageSource p_38115_, CallbackInfo ci)
    {
        ItemStack carryStack = minecart.getDisplayBlockState().getBlock().asItem().getDefaultInstance();
        if (!carryStack.isEmpty())
        {
            minecart.remove(Entity.RemovalReason.KILLED);
            if (minecart.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
            {
                ItemStack itemstack = new ItemStack(Items.MINECART);
                if (minecart.hasCustomName())
                {
                    itemstack.setHoverName(minecart.getCustomName());
                }

                minecart.spawnAtLocation(itemstack);
                minecart.spawnAtLocation(carryStack);
            }
            ci.cancel();
        }
    }
}
