package com.momosoftworks.coldsweat.mixin;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.api.event.core.init.InitDynamicTagsEvent;
import com.momosoftworks.coldsweat.mixin_interface.RegistryTagLoader;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TagLoader.class)
public class MixinTagLoading<T> implements RegistryTagLoader<T>
{
    @Unique
    private Registry<T> registry;
    @Unique
    private final Map<ResourceLocation, Collection<Holder<T>>> tags = new HashMap<>();

    @Mixin(TagManager.class)
    public static final class Manager
    {
        @Inject(method = "createLoader", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
        public <V> void injectLoaderRegistry(ResourceManager pResourceManager, Executor pBackgroundExecutor, RegistryAccess.RegistryEntry<V> pEntry, CallbackInfoReturnable<CompletableFuture<TagManager.LoadResult<V>>> cir,
                                             // locals
                                             ResourceKey<? extends Registry<V>> resourcekey, Registry<V> registry, TagLoader<Holder<V>> tagloader)
        {
            ((RegistryTagLoader) tagloader).setRegistry(registry);
        }
    }

    @Override
    public Registry<T> getRegistry()
    {   return registry;
    }
    @Override
    public void setRegistry(Registry<T> registry)
    {   this.registry = registry;
    }
    @Override
    public Map<ResourceLocation, Collection<Holder<T>>> getTags()
    {   return tags;
    }

    @Inject(method = "build", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onBuildStart(Map<ResourceLocation, Tag.Builder> builders, CallbackInfoReturnable<Map<ResourceLocation, Tag<T>>> cir,
                              // locals
                              Map<ResourceLocation, Tag<Holder<T>>> map)
    {
        if (this.registry == null) return;
        if (tags.isEmpty())
        {
            InitDynamicTagsEvent<T> event = new InitDynamicTagsEvent<>(registry);
            MinecraftForge.EVENT_BUS.post(event);
            tags.putAll(event.getTags());
        }

        tags.forEach((tagID, values) ->
        {
            Tag<Holder<T>> tag = map.get(tagID);
            if (tag != null)
            {   map.put(tagID, new Tag<>(values));
            }
        });
    }
}
