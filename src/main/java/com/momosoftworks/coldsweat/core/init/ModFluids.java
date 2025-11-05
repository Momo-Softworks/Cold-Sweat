package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.fluid.SlushFluid;
import com.momosoftworks.coldsweat.common.fluid.SlushFluidType;
import com.mrbysco.spoiled.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids
{
    /*
     Fluid Types
     */
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ColdSweat.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> SLUSH_TYPE = FLUID_TYPES.register("slush", () -> new SlushFluidType(SlushFluid.getFluidProperties()));

    /*
     Fluids
     */
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ColdSweat.MOD_ID);

    public static final DeferredHolder<Fluid, FlowingFluid> SLUSH = FLUIDS.register("slush", () -> new SlushFluid.Source(SlushFluid.getForgeProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SLUSH = FLUIDS.register("flowing_slush", () -> new SlushFluid.Flowing(SlushFluid.getForgeProperties()));
}
