package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegistryHelper
{
    public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> registry)
    {   return getRegistryAccess().registryOrThrow(registry);
    }

    @Nullable
    public static RegistryAccess getRegistryAccess()
    {
        RegistryAccess access;
        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            access = Minecraft.getInstance().getConnection() != null
                     ? Minecraft.getInstance().getConnection().registryAccess()
                     : Minecraft.getInstance().level != null
                       ? Minecraft.getInstance().level.registryAccess()
                       : WorldHelper.getServer() != null
                         ? WorldHelper.getServer().registryAccess()
                         : null;
        }
        else
        {
            access = WorldHelper.getServer() != null
                     ? WorldHelper.getServer().registryAccess()
                     : null;
        }
        return access;
    }

    public static <T extends IForgeRegistryEntry<T>> List<T> mapForgeRegistryTagList(IForgeRegistry<T> registry, List<Either<TagKey<T>, T>> eitherList)
    {
        List<T> list = new ArrayList<>();
        for (Either<TagKey<T>, T> either : eitherList)
        {
            either.ifLeft(tagKey -> list.addAll(registry.tags().getTag(tagKey).stream().toList()));
            either.ifRight(object -> list.add(object));
        }
        return list;
    }

    public static <T> List<T> mapVanillaRegistryTagList(ResourceKey<Registry<T>> registry, List<Either<TagKey<T>, T>> eitherList, @Nullable RegistryAccess registryAccess)
    {
        List<T> list = new ArrayList<>();
        for (Either<TagKey<T>, T> either : eitherList)
        {
            Registry<T> reg = registryAccess != null ? registryAccess.registryOrThrow(registry) : getRegistry(registry);
            either.ifLeft(tagKey ->
            {
                Optional<HolderSet.Named<T>> tag = reg.getTag(tagKey);
                tag.ifPresent(tag1 -> list.addAll(tag1.stream().map(Holder::value).toList()));
            });
            either.ifRight(list::add);
        }
        return list;
    }

    public static <T extends IForgeRegistryEntry<T>> Codec<Either<TagKey<T>, T>> createForgeTagCodec(IForgeRegistry<T> forgeRegistry, ResourceKey<Registry<T>> vanillaRegistry)
    {
        return Codec.STRING.xmap(
               objectPath ->
               {
                   if (objectPath.startsWith("#"))
                   {   return Either.left(TagKey.create(vanillaRegistry, new ResourceLocation(objectPath.substring(1))));
                   }
                   else
                   {   return Either.right(forgeRegistry.getValue(new ResourceLocation(objectPath)));
                   }
               },
               objectEither -> objectEither.map(
                       tagKey -> "#" + tagKey.location(),
                       object -> forgeRegistry.getKey(object).toString()
               ));
    }

    public static <T> Codec<Either<TagKey<T>, T>> createVanillaTagCodec(ResourceKey<Registry<T>> vanillaRegistry)
    {
        return Codec.STRING.xmap(
               objectPath ->
               {
                   if (objectPath.startsWith("#"))
                   {   return Either.left(TagKey.create(vanillaRegistry, new ResourceLocation(objectPath.substring(1))));
                   }
                   else
                   {   return Either.right(getVanillaRegistryValue(vanillaRegistry, new ResourceLocation(objectPath)).orElseThrow());
                   }
               },
               objectEither -> objectEither.map(
                       tagKey -> "#" + tagKey.location(),
                       object -> getVanillaRegistryKey(vanillaRegistry, object).orElseThrow().toString()
               ));
    }

    public static <T> Optional<T> getVanillaRegistryValue(ResourceKey< Registry<T>> registry, ResourceLocation id)
    {
        try
        {   return Optional.ofNullable(getRegistry(registry).get(id));
        }
        catch (Exception e)
        {   return Optional.empty();
        }
    }

    public static <T> Optional<ResourceLocation> getVanillaRegistryKey(ResourceKey<Registry<T>> registry, T value)
    {
        try
        {   return Optional.ofNullable(getRegistry(registry).getKey(value));
        }
        catch (Exception e)
        {   return Optional.empty();
        }
    }

    @Nullable
    public static Biome getBiome(ResourceLocation biomeId)
    {   return getVanillaRegistryValue(Registry.BIOME_REGISTRY, biomeId).orElse(null);
    }

    @Nullable
    public static ResourceLocation getBiomeId(Biome biome)
    {   return getVanillaRegistryKey(Registry.BIOME_REGISTRY, biome).orElse(null);
    }

    @Nullable
    public static DimensionType getDimension(ResourceLocation dimensionId)
    {   return getVanillaRegistryValue(Registry.DIMENSION_TYPE_REGISTRY, dimensionId).orElse(null);
    }

    @Nullable
    public static ResourceLocation getDimensionId(DimensionType dimension)
    {   return getVanillaRegistryKey(Registry.DIMENSION_TYPE_REGISTRY, dimension).orElse(null);
    }

    @Nullable
    public static StructureFeature<?> getStructure(ResourceLocation structureId)
    {   return getVanillaRegistryValue(Registry.STRUCTURE_FEATURE_REGISTRY, structureId).orElse(null);
    }

    @Nullable
    public static ResourceLocation getStructureId(StructureFeature<?> structure)
    {   return getVanillaRegistryKey(Registry.STRUCTURE_FEATURE_REGISTRY, structure).orElse(null);
    }
}
