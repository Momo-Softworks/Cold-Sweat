package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundInit
{
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ColdSweat.MOD_ID);

    public static final RegistryObject<SoundEvent> FREEZE_SOUND_REGISTRY = SOUNDS.register("entity.player.damage.freeze",
                                                                                           () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.player.damage.freeze")));

    public static final RegistryObject<SoundEvent> SOUL_LAMP_ON_SOUND_REGISTRY = SOUNDS.register("item.soulspring_lamp.on",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "item.soulspring_lamp.on")));
    public static final RegistryObject<SoundEvent> SOUL_LAMP_OFF_SOUND_REGISTRY = SOUNDS.register("item.soulspring_lamp.off",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "item.soulspring_lamp.off")));

    public static final RegistryObject<SoundEvent> WATERSKIN_POUR_SOUND_REGISTRY = SOUNDS.register("item.waterskin.pour",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "item.waterskin.pour")));
    public static final RegistryObject<SoundEvent> WATERSKIN_FILL_SOUND_REGISTRY = SOUNDS.register("item.waterskin.fill",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "item.waterskin.fill")));

    public static final RegistryObject<SoundEvent> HEARTH_DEPLETE_SOUND_REGISTRY = SOUNDS.register("block.hearth.fuel_deplete",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "block.hearth.fuel_deplete")));
    public static final RegistryObject<SoundEvent> BOILER_DEPLETE_SOUND_REGISTRY = SOUNDS.register("block.boiler.fuel_deplete",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "block.boiler.fuel_deplete")));
    public static final RegistryObject<SoundEvent> ICEBOX_DEPLETE_SOUND_REGISTRY = SOUNDS.register("block.icebox.fuel_deplete",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "block.icebox.fuel_deplete")));

    public static final RegistryObject<SoundEvent> CHAMELEON_AMBIENT_SOUND_REGISTRY = SOUNDS.register("entity.chameleon.ambient",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.chameleon.ambient")));
    public static final RegistryObject<SoundEvent> CHAMELEON_HURT_SOUND_REGISTRY = SOUNDS.register("entity.chameleon.hurt",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.chameleon.hurt")));
    public static final RegistryObject<SoundEvent> CHAMELEON_DEATH_SOUND_REGISTRY = SOUNDS.register("entity.chameleon.death",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.chameleon.death")));
    public static final RegistryObject<SoundEvent> CHAMELEON_FIND_SOUND_REGISTRY = SOUNDS.register("entity.chameleon.find",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.chameleon.find")));
    public static final RegistryObject<SoundEvent> CHAMELEON_TONGUE_IN_SOUND_REGISTRY = SOUNDS.register("entity.chameleon.tongue.in",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.chameleon.tongue.in")));
    public static final RegistryObject<SoundEvent> CHAMELEON_TONGUE_OUT_SOUND_REGISTRY = SOUNDS.register("entity.chameleon.tongue.out",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.chameleon.tongue.out")));
    public static final RegistryObject<SoundEvent> CHAMELEON_SHED_SOUND_REGISTRY = SOUNDS.register("entity.chameleon.shed",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.chameleon.shed")));
}
