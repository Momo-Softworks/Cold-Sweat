package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public class MixinMinecart
{
    AbstractMinecart minecart = (AbstractMinecart) (Object) this;

    @Inject(method = "destroy(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At("HEAD"), remap = ColdSweat.REMAP_MIXINS)
    public void destroy(DamageSource source, CallbackInfo ci)
    {
        Block block = minecart.getDisplayBlockState().getBlock();

        if (minecart.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)
        && block != Blocks.HOPPER
        && block != Blocks.CHEST
        && block != Blocks.TNT
        && block != Blocks.FURNACE)
        {
            ItemStack itemStack = new ItemStack(block.asItem());
            minecart.spawnAtLocation(itemStack);
        }
    }
}
