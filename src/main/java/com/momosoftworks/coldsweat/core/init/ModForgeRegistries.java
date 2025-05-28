package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class ModForgeRegistries
{
    public static final DeferredRegister<TempEffectType<?>> TEMP_EFFECTS = DeferredRegister.create(new ResourceLocation(ColdSweat.MOD_ID, "temp_effect"), ColdSweat.MOD_ID);
    public static final Supplier<IForgeRegistry<TempEffectType<?>>> TEMP_EFFECTS_REGISTRY = TEMP_EFFECTS.makeRegistry(RegistryBuilder::new);
}
