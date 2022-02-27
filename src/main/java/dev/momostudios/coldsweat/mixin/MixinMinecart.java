package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public class MixinMinecart
{
    AbstractMinecart minecart = (AbstractMinecart) (Object) this;

    @Inject(method = "destroy(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At("HEAD"), remap = ColdSweat.remapMixins)
    public void killMinecart(DamageSource source, CallbackInfo ci)
    {
        if (minecart.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
        {
            ItemStack itemStack = minecart.getDisplayBlockState().getBlock().asItem().getDefaultInstance();
            minecart.level.addFreshEntity(new ItemEntity(minecart.level, minecart.getX(), minecart.getY(), minecart.getZ(), itemStack));
        }
    }
}
