package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.LivingEntityLoadAdditionalEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingLoad
{
    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void postLivingLoadEvent(CompoundTag nbt, CallbackInfo ci)
    {
        LivingEntityLoadAdditionalEvent event = new LivingEntityLoadAdditionalEvent((LivingEntity)(Object)this, nbt);
        NeoForge.EVENT_BUS.post(event);
    }
}
