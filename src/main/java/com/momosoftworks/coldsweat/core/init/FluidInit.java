package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.fluid.SlushFluid;
import com.momosoftworks.coldsweat.common.fluid.SlushFluidType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FluidInit
{
    /*
     Fluid Types
     */
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, ColdSweat.MOD_ID);

    public static final RegistryObject<FluidType> SLUSH_TYPE = FLUID_TYPES.register("slush", () -> new SlushFluidType(SlushFluid.getFluidProperties()));

    /*
     Fluids
     */
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, ColdSweat.MOD_ID);

    public static final RegistryObject<FlowingFluid> SLUSH = FLUIDS.register("slush", () -> new SlushFluid.Source(SlushFluid.getForgeProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_SLUSH = FLUIDS.register("flowing_slush", () -> new SlushFluid.Flowing(SlushFluid.getForgeProperties()));

}
