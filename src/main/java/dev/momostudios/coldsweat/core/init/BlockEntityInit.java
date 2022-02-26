package dev.momostudios.coldsweat.core.init;

import net.minecraft.tileentity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.te.BoilerBlockEntity;
import dev.momostudios.coldsweat.common.te.HearthBlockEntity;
import dev.momostudios.coldsweat.common.te.IceboxBlockEntity;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityInit
{
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ColdSweat.MOD_ID);

    public static final RegistryObject<BlockEntityType<BoilerBlockEntity>> BOILER_TILE_ENTITY_TYPE =
            TILE_ENTITY_TYPE.register("boiler", () -> BlockEntityType.Builder.of(BoilerBlockEntity::new, BlockInit.BOILER.get()).build(null));
    public static final RegistryObject<BlockEntityType<IceboxBlockEntity>> ICEBOX_TILE_ENTITY_TYPE =
        TILE_ENTITY_TYPE.register("icebox", () -> BlockEntityType.Builder.of(IceboxBlockEntity::new, BlockInit.ICEBOX.get()).build(null));
    public static final RegistryObject<BlockEntityType<HearthBlockEntity>> HEARTH_TILE_ENTITY_TYPE =
        TILE_ENTITY_TYPE.register("hearth", () -> BlockEntityType.Builder.of(HearthBlockEntity::new, BlockInit.HEARTH.get()).build(null));
}
