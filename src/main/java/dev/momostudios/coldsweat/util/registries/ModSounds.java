package dev.momostudios.coldsweat.util.registries;

import dev.momostudios.coldsweat.core.init.SoundInit;
import net.minecraft.sounds.SoundEvent;

public class ModSounds
{
    public static SoundEvent FREEZE_DAMAGE = SoundInit.FREEZE_SOUND_REGISTRY.get();
    public static SoundEvent CHAMELEON_AMBIENT = SoundInit.CHAMELEON_AMBIENT_SOUND_REGISTRY.get();
    public static SoundEvent CHAMELEON_HURT = SoundInit.CHAMELEON_HURT_SOUND_REGISTRY.get();

    public static SoundEvent NETHER_LAMP_ON = SoundInit.SOUL_LAMP_ON_SOUND_REGISTRY.get();
    public static SoundEvent NETHER_LAMP_OFF = SoundInit.SOUL_LAMP_OFF_SOUND_REGISTRY.get();

    public static SoundEvent HEARTH_FUEL = SoundInit.HEARTH_FUEL_SOUND_REGISTRY.get();
}
