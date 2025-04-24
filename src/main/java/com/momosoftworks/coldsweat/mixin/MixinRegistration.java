package com.momosoftworks.coldsweat.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Decoder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.compat.CompatManager;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.Reader;
import java.util.Optional;

@Mixin(RegistryDataLoader.class)
public class MixinRegistration
{
    /**
     * Injects into the datapack loading process to prevent CS registry elements from loading if their "required_mods" aren't met.
     */
    @Inject(method = "loadElementFromResource", at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonParser;parseReader(Ljava/io/Reader;)Lcom/google/gson/JsonElement;", shift = At.Shift.BY, by = 2),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static <E> void loadRegistryContents(WritableRegistry<E> registry, Decoder<E> codec, RegistryOps<JsonElement> registryOps,
                                                 ResourceKey<E> elementKey, Resource resource, RegistrationInfo registrationInfo, CallbackInfo ci,
                                                 // locals
                                                 Decoder<Optional<E>> decoder, Reader reader, JsonElement jsonElement)
    {
        if (elementKey.location().getNamespace().equals(ColdSweat.MOD_ID))
        {
            JsonObject json = jsonElement.getAsJsonObject();
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
                // Add required mods as forge conditions
                for (JsonElement requiredMod : requiredMods)
                {
                    if (!CompatManager.modLoaded(requiredMod.getAsString()))
                    {   ColdSweat.LOGGER.info("Skipping registration of {} {}: missing mod \"{}\"", registry.key().location(), elementKey.location(), requiredMod.getAsString());
                        ci.cancel();
                        return;
                    }
                }
                for (JsonElement excludedMod : excludedMods)
                {
                    if (CompatManager.modLoaded(excludedMod.getAsString()))
                    {   ColdSweat.LOGGER.info("Skipping registration of {} {}: disallowed mod \"{}\" is loaded", registry.key().location(), elementKey.location(), excludedMod.getAsString());
                        ci.cancel();
                        return;
                    }
                }
            }
        }
    }
}
