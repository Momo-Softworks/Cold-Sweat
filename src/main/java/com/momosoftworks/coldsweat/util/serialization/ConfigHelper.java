package com.momosoftworks.coldsweat.util.serialization;

import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import com.momosoftworks.coldsweat.config.spec.CSConfigSpec;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.math.FastMap;
import com.momosoftworks.coldsweat.util.math.FastMultiMap;
import com.momosoftworks.coldsweat.util.math.RegistryMultiMap;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigHelper
{
    private ConfigHelper() {}

    public static <T> NegatableList<Either<TagKey<T>, Holder<T>>> parseRegistryItems(ResourceKey<Registry<T>> registry, RegistryAccess registryAccess, String objects)
    {   return parseRegistryItems(registry, registryAccess, objects.split(","));
    }

    public static <T> NegatableList<Either<TagKey<T>, Holder<T>>> parseRegistryItems(ResourceKey<Registry<T>> registry, RegistryAccess registryAccess, String[] objects)
    {
        NegatableList<Either<TagKey<T>, Holder<T>>> registryList = new NegatableList<>();
        Registry<T> reg = registryAccess.registryOrThrow(registry);

        for (String objString : objects)
        {
            boolean negate = objString.startsWith("!");
            if (negate) objString = objString.substring(1);
            if (objString.startsWith("#"))
            {
                final String tagID = objString.replace("#", "");
                registryList.add(Either.left(TagKey.create(registry, ResourceLocation.parse(tagID))), negate);
            }
            else
            {
                ResourceLocation id = ResourceLocation.parse(objString);
                Optional<Holder.Reference<T>> obj = reg.getHolder(ResourceKey.create(registry, id));
                if (!reg.containsKey(id) || obj.isEmpty())
                {
                    ColdSweat.LOGGER.error("Error parsing config: {} \"{}\" does not exist", registry.location().getPath(), objString);
                    continue;
                }
                registryList.add(Either.right(obj.get()), negate);
            }
        }
        return registryList;
    }

    public static <T> NegatableList<Either<TagKey<T>, T>> parseBuiltinItems(ResourceKey<Registry<T>> registryKey, Registry<T> registry, String objects)
    {   return parseBuiltinItems(registryKey, registry, objects.split(","));
    }

    public static <T> NegatableList<Either<TagKey<T>, T>> parseBuiltinItems(ResourceKey<Registry<T>> registryKey, Registry<T> registry, String[] objects)
    {
        NegatableList<Either<TagKey<T>, T>> registryList = new NegatableList<>();

        for (String objString : objects)
        {
            boolean negate = objString.startsWith("!");
            if (negate) objString = objString.substring(1);
            if (objString.startsWith("#"))
            {
                final String tagID = objString.replace("#", "");
                registryList.add(Either.left(TagKey.create(registryKey, ResourceLocation.parse(tagID))), negate);
            }
            else
            {
                ResourceLocation id = ResourceLocation.parse(objString);
                if (!registry.containsKey(id))
                {
                    ColdSweat.LOGGER.error("Error parsing config: {} \"{}\" does not exist", registryKey.location().getPath(), objString);
                    continue;
                }
                T obj = registry.get(id);
                registryList.add(Either.right(obj), negate);
            }
        }
        return registryList;
    }

    public static NegatableList<Either<TagKey<Block>, Block>> getBlocks(String blocks)
    {   return getBlocks(blocks.split(","));
    }
    public static NegatableList<Either<TagKey<Block>, Block>> getBlocks(String[] blocks)
    {   return parseBuiltinItems(Registries.BLOCK, BuiltInRegistries.BLOCK, blocks);
    }

    public static NegatableList<Either<TagKey<Item>, Item>> getItems(String items)
    {   return getItems(items.split(","));
    }
    public static NegatableList<Either<TagKey<Item>, Item>> getItems(String[] items)
    {   return parseBuiltinItems(Registries.ITEM, BuiltInRegistries.ITEM, items);
    }

    public static NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> getEntityTypes(String entities)
    {   return getEntityTypes(entities.split(","));
    }
    public static NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> getEntityTypes(String[] entities)
    {   return parseBuiltinItems(Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE, entities);
    }

    public static <K, V extends ConfigData> Multimap<K, V> parseTomlRegistry(CSConfigSpec.ConfigValue<List<? extends List<?>>> config, Function<List<?>, V> tomlParser, Function<V, NegatableList<Either<TagKey<K>, K>>> keyListGetter,
                                                                             Registry<K> keyRegistry, ResourceKey<Registry<V>> valueRegistry)
    {
        Multimap<K, V> dataMap = new RegistryMultiMap<>();
        for (List<?> entry : config.get())
        {
            V data = tomlParser.apply(entry);
            if (data == null) continue;

            data.setRegistryType(ConfigData.Type.TOML);

            RegistryHelper.mapBuiltinRegistryTagList(keyRegistry, keyListGetter.apply(data)).forEach(ent -> dataMap.put(ent, data));
        }
        // Handle registry removals
        ConfigLoadingHandler.removeEntries(dataMap.values(), valueRegistry);
        return dataMap;
    }

    public static <K, V extends ConfigData> Map<K, V> parseTomlRegistryUnique(ModConfigSpec.ConfigValue<List<? extends List<?>>> config, Function<List<?>, V> tomlParser, Function<V, List<Either<TagKey<K>, K>>> keyListGetter,
                                                                              Registry<K> keyRegistry, ResourceKey<Registry<V>> valueRegistry)
    {
        Map<K, V> dataMap = new HashMap<>();
        for (List<?> entry : config.get())
        {
            V data = tomlParser.apply(entry);
            if (data == null) continue;

            data.setRegistryType(ConfigData.Type.TOML);

            RegistryHelper.mapBuiltinRegistryTagList(keyRegistry, keyListGetter.apply(data)).forEach(ent -> dataMap.put(ent, data));
        }
        // Handle registry removals
        ConfigLoadingHandler.removeEntries(dataMap.values(), valueRegistry);
        return dataMap;
    }

    private static <K, V> void putRegistryEntries(Multimap<K, V> map, Registry<K> registry, List<Either<TagKey<K>, K>> list, V data)
    {
        RegistryHelper.mapBuiltinRegistryTagList(registry, list).forEach(entry -> map.put(entry, data));
    }

    public static <K, V extends ConfigData> Map<Holder<K>, V> getRegistryMap(List<? extends List<?>> source, RegistryAccess registryAccess, ResourceKey<Registry<K>> keyRegistry,
                                                                             Function<List<?>, V> valueCreator, Function<V, NegatableList<Either<TagKey<K>, Holder<K>>>> taggedListGetter)
    {
        return getRegistryMapLike(source, registryAccess, keyRegistry, valueCreator, taggedListGetter, FastMap::new, FastMap::put);
    }

    public static <K, V extends ConfigData> Multimap<Holder<K>, V> getRegistryMultimap(List<? extends List<?>> source, RegistryAccess registryAccess, ResourceKey<Registry<K>> keyRegistry,
                                                                                       Function<List<?>, V> valueCreator, Function<V, NegatableList<Either<TagKey<K>, Holder<K>>>> taggedListGetter)
    {
        return getRegistryMapLike(source, registryAccess, keyRegistry, valueCreator, taggedListGetter, FastMultiMap::new, FastMultiMap::put);
    }

    private static <K, V extends ConfigData, M> M getRegistryMapLike(List<? extends List<?>> source, RegistryAccess registryAccess, ResourceKey<Registry<K>> keyRegistry,
                                                                     Function<List<?>, V> valueCreator, Function<V, NegatableList<Either<TagKey<K>, Holder<K>>>> taggedListGetter,
                                                                     Supplier<M> mapSupplier, TriConsumer<M, Holder<K>, V> mapAdder)
    {
        M map = mapSupplier.get();
        for (List<?> entry : source)
        {
            V data = valueCreator.apply(entry);
            if (data != null)
            {
                data.setRegistryType(ConfigData.Type.TOML);
                for (Holder<K> key : RegistryHelper.mapRegistryTagList(keyRegistry, taggedListGetter.apply(data), registryAccess))
                {   mapAdder.accept(map, key, data);
                }
            }
            else ColdSweat.LOGGER.error("Error parsing {} config \"{}\"", keyRegistry.location(), entry.toString());
        }
        return map;
    }

    public static <T> Codec<Either<TagKey<T>, T>> tagOrBuiltinCodec(ResourceKey<Registry<T>> vanillaRegistry, DefaultedRegistry<T> forgeRegistry)
    {
        return Codec.either(Codec.STRING.comapFlatMap(str ->
                                                      {
                                                          if (!str.startsWith("#"))
                                                          {   return DataResult.<TagKey<T>>error(() -> String.format("Not a tag key for builtin registry %s: %s", vanillaRegistry.location(), str));
                                                          }
                                                          ResourceLocation itemLocation = ResourceLocation.parse(str.replace("#", ""));
                                                          return DataResult.success(TagKey.create(vanillaRegistry, itemLocation));
                                                      },
                                                      key -> "#" + key.location()),
                            Codec.STRING.comapFlatMap(str ->
                                                      {
                                                          ResourceLocation itemLocation = ResourceLocation.parse(str);
                                                          Optional<T> obj = forgeRegistry.getOptional(itemLocation);
                                                          if (obj.isEmpty())
                                                          {
                                                              if (CompatManager.modLoaded(itemLocation.getNamespace()))
                                                              {
                                                                  ColdSweat.LOGGER.error("Error deserializing config: object \"{}\" does not exist", str);
                                                                  return DataResult.error(() -> "Object does not exist");
                                                              }
                                                              else return DataResult.success(forgeRegistry.get(forgeRegistry.getDefaultKey()));
                                                          }
                                                          return DataResult.success(obj.get());
                                                      },
                                                      obj ->
                                                      {
                                                          ResourceLocation itemLocation = forgeRegistry.getKey(obj);
                                                          return itemLocation.toString();
                                                      }));
    }

    public static <T> Codec<Either<TagKey<T>, Holder<T>>> tagOrHolderCodec(ResourceKey<Registry<T>> vanillaRegistry)
    {
        return Codec.either(Codec.STRING.comapFlatMap(str ->
                                                      {
                                                          if (!str.startsWith("#"))
                                                          {   return DataResult.error(() -> String.format("Not a tag key for dynamic holder registry %s: %s", vanillaRegistry.location(), str));
                                                          }
                                                          ResourceLocation itemLocation = ResourceLocation.parse(str.replace("#", ""));
                                                          return DataResult.success(TagKey.create(vanillaRegistry, itemLocation));
                                                      },
                                                      key -> "#" + key.location()),
                            Codec.STRING.comapFlatMap(str ->
                                                      {
                                                          RegistryAccess registryAccess = RegistryHelper.getRegistryAccess();
                                                          if (registryAccess == null)
                                                          {   ColdSweat.LOGGER.error("Error deserializing config: registry access is null");
                                                              return DataResult.error(() -> "Registry access is null");
                                                          }
                                                          ResourceLocation itemLocation = ResourceLocation.parse(str);
                                                          Registry<T> registry = registryAccess.registry(vanillaRegistry).orElse(null);
                                                          if (registry == null)
                                                          {   ColdSweat.LOGGER.error("Error deserializing config: registry \"{}\" does not exist", vanillaRegistry.location());
                                                              return DataResult.error(() -> "Registry does not exist");
                                                          }
                                                          Optional<Holder.Reference<T>> holder = registry.getHolder(itemLocation);
                                                          if (holder.isEmpty())
                                                          {
                                                              if (CompatManager.modLoaded(itemLocation.getNamespace()))
                                                              {
                                                                  ColdSweat.LOGGER.error("Error deserializing config: object \"{}\" does not exist", str);
                                                                  return DataResult.error(() -> "Object does not exist");
                                                              }
                                                              else return DataResult.success(Holder.Reference.createIntrusive(new HolderOwner<>(){}, registry.stream().findFirst().get()));
                                                          }
                                                          return DataResult.success(holder.get());
                                                      },
                                                      holder ->
                                                      {
                                                          RegistryAccess registryAccess = RegistryHelper.getRegistryAccess();
                                                          if (registryAccess == null)
                                                          {   ColdSweat.LOGGER.error("Error serializing config: registry access is null");
                                                              return "null";
                                                          }
                                                          Registry<T> registry = registryAccess.registry(vanillaRegistry).orElse(null);
                                                          if (registry == null)
                                                          {   ColdSweat.LOGGER.error("Error serializing config: registry \"{}\" does not exist", vanillaRegistry.location());
                                                              return "null";
                                                          }
                                                          return registry.getKey(holder.value()).toString();
                                                      }));
    }

    public static <T> Codec<Either<TagKey<T>, Holder<T>>> tagOrBuiltinHolderCodec(ResourceKey<Registry<T>> vanillaRegistry, Registry<T> registry)
    {
        return Codec.either(Codec.STRING.comapFlatMap(str ->
                                                      {
                                                          if (!str.startsWith("#"))
                                                          {   return DataResult.error(() -> String.format("Not a tag key for builtin holder registry %s: %s", vanillaRegistry.location(), str));
                                                          }
                                                          ResourceLocation itemLocation = ResourceLocation.parse(str.replace("#", ""));
                                                          return DataResult.success(TagKey.create(vanillaRegistry, itemLocation));
                                                      },
                                                      key -> "#" + key.location()),
                            Codec.STRING.comapFlatMap(str ->
                                                      {
                                                          ResourceLocation itemLocation = ResourceLocation.parse(str);
                                                          Optional<Holder.Reference<T>> holder = registry.getHolder(itemLocation);
                                                          if (holder.isEmpty())
                                                          {
                                                              if (CompatManager.modLoaded(itemLocation.getNamespace()))
                                                              {
                                                                  ColdSweat.LOGGER.error("Error deserializing config: object \"{}\" does not exist", str);
                                                                  return DataResult.error(() -> "Object does not exist");
                                                              }
                                                              else return DataResult.success(Holder.Reference.createIntrusive(new HolderOwner<>(){}, registry.stream().findFirst().get()));
                                                          }
                                                          return DataResult.success(registry.getHolder(itemLocation).get());
                                                      },
                                                      holder ->
                                                      {
                                                          ResourceLocation itemLocation = registry.getKey(holder.value());
                                                          return itemLocation.toString();
                                                      }));
    }

    public static <T> Codec<Either<TagKey<T>, Holder<T>>> tagOrHolderCodec(ResourceKey<Registry<T>> vanillaRegistry, Codec<Holder<T>> codec)
    {
        return Codec.either(Codec.STRING.comapFlatMap(str ->
                                                      {
                                                          if (!str.startsWith("#"))
                                                          {   return DataResult.error(() -> "Not a tag key: " + str);
                                                          }
                                                          ResourceLocation itemLocation = ResourceLocation.parse(str.replace("#", ""));
                                                          return DataResult.success(TagKey.create(vanillaRegistry, itemLocation));
                                                      },
                                                      key -> "#" + key.location()),
                            codec);
    }

    public static <T> Codec<Either<TagKey<T>, ResourceKey<T>>> tagOrResourceKeyCodec(ResourceKey<Registry<T>> vanillaRegistry)
    {
        return Codec.either(Codec.STRING.comapFlatMap(str ->
                                                      {
                                                          if (!str.startsWith("#"))
                                                          {   return DataResult.error(() -> String.format("Not a tag key for dynamic resource registry %s: %s", vanillaRegistry.location(), str));
                                                          }
                                                          ResourceLocation itemLocation = ResourceLocation.parse(str.replace("#", ""));
                                                          return DataResult.success(TagKey.create(vanillaRegistry, itemLocation));
                                                      },
                                                      key -> "#" + key.location()),
                            net.minecraft.resources.ResourceKey.codec(vanillaRegistry));
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

    public static <T> Optional<T> parseResource(ResourceManager resourceManager, ResourceLocation location, Codec<T> codec)
    {
        if (resourceManager == null)
        {   return Optional.empty();
        }
        try
        {
            Resource resource = resourceManager.getResource(location).orElseThrow();
            try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8))
            {
                JsonObject json = GsonHelper.parse(reader);
                return codec.parse(JsonOps.INSTANCE, json).result();
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

    public static <K, T extends ConfigData> List<T> getTaggedConfigsFor(K object, TagKey<T> tag, Multimap<K, T> config, RegistryAccess registryAccess)
    {
        Registry<T> registry = registryAccess.registryOrThrow(tag.registry());

        List<T> results = new ArrayList<>();
        for (T configData : config.get(object))
        {
            Holder<T> holder = registry.wrapAsHolder(configData);
            if (holder.is(tag))
            {   results.add(configData);
            }
        }
        return results;
    }

    public static <K, V> V getFirstOrNull(DynamicHolder<Multimap<K, V>> map, K key, Predicate<V> filter)
    {
        Collection<V> values = map.get().get(key).stream().filter(filter).toList();
        if (values.isEmpty())
        {   return null;
        }
        return values.iterator().next();
    }
}
