package com.momosoftworks.coldsweat.data;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import net.minecraft.resources.FallbackResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.packs.ModFileResourcePack;

import java.util.*;

public class ModRegistries
{
    private static final FallbackResourceManager RESOURCE_MANAGER = new FallbackResourceManager(ResourcePackType.SERVER_DATA, ColdSweat.MOD_ID);
    private static final Collection<CodecRegistry<?>> ALL_REGISTRIES = new HashSet<>();

    public static IResourceManager getResourceManager()
    {   return RESOURCE_MANAGER;
    }

    static
    {   RESOURCE_MANAGER.add(new ModFileResourcePack(ModList.get().getModFileById(ColdSweat.MOD_ID).getFile()));
    }

    // Item Registries
    public static CodecRegistry<InsulatorData> INSULATOR_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "item/insulator"), InsulatorData.CODEC);
    public static CodecRegistry<FuelData> FUEL_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "item/fuel"), FuelData.CODEC);
    public static CodecRegistry<FoodData> FOOD_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "item/food"), FoodData.CODEC);
    public static CodecRegistry<ItemCarryTempData> CARRY_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "item/carried_temp"), ItemCarryTempData.CODEC);

    // World Registries
    public static CodecRegistry<BlockTempData> BLOCK_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "block/block_temp"), BlockTempData.CODEC);
    public static CodecRegistry<BiomeTempData> BIOME_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "world/biome_temp"), BiomeTempData.CODEC);
    public static CodecRegistry<DimensionTempData> DIMENSION_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "world/dimension_temp"), DimensionTempData.CODEC);
    public static CodecRegistry<StructureTempData> STRUCTURE_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "world/structure_temp"), StructureTempData.CODEC);
    public static CodecRegistry<DepthTempData> DEPTH_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "world/depth_temp"), DepthTempData.CODEC);

    // Entity Registries
    public static CodecRegistry<MountData> MOUNT_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "entity/mount"), MountData.CODEC);
    public static CodecRegistry<SpawnBiomeData> ENTITY_SPAWN_BIOME_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "entity/spawn_biome"), SpawnBiomeData.CODEC);
    public static final CodecRegistry<EntityTempData> ENTITY_TEMP_DATA = createRegistry(new ResourceLocation(ColdSweat.MOD_ID, "entity/entity_temp"), EntityTempData.CODEC);

    public static <T> CodecRegistry<T> createRegistry(ResourceLocation registryName, Codec<T> codec)
    {
        CodecRegistry<T> registry = new CodecRegistry<>(registryName, codec);
        ALL_REGISTRIES.add(registry);
        return registry;
    }

    public static Collection<CodecRegistry<?>> getRegistries()
    {   return new HashSet<>(ALL_REGISTRIES);
    }
    
    public static class CodecRegistry<V>
    {
        private final Set<V> registry = new HashSet<>();
        private final Codec<V> codec;
        private final ResourceLocation registryName;

        public CodecRegistry(ResourceLocation registryName, Codec<V> codec)
        {
            this.registryName = registryName;
            this.codec = codec;
        }
        
        public Codec<V> getCodec()
        {   return codec;
        }
        
        public void register(Object value)
        {
            try
            {   registry.add((V) value);
            }
            catch (ClassCastException e)
            {   ColdSweat.LOGGER.error("Failed to register {} ({}) to {}. Incompatible classes.", value, value.getClass(), this.getRegistryName());
            }
        }
        
        public Collection<V> getValues()
        {   return new HashSet<>(registry);
        }

        public ResourceLocation getRegistryName()
        {   return registryName;
        }

        public void flush()
        {   registry.clear();
        }
    }
}
