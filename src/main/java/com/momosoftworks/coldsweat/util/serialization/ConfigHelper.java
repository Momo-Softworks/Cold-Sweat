package com.momosoftworks.coldsweat.util.serialization;

import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import com.momosoftworks.coldsweat.data.RegistryHolder;
import com.momosoftworks.coldsweat.data.RegistryHolder;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.math.RegistryMultiMap;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.*;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class ConfigHelper
{
    private ConfigHelper() {}

    public static <T> NegatableList<T> parseRegistryItems(RegistryKey<Registry<T>> registry, DynamicRegistries registryAccess, String objects)
    {   return parseRegistryItems(registry, registryAccess, objects.split(","));
    }
    public static <T> NegatableList<T> parseRegistryItems(RegistryKey<Registry<T>> registry, DynamicRegistries registryAccess, String[] objects)
    {
        NegatableList<T> registryList = new NegatableList<>();
        Registry<T> reg = registryAccess.registryOrThrow(registry);

        for (String objString : objects)
        {
            boolean negate = objString.startsWith("!");
            if (negate) objString = objString.substring(1);
            ResourceLocation id = new ResourceLocation(objString);
            Optional<T> obj = reg.getOptional(id);
            if (!obj.isPresent())
            {
                ColdSweat.LOGGER.error("Error parsing config: \"{}\" does not exist", objString);
                continue;
            }
            registryList.add(obj.get(), negate);
        }
        return registryList;
    }

    public static <T> NegatableList<Either<ITag<T>, T>> parseTaggableRegistryItems(RegistryKey<Registry<T>> registry, DynamicRegistries registryAccess, String objects)
    {   return parseTaggableRegistryItems(registry, registryAccess, objects.split(","));
    }
    public static <T> NegatableList<Either<ITag<T>, T>> parseTaggableRegistryItems(RegistryKey<Registry<T>> registry, DynamicRegistries registryAccess, String[] objects)
    {
        NegatableList<Either<ITag<T>, T>> registryList = new NegatableList<>();
        Registry<T> reg = registryAccess.registryOrThrow(registry);

        for (String objString : objects)
        {
            boolean negate = objString.startsWith("!");
            if (negate) objString = objString.substring(1);
            if (objString.startsWith("#"))
            {
                ITagCollection<T> tags = getTagsForRegistry(registry);
                if (tags == null) continue;

                final String tagID = objString.replace("#", "");
                registryList.add(Either.left(getTagsForRegistry(registry).getTag(new ResourceLocation(tagID))), negate);
            }
            else
            {
                ResourceLocation id = new ResourceLocation(objString);
                Optional<T> obj = reg.getOptional(id);
                if (!reg.containsKey(id) || !obj.isPresent())
                {
                    ColdSweat.LOGGER.error("Error parsing config: {} \"{}\" does not exist", registry.location().getPath(), objString);
                    continue;
                }
                registryList.add(Either.right(obj.get()), negate);
            }
        }
        return registryList;
    }

    public static <T> ITagCollection<T> getTagsForRegistry(RegistryKey<Registry<T>> registry)
    {
        if (registry.equals(net.minecraft.util.registry.Registry.ITEM_REGISTRY))
        {   return ((ITagCollection<T>) ItemTags.getAllTags());
        }
        else if (registry.equals(net.minecraft.util.registry.Registry.BLOCK_REGISTRY))
        {   return ((ITagCollection<T>) BlockTags.getAllTags());
        }
        else if (registry.equals(net.minecraft.util.registry.Registry.FLUID_REGISTRY))
        {   return ((ITagCollection<T>) FluidTags.getAllTags());
        }
        else if (registry.equals(net.minecraft.util.registry.Registry.ENTITY_TYPE_REGISTRY))
        {   return ((ITagCollection<T>) EntityTypeTags.getAllTags());
        }
        return null;
    }

    public static <T> ITagCollection<T> getTagsForObject(T object)
    {
        if (object instanceof Item)
        {   return ((ITagCollection<T>) ItemTags.getAllTags());
        }
        else if (object instanceof Block)
        {   return ((ITagCollection<T>) BlockTags.getAllTags());
        }
        else if (object instanceof Fluid)
        {   return ((ITagCollection<T>) FluidTags.getAllTags());
        }
        else if (object instanceof EntityType)
        {   return ((ITagCollection<T>) EntityTypeTags.getAllTags());
        }
        return null;
    }

    public static <T extends IForgeRegistryEntry<T>> NegatableList<Either<ITag<T>, T>> parseBuiltinItems(RegistryKey<Registry<T>> registryKey, IForgeRegistry<T> registry, String objects)
    {   return parseBuiltinItems(registryKey, registry, objects.split(","));
    }

    public static <T extends IForgeRegistryEntry<T>> NegatableList<Either<ITag<T>, T>> parseBuiltinItems(RegistryKey<Registry<T>> registryKey, IForgeRegistry<T> registry, String[] objects)
    {
        NegatableList<Either<ITag<T>, T>> registryList = new NegatableList<>();

        for (String objString : objects)
        {
            boolean negate = objString.startsWith("!");
            if (negate) objString = objString.substring(1);
            if (objString.startsWith("#"))
            {
                final String tagID = objString.replace("#", "");
                ITagCollection<T> tags = getTagsForRegistry(registryKey);
                if (tags == null)
                {   ColdSweat.LOGGER.error("Error parsing config: {} does not support tags", registryKey.location().getPath());
                    continue;
                }
                ITag<T> tag = tags.getTag(new ResourceLocation(tagID));
                if (tag == null)
                {   ColdSweat.LOGGER.error("Error parsing {} config: tag \"{}\" does not exist", registryKey.location().getPath(), tagID);
                    continue;
                }
                registryList.add(Either.left(tag), negate);
            }
            else
            {
                ResourceLocation id = new ResourceLocation(objString);
                if (!registry.containsKey(id))
                {
                    ColdSweat.LOGGER.error("Error parsing config: {} \"{}\" does not exist", registryKey.location().getPath(), objString);
                    continue;
                }
                T obj = registry.getValue(id);
                registryList.add(Either.right(obj), negate);
            }
        }
        return registryList;
    }

    public static NegatableList<Either<ITag<Block>, Block>> getBlocks(String blocks)
    {   return getBlocks(blocks.split(","));
    }
    public static NegatableList<Either<ITag<Block>, Block>> getBlocks(String[] blocks)
    {   return parseBuiltinItems(net.minecraft.util.registry.Registry.BLOCK_REGISTRY, ForgeRegistries.BLOCKS, blocks);
    }

    public static NegatableList<Either<ITag<Item>, Item>> getItems(String items)
    {   return getItems(items.split(","));
    }
    public static NegatableList<Either<ITag<Item>, Item>> getItems(String[] items)
    {   return parseBuiltinItems(net.minecraft.util.registry.Registry.ITEM_REGISTRY, ForgeRegistries.ITEMS, items);
    }

    public static NegatableList<Either<ITag<EntityType<?>>, EntityType<?>>> getEntityTypes(String entities)
    {   return getEntityTypes(entities.split(","));
    }
    public static NegatableList<Either<ITag<EntityType<?>>, EntityType<?>>> getEntityTypes(String[] entities)
    {   return parseBuiltinItems(net.minecraft.util.registry.Registry.ENTITY_TYPE_REGISTRY, ForgeRegistries.ENTITIES, entities);
    }

    public static <K extends IForgeRegistryEntry<K>, V extends ConfigData> Multimap<K, V> parseTomlRegistry(ForgeConfigSpec.ConfigValue<List< ? extends List<?>>> config, Function<List<?>, V> tomlParser,
                                                                                                            Function<V, NegatableList<Either<ITag<K>, K>>> keyListGetter,
                                                                                                            IForgeRegistry<K> keyRegistry, RegistryHolder<V> valueRegistry)
    {
        Multimap<K, V> dataMap = new RegistryMultiMap<>();
        for (List<?> entry : config.get())
        {
            V data = tomlParser.apply(entry);
            if (data == null) continue;

            data.setConfigType(ConfigData.Type.TOML);

            RegistryHelper.mapTaggableList(keyListGetter.apply(data)).forEach(ent -> dataMap.put(ent, data));
        }
        // Handle registry modifiers
        ConfigLoadingHandler.modifyEntries(dataMap, valueRegistry);
        return dataMap;
    }

    public static <K extends IForgeRegistryEntry<K>, V extends ConfigData> Map<K, V> parseTomlRegistryUnique(ForgeConfigSpec.ConfigValue<List<? extends List<?>>> config, Function<List<?>, V> tomlParser,
                                                                                                             Function<V, List<Either<ITag<K>, K>>> keyListGetter,
                                                                                                             IForgeRegistry<K> keyRegistry, RegistryHolder<V> valueRegistry)
    {
        Map<K, V> dataMap = new HashMap<>();
        for (List<?> entry : config.get())
        {
            V data = tomlParser.apply(entry);
            if (data == null) continue;

            data.setConfigType(ConfigData.Type.TOML);

            RegistryHelper.mapTaggableList(keyListGetter.apply(data)).forEach(ent -> dataMap.put(ent, data));
        }
        // Handle registry removals
        ConfigLoadingHandler.modifyEntries(dataMap, valueRegistry);
        return dataMap;
    }

    private static <K extends IForgeRegistryEntry<K>, V> void putRegistryEntries(Multimap<K, V> map, IForgeRegistry<K> registry, List<Either<ITag<K>, K>> list, V data)
    {
        RegistryHelper.mapTaggableList(list).forEach(entry -> map.put(entry, data));
    }

    public static <K, V extends ConfigData> Map<K, V> getRegistryMap(List<? extends List<?>> source, DynamicRegistries dynamicRegistries, RegistryKey<Registry<K>> keyRegistry,
                                                                             Function<List<?>, V> valueCreator, Function<V, NegatableList<Either<ITag<K>, K>>> taggedListGetter)
    {
        return getRegistryMapLike(source, dynamicRegistries, keyRegistry, valueCreator, taggedListGetter, HashMap::new, HashMap::put);
    }
    public static <K, V extends ConfigData> Map<K, V> getRegistryMap(List<? extends List<?>> source, RegistryKey<Registry<K>> keyRegistry,
                                                                     Function<List<?>, V> valueCreator, Function<V, NegatableList<K>> taggedListGetter)
    {
        return getRegistryMapLike(source, null, keyRegistry, valueCreator,
                                  v -> new NegatableList<>(taggedListGetter.apply(v).flatList().stream().map(Either::<ITag<K>, K>right).collect(Collectors.toList())),
                                  HashMap::new, HashMap::put);
    }

    public static <K, V extends ConfigData> Multimap<K, V> getRegistryMultimap(List<? extends List<?>> source, DynamicRegistries dynamicRegistries, RegistryKey<Registry<K>> keyRegistry,
                                                                               Function<List<?>, V> valueCreator, Function<V, NegatableList<Either<ITag<K>, K>>> taggedListGetter)
    {
        return getRegistryMapLike(source, dynamicRegistries, keyRegistry, valueCreator, taggedListGetter, RegistryMultiMap::new, RegistryMultiMap::put);
    }
    public static <K, V extends ConfigData> Multimap<K, V> getRegistryMultimap(List<? extends List<?>> source, RegistryKey<Registry<K>> keyRegistry,
                                                                               Function<List<?>, V> valueCreator, Function<V, NegatableList<K>> taggedListGetter)
    {
        return getRegistryMapLike(source, null, keyRegistry, valueCreator,
                                  v -> new NegatableList<>(taggedListGetter.apply(v).flatList().stream().map(Either::<ITag<K>, K>right).collect(Collectors.toList())),
                                  RegistryMultiMap::new, RegistryMultiMap::put);
    }

    private static <K, V extends ConfigData, M> M getRegistryMapLike(List<? extends List<?>> source, DynamicRegistries dynamicRegistries, RegistryKey<Registry<K>> keyRegistry,
                                                                     Function<List<?>, V> valueCreator, Function<V, NegatableList<Either<ITag<K>, K>>> listGetter,
                                                                     Supplier<M> mapSupplier, TriConsumer<M, K, V> mapAdder)
    {
        M map = mapSupplier.get();
        for (List<?> entry : source)
        {
            V data = valueCreator.apply(entry);
            if (data != null)
            {
                data.setConfigType(ConfigData.Type.TOML);
                for (K key : RegistryHelper.mapTaggableList(listGetter.apply(data)))
                {   mapAdder.accept(map, key, data);
                }
            }
            else ColdSweat.LOGGER.error("Error parsing {} config \"{}\"", keyRegistry.location(), entry.toString());
        }
        return map;
    }

    public static <T extends IForgeRegistryEntry<T>> Codec<Either<ITag<T>, T>> tagOrBuiltinCodec(RegistryKey<Registry<T>> vanillaRegistry, Registry<T> forgeRegistry)
    {
        ITagCollection<T> vanillaTags = ConfigHelper.getTagsForRegistry(vanillaRegistry);
        return Codec.either(Codec.STRING.comapFlatMap(str ->
                                                      {
                                                          if (!str.startsWith("#"))
                                                          {   return DataResult.error("Not a tag key: " + str);
                                                          }
                                                          ResourceLocation itemLocation = new ResourceLocation(str.replace("#", ""));
                                                          return DataResult.success(vanillaTags.getTag(itemLocation));
                                                      },
                                                      key -> "#" + vanillaTags.getId(key)),
                            forgeRegistry);
    }

    public static <T> Codec<T> dynamicCodec(RegistryKey<Registry<T>> vanillaRegistry)
    {
        Supplier<Registry<T>> registryGetter = () -> RegistryHelper.getDynamicRegistries().registry(vanillaRegistry).get();
        return Codec.STRING.xmap(str -> registryGetter.get().get(new ResourceLocation(str)),
                                 item -> registryGetter.get().getKey(item).toString());
    }

    public static <T> String serializeTagOrRegistryKey(Either<ITag<T>, RegistryKey<T>> obj)
    {
        return obj.map(tag -> "#" + ConfigHelper.getTagsForObject(obj).getId((ITag) tag),
                       key -> key.location().toString());
    }

    public static <T extends IForgeRegistryEntry<T>> String serializeTagOrBuiltinObject(IForgeRegistry<T> forgeRegistry, Either<ITag<T>, T> obj)
    {
        return obj.map(tag -> "#" + ConfigHelper.getTagsForObject(obj).getId((ITag) tag),
                       regObj -> Optional.ofNullable(forgeRegistry.getKey(regObj)).map(ResourceLocation::toString).orElse(""));
    }

    public static <T> String serializeTagOrRegistryObject(RegistryKey<Registry<T>> registry, Either<ITag<T>, T> obj, DynamicRegistries dynamicRegistries)
    {
        Registry<T> reg = dynamicRegistries.registryOrThrow(registry);
        return obj.map(tag -> "#" + ConfigHelper.getTagsForObject(obj).getId((ITag) tag),
                       regObj -> Optional.ofNullable(reg.getKey(regObj)).map(ResourceLocation::toString).orElse(""));
    }

    public static <T> Either<ITag<T>, RegistryKey<T>> deserializeTagOrRegistryKey(RegistryKey<Registry<T>> registry, String key)
    {
        if (key.startsWith("#"))
        {
            ResourceLocation tagID = new ResourceLocation(key.replace("#", ""));
            return Either.left(ConfigHelper.getTagsForRegistry(registry).getTag(tagID));
        }
        else
        {
            RegistryKey<T> biomeKey = RegistryKey.create(registry, new ResourceLocation(key));
            return Either.right(biomeKey);
        }
    }

    public static <T extends IForgeRegistryEntry<T>> Either<ITag<T>, T> deserializeTagOrRegistryObject(String tagOrRegistryObject, RegistryKey<Registry<T>> vanillaRegistry, IForgeRegistry<T> forgeRegistry)
    {
        if (tagOrRegistryObject.startsWith("#"))
        {
            ResourceLocation tagID = new ResourceLocation(tagOrRegistryObject.replace("#", ""));
            return Either.left(ConfigHelper.getTagsForRegistry(vanillaRegistry).getTag(tagID));
        }
        else
        {
            ResourceLocation id = new ResourceLocation(tagOrRegistryObject);
            T obj = forgeRegistry.getValue(id);
            if (obj == null)
            {   ColdSweat.LOGGER.error("Error deserializing config: object \"{}\" does not exist", tagOrRegistryObject);
                return null;
            }
            return Either.right(obj);
        }
    }

    public static Optional<FuelData> findFirstFuelMatching(DynamicHolder<Multimap<Item, FuelData>> predicates, ItemStack stack)
    {
        for (FuelData predicate : predicates.get().get(stack.getItem()))
        {
            if (predicate.test(stack))
            {   return Optional.of(predicate);
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<T> parseResource(IResourceManager resourceManager, ResourceLocation location, Codec<T> codec)
    {
        if (resourceManager == null)
        {   return Optional.empty();
        }
        try
        {
            IResource resource = resourceManager.getResource(location);
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
            {
                JsonObject json = JSONUtils.parse(reader);
                DataResult<T> result = codec.parse(JsonOps.INSTANCE, json);
                return result.result();
            }
        }
        catch (IOException e)
        {   ColdSweat.LOGGER.error(new RuntimeException("Failed to load JSON file: " + location, e));
            return Optional.empty();
        }
    }

    public static String concatStringList(List<String> list)
    {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = list.iterator();
        while (iter.hasNext())
        {
            builder.append(iter.next());
            if (iter.hasNext())
            {   builder.append(",");
            }
        }
        return builder.toString();
    }

    public static <K, V> V getFirstOrNull(DynamicHolder<Multimap<K, V>> map, K key, Predicate<V> filter)
    {
        Collection<V> values = map.get().get(key).stream().filter(filter).collect(Collectors.toList());
        if (values.isEmpty())
        {   return null;
        }
        return values.iterator().next();
    }
}
