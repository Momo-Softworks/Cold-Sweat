package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.EntityPickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MixinPickEntity
{
    @Redirect(method = "pickBlock",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPickedResult(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack getPickResult(Entity entity, HitResult hitResult)
    {
        if (hitResult.getType() == HitResult.Type.ENTITY)
        {
            EntityPickEvent event = new EntityPickEvent(entity, entity.getPickedResult(hitResult));
            NeoForge.EVENT_BUS.post(event);
            return event.getStack();
        }
        return ItemStack.EMPTY;
    }
}
