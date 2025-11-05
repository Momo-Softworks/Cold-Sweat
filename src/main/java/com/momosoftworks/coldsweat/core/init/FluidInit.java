package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.fluid.SlushFluid;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidInit
{
    /*
     Fluids
     */
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, ColdSweat.MOD_ID);

    public static final RegistryObject<FlowingFluid> SLUSH = FLUIDS.register("slush", () -> new SlushFluid.Source(SlushFluid.getForgeProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_SLUSH = FLUIDS.register("flowing_slush", () -> new SlushFluid.Flowing(SlushFluid.getForgeProperties()));

}
