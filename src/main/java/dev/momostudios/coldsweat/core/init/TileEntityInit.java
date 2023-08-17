package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.common.tileentity.ThermolithTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.tileentity.BoilerTileEntity;
import dev.momostudios.coldsweat.common.tileentity.HearthTileEntity;
import dev.momostudios.coldsweat.common.tileentity.IceboxTileEntity;

public class TileEntityInit
{
    public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ColdSweat.MOD_ID);

    public static final RegistryObject<TileEntityType<BoilerTileEntity>> BOILER_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("boiler", () -> TileEntityType.Builder.of(BoilerTileEntity::new, BlockInit.BOILER.get()).build(null));
    public static final RegistryObject<TileEntityType<IceboxTileEntity>> ICEBOX_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("icebox", () -> TileEntityType.Builder.of(IceboxTileEntity::new, BlockInit.ICEBOX.get()).build(null));
    public static final RegistryObject<TileEntityType<HearthTileEntity>> HEARTH_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("hearth", () -> TileEntityType.Builder.of(HearthTileEntity::new, BlockInit.HEARTH_BOTTOM.get()).build(null));
    public static final RegistryObject<TileEntityType<ThermolithTileEntity>> THERMOLITH_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("thermolith", () -> TileEntityType.Builder.of(ThermolithTileEntity::new, BlockInit.THERMOLITH.get()).build(null));
}