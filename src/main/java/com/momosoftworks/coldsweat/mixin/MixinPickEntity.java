package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.EntityPickEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ForgeHooks.class)
public class MixinPickEntity
{
    @Redirect(method = "onPickBlock(Lnet/minecraft/world/phys/HitResult;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;)Z",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPickedResult(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/item/ItemStack;"), remap = false)
    private static ItemStack getPickResult(Entity entity, HitResult hitResult)
    {
        if (hitResult.getType() == HitResult.Type.ENTITY)
        {
            EntityPickEvent event = new EntityPickEvent(entity, entity.getPickedResult(hitResult));
            MinecraftForge.EVENT_BUS.post(event);
            return event.getStack();
        }
        return ItemStack.EMPTY;
    }
}
