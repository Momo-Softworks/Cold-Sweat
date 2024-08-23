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
    public static final RegistryObject<SoundEvent> ICEBOX_OPEN_SOUND_REGISTRY = SOUNDS.register("block.icebox.open",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "block.icebox.open")));
    public static final RegistryObject<SoundEvent> ICEBOX_CLOSE_SOUND_REGISTRY = SOUNDS.register("block.icebox.close",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "block.icebox.close")));

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

    public static final RegistryObject<SoundEvent> ENTITY_GOAT_AMBIENT = SOUNDS.register("entity.goat.ambient",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.ambient")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_DEATH = SOUNDS.register("entity.goat.death",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.death")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_EAT = SOUNDS.register("entity.goat.eat",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.eat")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_HURT = SOUNDS.register("entity.goat.hurt",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.hurt")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_LONG_JUMP = SOUNDS.register("entity.goat.long_jump",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.long_jump")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_MILK = SOUNDS.register("entity.goat.milk",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.milk")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_PREPARE_RAM = SOUNDS.register("entity.goat.prepare_ram",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.prepare_ram")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_RAM_IMPACT = SOUNDS.register("entity.goat.ram_impact",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.ram_impact")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_SCREAMING_AMBIENT = SOUNDS.register("entity.goat.screaming.ambient",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.screaming.ambient")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_SCREAMING_DEATH = SOUNDS.register("entity.goat.screaming.death",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.screaming.death")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_SCREAMING_EAT = SOUNDS.register("entity.goat.screaming.eat",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.screaming.eat")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_SCREAMING_HURT = SOUNDS.register("entity.goat.screaming.hurt",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.screaming.hurt")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_SCREAMING_LONG_JUMP = SOUNDS.register("entity.goat.screaming.long_jump",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.screaming.long_jump")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_SCREAMING_MILK = SOUNDS.register("entity.goat.screaming.milk",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.screaming.milk")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_SCREAMING_PREPARE_RAM = SOUNDS.register("entity.goat.screaming.prepare_ram",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.screaming.prepare_ram")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_SCREAMING_RAM_IMPACT = SOUNDS.register("entity.goat.screaming.ram_impact",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.screaming.ram_impact")));
    public static final RegistryObject<SoundEvent> ENTITY_GOAT_STEP = SOUNDS.register("entity.goat.step",
            () -> new SoundEvent(new ResourceLocation(ColdSweat.MOD_ID, "entity.goat.step")));
}
