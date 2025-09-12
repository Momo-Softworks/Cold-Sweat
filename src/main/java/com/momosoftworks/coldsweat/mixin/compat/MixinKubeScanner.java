package com.momosoftworks.coldsweat.mixin.compat;

import dev.latvian.mods.kubejs.registry.RegistryType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(RegistryType.Scanner.class)
public class MixinKubeScanner
{
    @Inject(method = "scan", at = @At("HEAD"),
            cancellable = true)
    private static void disableScan(ResourceLocation registryName, ResourceLocation location, CallbackInfo ci)
    {
        if (registryName == null || location == null)
        {   ci.cancel(); return;
        }
    }

    @Redirect(method = "scan", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private static boolean redirectContains(Set<String> set, Object o)
    {   return set.stream().anyMatch(ns -> ns.equals(o));
    }
}
