package com.momosoftworks.coldsweat.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
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
                JsonElement requiredModsField = json.get("required_mods");
                NegatableList<String> requiredMods = NegatableList.listCodec(Codec.STRING).parse(JsonOps.INSTANCE, requiredModsField).result().orElse(new NegatableList<>());
                requiredMods.forEach(
                    req ->
                    {
                        if (ci.isCancelled()) return;
                        if (!CompatManager.modLoaded(req))
                        {   ColdSweat.LOGGER.info("Skipping registration of {} {}: missing mod \"{}\"", registry.key().location(), elementKey.location(), req);
                            ci.cancel();
                        }
                    },
                    exc ->
                    {
                        if (ci.isCancelled()) return;
                        if (CompatManager.modLoaded(exc))
                        {   ColdSweat.LOGGER.info("Skipping registration of {} {}: disallowed mod \"{}\" is loaded", registry.key().location(), elementKey.location(), exc);
                            ci.cancel();
                        }
                    });
            }
        }
    }
}
