package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockInit
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ColdSweat.MOD_ID);

    public static final RegistryObject<Block> BOILER = BLOCKS.register("boiler", () -> new BoilerBlock(BoilerBlock.getProperties()));
    public static final RegistryObject<Block> ICEBOX = BLOCKS.register("icebox", () -> new IceboxBlock(IceboxBlock.getProperties()));
    public static final RegistryObject<Block> SEWING_TABLE = BLOCKS.register("sewing_table", () -> new SewingTableBlock(SewingTableBlock.getProperties()));
    public static final RegistryObject<Block> MINECART_INSULATION = BLOCKS.register("minecart_insulation", () -> new MinecartInsulationBlock(MinecartInsulationBlock.getProperties()));
    public static final RegistryObject<Block> HEARTH_BOTTOM = BLOCKS.register("hearth_bottom", () -> new HearthBottomBlock(HearthBottomBlock.getProperties()));
    public static final RegistryObject<Block> HEARTH_TOP = BLOCKS.register("hearth_top", () -> new HearthTopBlock(HearthTopBlock.getProperties()));
}