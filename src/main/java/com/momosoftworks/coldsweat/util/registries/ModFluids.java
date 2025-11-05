package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.core.init.FluidInit;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.fluids.FluidType;

public class ModFluids
{
    public static final FlowingFluid SLUSH = FluidInit.SLUSH.get();
    public static final FlowingFluid FLOWING_SLUSH = FluidInit.FLOWING_SLUSH.get();
    public static final FluidType SLUSH_TYPE = FluidInit.SLUSH_TYPE.get();
}
