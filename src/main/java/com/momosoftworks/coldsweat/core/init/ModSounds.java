package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds
{
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ColdSweat.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> FREEZE = SOUNDS.register("entity.player.damage.freeze",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "entity.player.damage.freeze")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_LAMP_ON = SOUNDS.register("item.soulspring_lamp.on",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "item.soulspring_lamp.on")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_LAMP_OFF = SOUNDS.register("item.soulspring_lamp.off",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "item.soulspring_lamp.off")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WATERSKIN_POUR = SOUNDS.register("item.waterskin.pour",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "item.waterskin.pour")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WATERSKIN_FILL = SOUNDS.register("item.waterskin.fill",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "item.waterskin.fill")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTH_DEPLETE = SOUNDS.register("block.hearth.fuel_deplete",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "block.hearth.fuel_deplete")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BOILER_DEPLETE = SOUNDS.register("block.boiler.fuel_deplete",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "block.boiler.fuel_deplete")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ICEBOX_DEPLETE = SOUNDS.register("block.icebox.fuel_deplete",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "block.icebox.fuel_deplete")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ICEBOX_OPEN = SOUNDS.register("block.icebox.open",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "block.icebox.open")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ICEBOX_CLOSE = SOUNDS.register("block.icebox.close",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "block.icebox.close")));

    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_AMBIENT = SOUNDS.register("entity.chameleon.ambient",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "entity.chameleon.ambient")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_HURT = SOUNDS.register("entity.chameleon.hurt",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "entity.chameleon.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_DEATH = SOUNDS.register("entity.chameleon.death",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "entity.chameleon.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_FIND = SOUNDS.register("entity.chameleon.find",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "entity.chameleon.find")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_TONGUE_IN = SOUNDS.register("entity.chameleon.tongue.in",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "entity.chameleon.tongue.in")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_TONGUE_OUT = SOUNDS.register("entity.chameleon.tongue.out",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "entity.chameleon.tongue.out")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAMELEON_SHED = SOUNDS.register("entity.chameleon.shed",
                                              () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "entity.chameleon.shed")));
}
