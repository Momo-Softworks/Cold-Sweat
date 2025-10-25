package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.api.event.vanilla.ServerConfigsLoadedEvent;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class RegistryHelper
{
    private static RegistryAccess REGISTRY_ACCESS = null;

    @Nullable
    public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> registry)
    {   return CSMath.getIfNotNull(getRegistryAccess(), access -> access.registryOrThrow(registry), null);
    }

    @Mod.EventBusSubscriber
    public static class GetAccessServer
    {
        @SubscribeEvent
        public static void onServerLoading(ServerConfigsLoadedEvent event)
        {   REGISTRY_ACCESS = event.getServer().registryAccess();
        }
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class GetAccessClient
    {
        @SubscribeEvent
        public static void onClientLoading(ClientPlayerNetworkEvent.LoggingIn event)
        {
            if (!Minecraft.getInstance().hasSingleplayerServer())
            {   REGISTRY_ACCESS = event.getPlayer().connection.registryAccess();
            }
        }
    }

    @Nullable
    public static RegistryAccess getRegistryAccess()
    {   return REGISTRY_ACCESS;
    }

    public static <T> List<T> mapForgeRegistryTagList(IForgeRegistry<T> registry, NegatableList<Either<TagKey<T>, T>> eitherList)
    {
        List<T> list = new ArrayList<>();
        for (Either<TagKey<T>, T> either : eitherList.requirements())
        {
            either.ifLeft(tagKey -> list.addAll(registry.tags().getTag(tagKey).stream().toList()));
            either.ifRight(object -> list.add(object));
        }
        for (Either<TagKey<T>, T> either : eitherList.exclusions())
        {
            either.ifLeft(tagKey -> list.removeAll(registry.tags().getTag(tagKey).stream().toList()));
            either.ifRight(object -> list.remove(object));
        }
        return list;
    }

    public static <T> List<T> mapForgeRegistryTagList(IForgeRegistry<T> registry, List<Either<TagKey<T>, T>> eitherList)
    {   return mapForgeRegistryTagList(registry, new NegatableList<>(eitherList));
    }

    public static <T> List<OptionalHolder<T>> mapVanillaRegistryTagList(ResourceKey<Registry<T>> registry, NegatableList<Either<TagKey<T>, OptionalHolder<T>>> eitherList, @Nullable RegistryAccess registryAccess)
    {
        Registry<T> reg = registryAccess != null ? registryAccess.registryOrThrow(registry) : getRegistry(registry);
        List<OptionalHolder<T>> list = new ArrayList<>();
        if (reg == null) return list;

        for (Either<TagKey<T>, OptionalHolder<T>> either : eitherList.requirements())
        {
            either.ifLeft(tagKey ->
            {
                Optional<HolderSet.Named<T>> tag = reg.getTag(tagKey);
                tag.ifPresent(tag1 -> list.addAll(tag1.stream().map(OptionalHolder::ofHolder).toList()));
            });
            either.ifRight(list::add);
        }
        for (Either<TagKey<T>, OptionalHolder<T>> either : eitherList.exclusions())
        {
            either.ifLeft(tagKey ->
            {
                Optional<HolderSet.Named<T>> tag = reg.getTag(tagKey);
                tag.ifPresent(tag1 -> list.removeAll(tag1.stream().map(OptionalHolder::ofHolder).toList()));
            });
            either.ifRight(list::remove);
        }
        return list;
    }

    public static <T> List<OptionalHolder<T>> mapVanillaRegistryTagList(ResourceKey<Registry<T>> registry, List<Either<TagKey<T>, OptionalHolder<T>>> eitherList, @Nullable RegistryAccess registryAccess)
    {   return mapVanillaRegistryTagList(registry, new NegatableList<>(eitherList), registryAccess);
    }

    public static <T> Optional<T> getVanillaRegistryValue(ResourceKey<Registry<T>> registry, ResourceLocation id)
    {
        try
        {   return Optional.ofNullable(getRegistry(registry)).map(reg -> reg.get(id));
        }
        catch (Exception e)
        {   return Optional.empty();
        }
    }

    public static <T> Optional<Holder<T>> getHolder(T object, ResourceKey<Registry<T>> registry, RegistryAccess registryAccess)
    {
        Registry<T> reg = registryAccess.registryOrThrow(registry);
        return reg.getHolder(reg.getId(object));
    }

    @Nullable
    public static ResourceLocation getKey(Holder<?> holder)
    {   return holder.unwrapKey().map(ResourceKey::location).orElse(null);
    }

    private static final Field REGISTRY_FIELD = ObfuscationReflectionHelper.findField(Holder.Reference.class, "f_205748_");
    private static final Field KEY_FIELD = ObfuscationReflectionHelper.findField(Holder.Reference.class, "f_205751_");
    private static final Method BIND_TAGS = ObfuscationReflectionHelper.findMethod(Holder.Reference.class, "m_205769_", Collection.class);
    static {
        REGISTRY_FIELD.setAccessible(true);
        KEY_FIELD.setAccessible(true);
        BIND_TAGS.setAccessible(true);
    }
    public static <T> Holder.Reference<T> modifyHolder(Holder.Reference<T> original, T value)
    {
        try
        {
            Registry<T> owner = (Registry<T>) REGISTRY_FIELD.get(original);
            Holder.Reference<T> newHolder = Holder.Reference.createIntrusive(owner, value);
            KEY_FIELD.set(newHolder, original.unwrapKey().orElse(null));
            BIND_TAGS.invoke(newHolder, original.tags().toList());

            return newHolder;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return original;
        }
    }
}
