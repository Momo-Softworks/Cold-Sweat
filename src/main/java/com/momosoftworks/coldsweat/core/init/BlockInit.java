package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.*;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

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
    public static final RegistryObject<Block> THERMOLITH = BLOCKS.register("thermolith", () -> new ThermolithBlock(ThermolithBlock.getProperties()));
    public static final RegistryObject<Block> SOUL_STALK = BLOCKS.register("soul_stalk", () -> new SoulStalkBlock(SoulStalkBlock.getProperties()));
}