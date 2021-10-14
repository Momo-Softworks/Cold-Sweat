package net.momostudios.coldsweat.core.init;

import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.block.*;

public class BlockInit
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ColdSweat.MOD_ID);

    public static Block boilerBlock = new BoilerBlock(BoilerBlock.getProperties());
    public static final RegistryObject<Block> BOILER = BLOCKS.register("boiler", () -> boilerBlock);

    public static Block iceBoxBlock = new IceboxBlock(IceboxBlock.getProperties());
    public static final RegistryObject<Block> ICEBOX = BLOCKS.register("icebox", () -> iceBoxBlock);

    public static Block sewingTableBlock = new SewingTableBlock(SewingTableBlock.getProperties());
    public static final RegistryObject<Block> SEWING_TABLE = BLOCKS.register("sewing_table", () -> sewingTableBlock);

    public static Block minecartInsulationBlock = new SewingTableBlock(SewingTableBlock.getProperties());
    public static final RegistryObject<Block> MINECART_INSULATION = BLOCKS.register("minecart_insulation", () -> minecartInsulationBlock);

    public static Block hearthBlock = new HearthBlock(HearthBlock.getProperties());
    public static final RegistryObject<Block> HEARTH = BLOCKS.register("hearth", () -> hearthBlock);

    public static Block hearthTopBlock = new HearthTopBlock(HearthBlock.getProperties());
    public static final RegistryObject<Block> HEARTH_TOP = BLOCKS.register("hearth_top", () -> hearthTopBlock);
}