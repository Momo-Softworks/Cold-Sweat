package net.momostudios.coldsweat.core.init;

import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.block.BoilerBlock;
import net.momostudios.coldsweat.common.block.IceboxBlock;
import net.momostudios.coldsweat.common.block.SewingTableBlock;

public class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ColdSweat.MOD_ID);

    public static Block boilerBlock = new BoilerBlock(BoilerBlock.getProperties());
    public static final RegistryObject<Block> BOILER = BLOCKS.register("boiler", () -> boilerBlock);
    public static Block iceBoxBlock = new IceboxBlock(IceboxBlock.getProperties());
    public static final RegistryObject<Block> ICEBOX = BLOCKS.register("icebox", () -> iceBoxBlock);
    public static Block sewingTableBlock = new SewingTableBlock(SewingTableBlock.getProperties());
    public static final RegistryObject<Block> SEWING_TABLE = BLOCKS.register("sewing_table", () -> sewingTableBlock);
}