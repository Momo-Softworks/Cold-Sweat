package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.common.blockentity.ThermolithBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.blockentity.BoilerBlockEntity;
import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import dev.momostudios.coldsweat.common.blockentity.IceboxBlockEntity;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockEntityInit
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ColdSweat.MOD_ID);

    public static final RegistryObject<BlockEntityType<BoilerBlockEntity>> BOILER_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("boiler", () -> BlockEntityType.Builder.of(BoilerBlockEntity::new, BlockInit.BOILER.get()).build(null));
    public static final RegistryObject<BlockEntityType<IceboxBlockEntity>> ICEBOX_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("icebox", () -> BlockEntityType.Builder.of(IceboxBlockEntity::new, BlockInit.ICEBOX.get()).build(null));
    public static final RegistryObject<BlockEntityType<HearthBlockEntity>> HEARTH_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("hearth", () -> BlockEntityType.Builder.of(HearthBlockEntity::new, BlockInit.HEARTH_BOTTOM.get()).build(null));
    public static final RegistryObject<BlockEntityType<ThermolithBlockEntity>> THERMOLITH_BLOCK_ENTITY_TYPE =
            BLOCK_ENTITY_TYPES.register("thermolith", () -> BlockEntityType.Builder.of(ThermolithBlockEntity::new, BlockInit.THERMOLITH.get()).build(null));
}