package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.core.init.InitDynamicTagsEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fires {@link InitDynamicTagsEvent} whenever a registry's tags are loaded.
 */
@Mixin(TagLoader.class)
public class MixinTagLoading
{
    /** The registry whose tags are currently being reloaded on this thread */
    @Unique
    private static final ThreadLocal<Registry<?>> COLD_SWEAT$LOADING_REGISTRY = new ThreadLocal<>();

    // Static/reloadable registries (blocks, items, etc.)
    @Inject(method = "loadPendingTags", at = @At("HEAD"))
    private static void storeLoadingRegistry(ResourceManager manager, Registry<?> registry, CallbackInfoReturnable<Optional<?>> cir)
    {   COLD_SWEAT$LOADING_REGISTRY.set(registry);
    }

    @Inject(method = "loadPendingTags", at = @At("RETURN"))
    private static void clearLoadingRegistry(ResourceManager manager, Registry<?> registry, CallbackInfoReturnable<Optional<?>> cir)
    {   COLD_SWEAT$LOADING_REGISTRY.remove();
    }

    @ModifyVariable(method = "wrapTags", at = @At("HEAD"), argsOnly = true)
    private static Map<Identifier, List<Holder<Object>>> addDynamicTags(Map<Identifier, List<Holder<Object>>> tags)
    {
        Registry<Object> registry = (Registry<Object>) COLD_SWEAT$LOADING_REGISTRY.get();
        if (registry == null) return tags;
        return InitDynamicTagsEvent.applyDynamicTags(registry, tags);
    }

    // Datapack (dynamic) registries (dimension types, biomes, etc.)
    @Mixin(RegistryLoadTask.class)
    public static abstract class DynamicRegistries<T>
    {
        @Shadow
        protected abstract Registry<T> readOnlyRegistry();

        @ModifyVariable(method = "registerTags", at = @At("HEAD"), argsOnly = true)
        private Map<TagKey<T>, List<Holder<T>>> addDynamicTags(Map<TagKey<T>, List<Holder<T>>> pendingTags)
        {   return InitDynamicTagsEvent.applyDynamicTagKeys(this.readOnlyRegistry(), pendingTags);
        }
    }
}
