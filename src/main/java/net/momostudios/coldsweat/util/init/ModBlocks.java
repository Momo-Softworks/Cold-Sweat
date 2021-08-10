package net.momostudios.coldsweat.util.init;

import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.block.BoilerBlock;

public class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ColdSweat.MOD_ID);

    public static Block boilerBlock = new BoilerBlock(BoilerBlock.getProperties());
    public static final RegistryObject<Block> BOILER = BLOCKS.register("boiler", () -> boilerBlock);
}