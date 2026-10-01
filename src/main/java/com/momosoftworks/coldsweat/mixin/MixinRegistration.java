package com.momosoftworks.coldsweat.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.StrictJsonParser;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.Reader;

@Mixin(targets = "net.minecraft.resources.RegistryLoadTask$PendingRegistration")
public class MixinRegistration
{
    /**
     * Prevents CS registry elements from loading if their "required_mods" aren't met.<br>
     * Such elements are given a NeoForge "never" condition, so they're skipped the same way as other conditional elements.
     */
    @Redirect(method = "loadFromResource", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StrictJsonParser;parse(Ljava/io/Reader;)Lcom/google/gson/JsonElement;"))
    private static JsonElement checkRequiredMods(Reader reader, Decoder<?> elementDecoder, RegistryOps<JsonElement> ops, ResourceKey<?> elementKey, Resource thunk) throws IOException
    {
        JsonElement jsonElement = StrictJsonParser.parse(reader);
        if (elementKey.identifier().getNamespace().equals(ColdSweat.MOD_ID) && jsonElement.isJsonObject())
        {
            JsonObject json = jsonElement.getAsJsonObject();
            if (json.has("required_mods"))
            {
                NegatableList<String> requiredMods = ConfigData.REQUIRED_MODS_CODEC.parse(JsonOps.INSTANCE, json.get("required_mods")).result().orElse(new NegatableList<>());
                if (!requiredMods.test(CompatManager::modLoaded))
                {
                    ColdSweat.LOGGER.info("Skipping registration of {}: required mods not met", elementKey.identifier());
                    JsonObject never = new JsonObject();
                    never.addProperty("type", "neoforge:never");
                    JsonArray conditions = new JsonArray();
                    conditions.add(never);
                    json.add(ConditionalOps.DEFAULT_CONDITIONS_KEY, conditions);
                }
            }
        }
        return jsonElement;
    }
}
