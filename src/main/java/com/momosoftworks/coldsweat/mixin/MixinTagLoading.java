package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.core.init.InitDynamicTagsEvent;
import com.momosoftworks.coldsweat.data.tag.TagHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

@Mixin(TagLoader.class)
public class MixinTagLoading
{
    private static String DIRECTORY = null;
    @Shadow @Final private String directory;

    private static final Field MANAGER_ACCESS = ObfuscationReflectionHelper.findField(TagManager.class, "f_144569_");
    static { MANAGER_ACCESS.setAccessible(true); }

    @Mixin(TagManager.class)
    public static final class Manager
    {
        @Shadow @Final private RegistryAccess registryAccess;

        @Inject(method = "createLoader", at = @At("HEAD"))
        private void onCreateLoader(CallbackInfoReturnable<TagLoader> cir)
        {   TagHelper.REGISTRY_ACCESS = this.registryAccess;
        }
    }

    @Inject(method = "build", at = @At("HEAD"))
    private <T> void onBuildStart(Map<ResourceLocation, Tag.Builder> builders, CallbackInfoReturnable<Map<ResourceLocation, Tag<T>>> cir)
    {
        DIRECTORY = this.directory;
        if (TagHelper.EVENT_TAGS.isEmpty())
        {
            InitDynamicTagsEvent event = new InitDynamicTagsEvent(TagHelper.REGISTRY_ACCESS);
            MinecraftForge.EVENT_BUS.post(event);
            TagHelper.EVENT_TAGS.putAll(event.getTags());
        }
    }

    @Inject(method = "lambda$build$10(Ljava/util/Map;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/tags/Tag;)V", at = @At("TAIL"))
    private static <T> void onTagBuild(Map<ResourceLocation, Tag<T>> map, ResourceLocation tagID, Tag<T> tag, CallbackInfo ci)
    {
        String directory = DIRECTORY;
        Collection<T> newValues = getTagValues(directory, tagID);
        if (!newValues.isEmpty())
        {   map.put(tagID, new Tag<>(newValues));
        }
    }

    private static <T> Collection<T> getTagValues(String directory, ResourceLocation tag)
    {
        directory = directory.replace("tags/", "");
        boolean endsWithS = directory.endsWith("s");
        String[] components = directory.split("/");
        ResourceLocation registry;
        if (ModList.get().getModFileById(components[0]) != null)
        {   registry = new ResourceLocation(components[0], directory.substring(directory.indexOf("/") + 1));
        }
        else
        {   registry = new ResourceLocation(directory);
        }
        Collection<T> values = (Collection<T>) TagHelper.getTagValues(registry, tag);
        if (values.isEmpty() && endsWithS)
        {   return getTagValues(directory.substring(0, directory.length() - 1), tag);
        }
        return values;
    }
}
