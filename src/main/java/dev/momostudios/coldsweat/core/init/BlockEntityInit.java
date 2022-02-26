package dev.momostudios.coldsweat.core.init;

import net.minecraft.tileentity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.te.BoilerTileEntity;
import dev.momostudios.coldsweat.common.te.HearthTileEntity;
import dev.momostudios.coldsweat.common.te.IceboxTileEntity;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityInit
{
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ColdSweat.MOD_ID);

    public static final RegistryObject<BlockEntityType<BoilerTileEntity>> BOILER_TILE_ENTITY_TYPE =
            TILE_ENTITY_TYPE.register("boiler", () -> BlockEntityType.Builder.of(BoilerTileEntity::new, BlockInit.BOILER.get()).build(null));
    public static final RegistryObject<BlockEntityType<IceboxTileEntity>> ICEBOX_TILE_ENTITY_TYPE =
        TILE_ENTITY_TYPE.register("icebox", () -> BlockEntityType.Builder.of(IceboxTileEntity::new, BlockInit.ICEBOX.get()).build(null));
    public static final RegistryObject<BlockEntityType<HearthTileEntity>> HEARTH_TILE_ENTITY_TYPE =
        TILE_ENTITY_TYPE.register("hearth", () -> BlockEntityType.Builder.of(HearthTileEntity::new, BlockInit.HEARTH.get()).build(null));
}
