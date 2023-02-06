package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundInit
{
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ColdSweat.MOD_ID);

    public static final RegistryObject<SoundEvent> FREEZE_SOUND_REGISTRY = SOUNDS.register("entity.player.damage.freeze",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.player.damage.freeze")));
    public static final RegistryObject<SoundEvent> SOUL_LAMP_ON_SOUND_REGISTRY = SOUNDS.register("item.soulspring_lamp.on",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "item.soulspring_lamp.on")));
    public static final RegistryObject<SoundEvent> SOUL_LAMP_OFF_SOUND_REGISTRY = SOUNDS.register("item.soulspring_lamp.off",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "item.soulspring_lamp.off")));
    public static final RegistryObject<SoundEvent> HEARTH_FUEL_SOUND_REGISTRY = SOUNDS.register("block.hearth.fuel",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "block.hearth.fuel")));
    public static final RegistryObject<SoundEvent> CHAMELEON_AMBIENT_SOUND_REGISTRY = SOUNDS.register("entity.chameleon.ambient",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.chameleon.ambient")));
    public static final RegistryObject<SoundEvent> CHAMELEON_HURT_SOUND_REGISTRY = SOUNDS.register("entity.chameleon.hurt",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.chameleon.hurt")));
}
