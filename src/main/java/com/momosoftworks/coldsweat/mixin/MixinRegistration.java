package com.momosoftworks.coldsweat.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.mixin_public.PublicMixinRegistration;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(RegistryOps.class)
public class MixinRegistration
{
     @Inject(method = "createAndLoad(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/core/RegistryAccess$Writable;Lnet/minecraft/server/packs/resources/ResourceManager;)Lnet/minecraft/resources/RegistryOps;", at = @At("HEAD"))
    private static <T> void captureResourceManager(DynamicOps<T> ops, RegistryAccess.Writable registryAccess, ResourceManager resourceManager, CallbackInfoReturnable<RegistryOps<T>> cir)
    {   PublicMixinRegistration.RESOURCE_MANAGER = resourceManager;
    }

    @Mixin(targets = "net.minecraft.resources.RegistryResourceAccess$1")
    public static final class Inner
    {
        @Inject(method = "listResources", at = @At("RETURN"), cancellable = true)
        private <E> void listResources(ResourceKey<? extends Registry<E>> registryKey, CallbackInfoReturnable<Collection<ResourceKey<E>>> cir)
        {
            if (registryKey.location().getNamespace().equals(ColdSweat.MOD_ID))
            {
                Collection<ResourceKey<E>> map = cir.getReturnValue();
                map.removeIf(entry ->
                {
                    ResourceLocation location = entry.location();
                    String absoluteLocation = String.format("%s/%s/%s.json", ColdSweat.MOD_ID, registryKey.location().getPath(), location.getPath());
                    try (Resource resource = PublicMixinRegistration.RESOURCE_MANAGER.getResource(new ResourceLocation(registryKey.location().getNamespace(), absoluteLocation));
                         Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                    {
                        JsonObject json = GsonHelper.parse(reader);
                        if (json.has("required_mods"))
                        {
                            JsonElement requiredModsField = json.get("required_mods");
                            NegatableList<String> requiredMods = NegatableList.listCodec(Codec.STRING).parse(JsonOps.INSTANCE, requiredModsField).result().orElse(new NegatableList<>());
                            if (!requiredMods.test(CompatManager::modLoaded))
                            {
                                ColdSweat.LOGGER.info("Skipping registration of {} {}: required mods not met", registryKey.location(), location);
                                return true;
                            }
                        }
                    }
                    catch (Exception e)
                    {   return false;
                    }
                    return false;
                });
                cir.setReturnValue(map);
            }
        }
    }
}
