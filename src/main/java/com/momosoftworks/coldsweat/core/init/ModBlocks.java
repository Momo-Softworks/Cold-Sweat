package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ColdSweat.MOD_ID);

    public static final DeferredBlock<Block> BOILER = BLOCKS.register("boiler", () -> new BoilerBlock(BoilerBlock.getProperties()));
    public static final DeferredBlock<Block> ICEBOX = BLOCKS.register("icebox", () -> new IceboxBlock(IceboxBlock.getProperties()));
    public static final DeferredBlock<Block> SEWING_TABLE = BLOCKS.register("sewing_table", () -> new SewingTableBlock(SewingTableBlock.getProperties()));
    public static final DeferredBlock<Block> MINECART_INSULATION = BLOCKS.register("minecart_insulation", () -> new MinecartInsulationBlock(MinecartInsulationBlock.getProperties()));
    public static final DeferredBlock<Block> HEARTH_BOTTOM = BLOCKS.register("hearth_bottom", () -> new HearthBottomBlock(HearthBottomBlock.getProperties()));
    public static final DeferredBlock<Block> HEARTH_TOP = BLOCKS.register("hearth_top", () -> new HearthTopBlock(HearthTopBlock.getProperties()));
    public static final DeferredBlock<Block> THERMOLITH = BLOCKS.register("thermolith", () -> new ThermolithBlock(ThermolithBlock.getProperties()));
    public static final DeferredBlock<Block> SOUL_STALK = BLOCKS.register("soul_stalk", () -> new SoulStalkBlock(SoulStalkBlock.getProperties()));
    public static final DeferredBlock<Block> SMOKESTACK = BLOCKS.register("smokestack", () -> new SmokestackBlock(SmokestackBlock.getProperties()));
}