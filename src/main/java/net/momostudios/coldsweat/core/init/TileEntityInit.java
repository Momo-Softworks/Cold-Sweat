package net.momostudios.coldsweat.core.init;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.te.BoilerTileEntity;

public class TileEntityInit
{
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ColdSweat.MOD_ID);

    public static final RegistryObject<TileEntityType<BoilerTileEntity>> BOILER_TILE_ENTITY_TYPE =
            TILE_ENTITY_TYPE.register("boiler", () -> TileEntityType.Builder.create(BoilerTileEntity::new, ModBlocks.BOILER.get()).build(null));
}
