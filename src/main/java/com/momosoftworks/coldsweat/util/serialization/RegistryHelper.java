package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.tags.ITag;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RegistryHelper
{
    private static DynamicRegistries REGISTRY_ACCESS = null;

    @Nullable
    public static <T> Registry<T> getRegistry(RegistryKey<Registry<T>> registry)
    {   return CSMath.getIfNotNull(getDynamicRegistries(), access -> access.registryOrThrow(registry), null);
    }

    @Mod.EventBusSubscriber
    public static class GetAccessServer
    {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onServerLoading(FMLServerAboutToStartEvent event)
        {   REGISTRY_ACCESS = event.getServer().registryAccess();
        }
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class GetAccessClient
    {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onClientLoading(ClientPlayerNetworkEvent.LoggedInEvent event)
        {   REGISTRY_ACCESS = event.getPlayer().connection.registryAccess();
        }
    }

    @Nullable
    public static DynamicRegistries getDynamicRegistries()
    {   return REGISTRY_ACCESS;
    }

    public static <T> List<T> mapTaggableList(List<Either<ITag<T>, T>> eitherList)
    {
        List<T> list = new ArrayList<>();
        for (Either<ITag<T>, T> either : eitherList)
        {
            either.ifLeft(tag ->
            {   if (tag != null) list.addAll(tag.getValues());
            });
            either.ifRight(list::add);
        }
        return list;
    }

    public static <T> Optional<T> getVanillaRegistryValue(RegistryKey<Registry<T>> registry, ResourceLocation id)
    {
        try
        {   return Optional.ofNullable(getRegistry(registry)).map(reg -> reg.get(id));
        }
        catch (Exception e)
        {   return Optional.empty();
        }
    }

    public static ResourceLocation getKey(ConfigData object)
    {
        for (ModRegistries.ConfigRegistry<?> registry : ModRegistries.getRegistries().values())
        {
            if (registry.type().isInstance(object))
            {
                for (Map.Entry<ResourceLocation, ?> entry : registry.data().entrySet())
                {
                    if (entry.getValue() == object)
                    {   return entry.getKey();
                    }
                }
            }
        }
        return new ResourceLocation("unknown");
    }
}
