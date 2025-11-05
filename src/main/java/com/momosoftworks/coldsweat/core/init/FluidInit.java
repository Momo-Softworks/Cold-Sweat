package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.fluid.SlushFluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FluidInit
{
    /*
     Fluids
     */
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, ColdSweat.MOD_ID);

    public static final RegistryObject<FlowingFluid> SLUSH = FLUIDS.register("slush", () -> new SlushFluid.Source(SlushFluid.getForgeProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_SLUSH = FLUIDS.register("flowing_slush", () -> new SlushFluid.Flowing(SlushFluid.getForgeProperties()));

}
