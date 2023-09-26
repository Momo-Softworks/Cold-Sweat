package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.core.init.SoundInit;
import net.minecraft.sounds.SoundEvent;

public class ModSounds
{
    public static SoundEvent FREEZE_DAMAGE = SoundInit.FREEZE_SOUND_REGISTRY.get();
    public static SoundEvent CHAMELEON_AMBIENT = SoundInit.CHAMELEON_AMBIENT_SOUND_REGISTRY.get();
    public static SoundEvent CHAMELEON_HURT = SoundInit.CHAMELEON_HURT_SOUND_REGISTRY.get();
    public static SoundEvent CHAMELEON_DEATH = SoundInit.CHAMELEON_DEATH_SOUND_REGISTRY.get();
    public static SoundEvent CHAMELEON_FIND = SoundInit.CHAMELEON_FIND_SOUND_REGISTRY.get();
    public static SoundEvent CHAMELEON_TONGUE_IN = SoundInit.CHAMELEON_TONGUE_IN_SOUND_REGISTRY.get();
    public static SoundEvent CHAMELEON_TONGUE_OUT = SoundInit.CHAMELEON_TONGUE_OUT_SOUND_REGISTRY.get();
    public static SoundEvent CHAMELEON_SHED = SoundInit.CHAMELEON_SHED_SOUND_REGISTRY.get();

    public static SoundEvent NETHER_LAMP_ON = SoundInit.SOUL_LAMP_ON_SOUND_REGISTRY.get();
    public static SoundEvent NETHER_LAMP_OFF = SoundInit.SOUL_LAMP_OFF_SOUND_REGISTRY.get();

    public static SoundEvent HEARTH_FUEL = SoundInit.HEARTH_FUEL_SOUND_REGISTRY.get();
}
