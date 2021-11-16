package net.momostudios.coldsweat.core.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;

public class SoundInit
{
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ColdSweat.MOD_ID);

    public static final RegistryObject<SoundEvent> FREEZE_SOUND_REGISTRY = SOUNDS.register("entity.player.damage.freeze",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.player.damage.freeze")));
}
