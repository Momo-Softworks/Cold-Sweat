package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegistryHelper
{
    private static RegistryAccess REGISTRY_ACCESS = null;

    @Nullable
    public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> registry)
    {   return CSMath.getIfNotNull(getRegistryAccess(), access -> access.registryOrThrow(registry), null);
    }

    @EventBusSubscriber
    public static class GetAccessServer
    {
        @SubscribeEvent
        public static void onServerLoading(ServerAboutToStartEvent event)
        {   REGISTRY_ACCESS = event.getServer().registryAccess();
        }
    }

    @EventBusSubscriber(Dist.CLIENT)
    public static class GetAccessClient
    {
        @SubscribeEvent
        public static void onClientLoading(ClientPlayerNetworkEvent.LoggingIn event)
        {   REGISTRY_ACCESS = event.getPlayer().connection.registryAccess();
        }
    }

    @Nullable
    public static RegistryAccess getRegistryAccess()
    {   return REGISTRY_ACCESS;
    }

    public static <T> List<T> mapBuiltinRegistryTagList(Registry<T> registry, List<Either<TagKey<T>, T>> eitherList)
    {
        List<T> list = new ArrayList<>();
        if (registry == null) return list;

        for (Either<TagKey<T>, T> either : eitherList)
        {
            either.ifLeft(tagKey ->
                          {
                              Optional<HolderSet.Named<T>> tag = registry.getTag(tagKey);
                              tag.ifPresent(tag1 -> list.addAll(tag1.stream().map(Holder::value).toList()));
                          });
            either.ifRight(list::add);
        }
        return list;
    }

    public static <T> List<Holder<T>> mapRegistryTagList(ResourceKey<Registry<T>> registry, List<Either<TagKey<T>, Holder<T>>> eitherList, @Nullable RegistryAccess registryAccess)
    {
        Registry<T> reg = registryAccess != null ? registryAccess.registryOrThrow(registry) : getRegistry(registry);
        List<Holder<T>> list = new ArrayList<>();
        if (reg == null) return list;

        for (Either<TagKey<T>, Holder<T>> either : eitherList)
        {
            either.ifLeft(tagKey ->
            {
                Optional<HolderSet.Named<T>> tag = reg.getTag(tagKey);
                tag.ifPresent(tag1 -> list.addAll(tag1.stream().toList()));
            });
            either.ifRight(list::add);
        }
        return list;
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

    @Nullable
    public static ResourceLocation getKey(Holder<?> holder)
    {   return holder.unwrapKey().map(ResourceKey::location).orElse(null);
    }
}
