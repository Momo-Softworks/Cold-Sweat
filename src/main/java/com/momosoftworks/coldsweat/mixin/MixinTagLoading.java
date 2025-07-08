package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.core.init.InitDynamicTagsEvent;
import com.momosoftworks.coldsweat.data.tag.TagHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.*;

@Mixin(TagLoader.class)
public class MixinTagLoading
{
    private static RegistryAccess REGISTRY_ACCESS = null;

    private static final Field MANAGER_ACCESS = ObfuscationReflectionHelper.findField(TagManager.class, "f_144569_");
    static { MANAGER_ACCESS.setAccessible(true); }

    @Inject(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("HEAD"))
    private <T> void onBuildStart(Map<ResourceLocation, List<TagLoader.EntryWithSource>> map, CallbackInfoReturnable<Map<ResourceLocation, Collection<Holder<T>>>> cir)
    {
        if (map.isEmpty()) return;
        if (TagHelper.EVENT_TAGS.isEmpty())
        {
            if (REGISTRY_ACCESS == null)
            {
                TagManager tagManager = TagHelper.SERVER_RESOURCES.listeners().stream().filter(listener -> listener instanceof TagManager).map(l -> (TagManager) l).findFirst().orElse(null);
                try
                {   REGISTRY_ACCESS = (RegistryAccess) MANAGER_ACCESS.get(tagManager);
                }
                catch (Exception e)
                {   return;
                }
            }
            InitDynamicTagsEvent event = new InitDynamicTagsEvent(REGISTRY_ACCESS);
            MinecraftForge.EVENT_BUS.post(event);
            TagHelper.EVENT_TAGS.putAll(event.getTags());
        }
    }

    @Inject(method = "lambda$build$12(Ljava/util/Map;Lnet/minecraft/resources/ResourceLocation;Ljava/util/Collection;)V", at = @At(value = "TAIL"), remap = false)
    private static void onTagAdded(Map<ResourceLocation, Collection<?>> map, ResourceLocation tag, Collection<?> values, CallbackInfo ci)
    {
        if (map.isEmpty()) return;
        Collection<?> currentValues = map.get(tag);

        Object firstObj = map.values().stream().flatMap(Collection::stream).findFirst().orElse(null);
        if (!(firstObj instanceof Holder<?> firstHolder)) return;
        ResourceKey<?> registryKey = firstHolder.unwrapKey().orElse(null);
        Collection<?> newValues = TagHelper.getTagValues(registryKey.registry(), tag);

        map.put(tag, CSMath.append((Collection) currentValues, newValues));
    }
}
