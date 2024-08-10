package com.momosoftworks.coldsweat.data;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.packs.ModFileResourcePack;

import java.util.*;

public class ModRegistries
{
    private static final SimpleReloadableResourceManager RESOURCE_MANAGER = new SimpleReloadableResourceManager(ResourcePackType.SERVER_DATA);

    public static SimpleReloadableResourceManager getResourceManager()
    {   return RESOURCE_MANAGER;
    }

    static
    {   RESOURCE_MANAGER.add(new ModFileResourcePack(ModList.get().getModFileById(ColdSweat.MOD_ID).getFile()));
    }

    // Item Registries
    public static CodecRegistry<InsulatorData> INSULATOR_DATA = new ModRegistries.CodecRegistry<>(new ResourceLocation(ColdSweat.MOD_ID, "item/insulator"), InsulatorData.CODEC);
    public static CodecRegistry<FuelData> FUEL_DATA = new ModRegistries.CodecRegistry<>(new ResourceLocation(ColdSweat.MOD_ID, "item/fuel"), FuelData.CODEC);
    public static CodecRegistry<FoodData> FOOD_DATA = new ModRegistries.CodecRegistry<>(new ResourceLocation(ColdSweat.MOD_ID, "item/food"), FoodData.CODEC);

    // World Registries
    public static CodecRegistry<BlockTempData> BLOCK_TEMP_DATA = new ModRegistries.CodecRegistry<>(new ResourceLocation(ColdSweat.MOD_ID, "block/block_temp"), BlockTempData.CODEC);
    public static CodecRegistry<BiomeTempData> BIOME_TEMP_DATA = new ModRegistries.CodecRegistry<>(new ResourceLocation(ColdSweat.MOD_ID, "world/biome_temp"), BiomeTempData.CODEC);
    public static CodecRegistry<DimensionTempData> DIMENSION_TEMP_DATA = new ModRegistries.CodecRegistry<>(new ResourceLocation(ColdSweat.MOD_ID, "world/dimension_temp"), DimensionTempData.CODEC);
    public static CodecRegistry<StructureTempData> STRUCTURE_TEMP_DATA = new ModRegistries.CodecRegistry<>(new ResourceLocation(ColdSweat.MOD_ID, "world/structure_temp"), StructureTempData.CODEC);
    public static CodecRegistry<DepthTempData> DEPTH_TEMP_DATA = new ModRegistries.CodecRegistry<>(new ResourceLocation(ColdSweat.MOD_ID, "world/depth_temp"), DepthTempData.CODEC);

    // Entity Registries
    public static CodecRegistry<MountData> MOUNT_DATA = new ModRegistries.CodecRegistry<>(new ResourceLocation(ColdSweat.MOD_ID, "entity/mount"), MountData.CODEC);
    public static CodecRegistry<SpawnBiomeData> ENTITY_SPAWN_BIOME_DATA = new ModRegistries.CodecRegistry<>(new ResourceLocation(ColdSweat.MOD_ID, "entity/spawn_biome"), SpawnBiomeData.CODEC);

    public static List<CodecRegistry<?>> getAllRegistries()
    {
        return Arrays.asList(INSULATOR_DATA,
                             FUEL_DATA,
                             FOOD_DATA,
                             BLOCK_TEMP_DATA,
                             BIOME_TEMP_DATA,
                             DIMENSION_TEMP_DATA,
                             STRUCTURE_TEMP_DATA,
                             DEPTH_TEMP_DATA,
                             MOUNT_DATA,
                             ENTITY_SPAWN_BIOME_DATA);
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
