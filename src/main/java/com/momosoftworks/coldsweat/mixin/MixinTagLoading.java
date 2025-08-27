package com.momosoftworks.coldsweat.mixin;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.api.event.core.init.InitDynamicTagsEvent;
import com.momosoftworks.coldsweat.data.tag.TagHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
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
import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public class MixinTagLoading
{
    @Shadow @Final private String directory;

    private static ResourceLocation CURRENT_TAG = null;

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

    @Inject(method = "build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;", at = @At("RETURN"), cancellable = true)
    private <T> void onBuildStart(TagEntry.Lookup<T> p_215979_, List<TagLoader.EntryWithSource> p_215980_, CallbackInfoReturnable<Either<Collection<TagLoader.EntryWithSource>, Collection<T>>> cir)
    {
        Either<Collection<TagLoader.EntryWithSource>, Collection<T>> list = cir.getReturnValue();
        if (list.left().isPresent()) return;
        if (TagHelper.EVENT_TAGS.isEmpty())
        {
            InitDynamicTagsEvent event = new InitDynamicTagsEvent(TagHelper.REGISTRY_ACCESS);
            MinecraftForge.EVENT_BUS.post(event);
            TagHelper.EVENT_TAGS.putAll(event.getTags());
        }

        Collection<T> newValues = getTagValues(this.directory);
        if (newValues.isEmpty()) return;
        cir.setReturnValue(Either.right(newValues));
    }

    private static <T> Collection<T> getTagValues(String directory)
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
        ResourceLocation tag = CURRENT_TAG;
        Collection<T> values = (Collection<T>) TagHelper.getTagValues(registry, tag);
        if (values.isEmpty() && endsWithS)
        {   return getTagValues(directory.substring(0, directory.length() - 1));
        }
        return values;
    }

    @Inject(method = "lambda$build$6(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/Map;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/tags/TagLoader$SortingEntry;)V", at = @At(value = "HEAD"), remap = false)
    private void onTagAdded(TagEntry.Lookup lookup, Map map, ResourceLocation tag, TagLoader.SortingEntry p_284683_, CallbackInfo ci)
    {   CURRENT_TAG = tag;
    }
}
