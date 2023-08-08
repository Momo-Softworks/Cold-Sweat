package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.api.event.common.EntityPickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ForgeHooks.class)
public class MixinPickEntity
{
    @Redirect(method = "onPickBlock(Lnet/minecraft/util/math/RayTraceResult;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;)Z",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPickedResult(Lnet/minecraft/util/math/RayTraceResult;)Lnet/minecraft/item/ItemStack;"), remap = false)
    private static ItemStack getPickResult(Entity entity, RayTraceResult hitResult)
    {
        if (hitResult.getType() == RayTraceResult.Type.ENTITY)
        {
            EntityPickEvent event = new EntityPickEvent(entity, entity.getPickedResult(hitResult));
            MinecraftForge.EVENT_BUS.post(event);
            return event.getStack();
        }
        return ItemStack.EMPTY;
    }
}
