package dev.momostudios.coldsweat.core.init;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.te.BoilerTileEntity;
import dev.momostudios.coldsweat.common.te.HearthTileEntity;
import dev.momostudios.coldsweat.common.te.IceboxTileEntity;

public class TileEntityInit
{
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ColdSweat.MOD_ID);

    public static final RegistryObject<TileEntityType<BoilerTileEntity>> BOILER_TILE_ENTITY_TYPE =
            TILE_ENTITY_TYPE.register("boiler", () -> TileEntityType.Builder.create(BoilerTileEntity::new, BlockInit.BOILER.get()).build(null));
    public static final RegistryObject<TileEntityType<IceboxTileEntity>> ICEBOX_TILE_ENTITY_TYPE =
        TILE_ENTITY_TYPE.register("icebox", () -> TileEntityType.Builder.create(IceboxTileEntity::new, BlockInit.ICEBOX.get()).build(null));
    public static final RegistryObject<TileEntityType<HearthTileEntity>> HEARTH_TILE_ENTITY_TYPE =
        TILE_ENTITY_TYPE.register("hearth", () -> TileEntityType.Builder.create(HearthTileEntity::new, BlockInit.HEARTH.get()).build(null));
}
