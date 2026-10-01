package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.*;
import com.momosoftworks.coldsweat.common.fluid.SlushFluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ColdSweat.MOD_ID);

    public static final DeferredBlock<Block> BOILER = BLOCKS.registerBlock("boiler", BoilerBlock::new, BoilerBlock::getProperties);
    public static final DeferredBlock<Block> ICEBOX = BLOCKS.registerBlock("icebox", IceboxBlock::new, IceboxBlock::getProperties);
    public static final DeferredBlock<Block> SEWING_TABLE = BLOCKS.registerBlock("sewing_table", SewingTableBlock::new, SewingTableBlock::getProperties);
    public static final DeferredBlock<Block> MINECART_INSULATION = BLOCKS.registerBlock("minecart_insulation", MinecartInsulationBlock::new, MinecartInsulationBlock::getProperties);
    public static final DeferredBlock<Block> HEARTH_BOTTOM = BLOCKS.registerBlock("hearth_bottom", HearthBottomBlock::new, HearthBottomBlock::getProperties);
    public static final DeferredBlock<Block> HEARTH_TOP = BLOCKS.registerBlock("hearth_top", HearthTopBlock::new, HearthTopBlock::getProperties);
    public static final DeferredBlock<Block> THERMOLITH = BLOCKS.registerBlock("thermolith", ThermolithBlock::new, ThermolithBlock::getProperties);
    public static final DeferredBlock<Block> SOUL_STALK = BLOCKS.registerBlock("soul_stalk", SoulStalkBlock::new, SoulStalkBlock::getProperties);
    public static final DeferredBlock<Block> SMOKESTACK = BLOCKS.registerBlock("smokestack", SmokestackBlock::new, SmokestackBlock::getProperties);
    public static final DeferredBlock<LiquidBlock> SLUSH = BLOCKS.registerBlock("slush", props -> new SlushLiquidBlock(ModFluids.SLUSH, props), SlushFluid::getBlockProperties);
}
