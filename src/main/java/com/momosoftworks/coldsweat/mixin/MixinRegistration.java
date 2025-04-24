package com.momosoftworks.coldsweat.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Decoder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.compat.CompatManager;
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
                JsonArray conditions = json.getAsJsonArray("forge:conditions");
                // Create conditions block if it doesn't exist
                if (conditions == null)
                {   conditions = new JsonArray();
                    json.add("forge:conditions", conditions);
                }
                // Add required mods as forge conditions
                for (JsonElement requiredMod : requiredMods)
                {
                    JsonObject condition = new JsonObject();
                    condition.addProperty("type", "forge:mod_loaded");
                    condition.addProperty("modid", requiredMod.getAsString());
                    conditions.add(condition);
                }
                // Add excluded mods as an impossible forge condition
                for (JsonElement excludedMod : excludedMods)
                {
                    if (!CompatManager.modLoaded(excludedMod.getAsString()))
                    {
                        JsonObject condition = new JsonObject();
                        condition.addProperty("type", "forge:mod_loaded");
                        condition.addProperty("modid", "cs_impossible");
                        conditions.add(condition);
                        break;
                    }
                }
            }
        }
    }
}
