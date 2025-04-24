package com.momosoftworks.coldsweat.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.mixin_public.PublicMixinRegistration;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.Reader;
import java.util.Map;

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
        @Inject(method = "listResources(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Map;", at = @At("RETURN"), cancellable = true)
        private <E> void listResources(ResourceKey<? extends Registry<E>> registryKey, CallbackInfoReturnable<Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>>> cir)
        {
            if (registryKey.location().getNamespace().equals(ColdSweat.MOD_ID))
            {
                Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>> map = cir.getReturnValue();
                map.entrySet().removeIf(entry ->
                {
                    ResourceLocation location = entry.getKey().location();
                    String absoluteLocation = String.format("%s/%s/%s.json", ColdSweat.MOD_ID, registryKey.location().getPath(), location.getPath());
                    try (Reader reader = PublicMixinRegistration.RESOURCE_MANAGER.openAsReader(new ResourceLocation(registryKey.location().getNamespace(), absoluteLocation)))
                    {
                        JsonObject json = GsonHelper.parse(reader);
                        if (json.has("required_mods"))
                        {
                            JsonArray requiredMods = new JsonArray();
                            JsonArray excludedMods = new JsonArray();
                            JsonElement requiredModField = json.get("required_mods");
                            if (requiredModField.isJsonArray())
                            {
                                requiredMods = requiredModField.getAsJsonArray();
                            }
                            else
                            {
                                JsonObject requiredModCompound = requiredModField.getAsJsonObject();
                                if (requiredModCompound.has("require"))
                                {   requiredMods = requiredModCompound.getAsJsonArray("require");
                                }
                                if (requiredModCompound.has("exclude"))
                                {   excludedMods = requiredModCompound.getAsJsonArray("exclude");
                                }
                            }
                            for (JsonElement requiredMod : requiredMods)
                            {
                                if (!CompatManager.modLoaded(requiredMod.getAsString()))
                                {   ColdSweat.LOGGER.warn("Skipping registration of {} {}: missing mod \"{}\"", registryKey.location(), location, requiredMod.getAsString());
                                    return true;
                                }
                            }
                            for (JsonElement excludedMod : excludedMods)
                            {
                                if (CompatManager.modLoaded(excludedMods.getAsString()))
                                {   ColdSweat.LOGGER.warn("Skipping registration of {} {}: disallowed mod \"{}\" is loaded", registryKey.location(), location, excludedMod.getAsString());
                                    return true;
                                }
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
