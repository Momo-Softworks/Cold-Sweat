package com.momosoftworks.coldsweat.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

@Mixin(RegistryDataLoader.class)
public class MixinRegistration
{
    /**
     * Injects into the datapack loading process to prevent CS registry elements from loading if their "required_mods" aren't met.
     */
    @Inject(method = "loadRegistryContents", at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonParser;parseReader(Ljava/io/Reader;)Lcom/google/gson/JsonElement;", shift = At.Shift.BY, by = 2),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static <E> void loadRegistryContents(RegistryOps.RegistryInfoLookup pLookup, ResourceManager pManager, ResourceKey<? extends Registry<E>> pRegistryKey,
                                                 WritableRegistry<E> pRegistry, Decoder<E> pDecoder, Map<ResourceKey<?>, Exception> pExceptions, CallbackInfo ci,
                                                 // locals
                                                 String s, FileToIdConverter filetoidconverter, RegistryOps<JsonElement> registryops, Iterator iterator,
                                                 Map.Entry<ResourceLocation, Resource> entry, ResourceLocation resourcelocation, ResourceKey<E> resourcekey, Resource resource, Reader reader, JsonElement jsonelement)
    {
        if (pRegistryKey.location().getNamespace().equals(ColdSweat.MOD_ID))
        {
            JsonObject json = jsonelement.getAsJsonObject();
            if (json.has("required_mods"))
            {
                JsonElement requiredModsField = json.get("required_mods");
                NegatableList<String> requiredMods = ConfigData.REQUIRED_MODS_CODEC.parse(JsonOps.INSTANCE, requiredModsField).result().orElse(new NegatableList<>());
                if (!json.has("forge:conditions"))
                {   json.add("forge:conditions", new JsonArray());
                }
                JsonArray conditions = json.getAsJsonArray("forge:conditions");
                if (!requiredMods.test(CompatManager::modLoaded))
                {
                    JsonObject condition = new JsonObject();
                    condition.addProperty("type", "forge:mod_loaded");
                    condition.addProperty("modid", "cs_impossible");
                    conditions.add(condition);
                    ColdSweat.LOGGER.info("Skipping registration of {} {}: required mods not met", pRegistryKey.location(), resourcelocation);
                }
            }
        }
    }
}
