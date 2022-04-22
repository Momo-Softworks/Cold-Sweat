package dev.momostudios.coldsweat.util.registries;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.blockentity.BoilerBlockEntity;
import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import dev.momostudios.coldsweat.common.blockentity.IceboxBlockEntity;
import dev.momostudios.coldsweat.core.init.BlockEntityInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities
{
    public static final BlockEntityType<HearthBlockEntity> HEARTH = BlockEntityInit.HEARTH_BLOCK_ENTITY_TYPE.get();
    public static final BlockEntityType<BoilerBlockEntity> BOILER = BlockEntityInit.BOILER_BLOCK_ENTITY_TYPE.get();
    public static final BlockEntityType<IceboxBlockEntity> ICEBOX = BlockEntityInit.ICEBOX_BLOCK_ENTITY_TYPE.get();

    public static BlockEntityType<?> get(String id)
    {
        return RegistryObject.create(new ResourceLocation(ColdSweat.MOD_ID, id), ForgeRegistries.BLOCK_ENTITIES).get();
    }
}
