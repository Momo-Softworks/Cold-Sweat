package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.MissingMappingsEvent;
import com.momosoftworks.coldsweat.core.init.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class RemapMissingIDs
{
    @SubscribeEvent
    public static void remapMissingItems(MissingMappingsEvent event)
    {
        event.remap(BuiltInRegistries.ITEM).namespace(ColdSweat.MOD_ID).from("goat_fur_cap").to(ModItems.GOAT_FUR_HELMET);
        event.remap(BuiltInRegistries.ITEM).namespace(ColdSweat.MOD_ID).from("goat_fur_parka").to(ModItems.GOAT_FUR_CHESTPLATE);
        event.remap(BuiltInRegistries.ITEM).namespace(ColdSweat.MOD_ID).from("goat_fur_pants").to(ModItems.GOAT_FUR_LEGGINGS);
        event.remap(BuiltInRegistries.ITEM).namespace(ColdSweat.MOD_ID).from("hoglin_headpiece").to(ModItems.HOGLIN_HELMET);
        event.remap(BuiltInRegistries.ITEM).namespace(ColdSweat.MOD_ID).from("hoglin_tunic").to(ModItems.HOGLIN_CHESTPLATE);
        event.remap(BuiltInRegistries.ITEM).namespace(ColdSweat.MOD_ID).from("hoglin_trousers").to(ModItems.HOGLIN_LEGGINGS);
        event.remap(BuiltInRegistries.ITEM).namespace(ColdSweat.MOD_ID).from("hoglin_hooves").to(ModItems.HOGLIN_BOOTS);
        event.remap(BuiltInRegistries.ITEM).namespace(ColdSweat.MOD_ID).from("chameleon_scale_helmet").to(ModItems.CHAMELEON_HELMET);
        event.remap(BuiltInRegistries.ITEM).namespace(ColdSweat.MOD_ID).from("chameleon_scale_chestplate").to(ModItems.CHAMELEON_CHESTPLATE);
        event.remap(BuiltInRegistries.ITEM).namespace(ColdSweat.MOD_ID).from("chameleon_scale_leggings").to(ModItems.CHAMELEON_LEGGINGS);
        event.remap(BuiltInRegistries.ITEM).namespace(ColdSweat.MOD_ID).from("chameleon_scale_boots").to(ModItems.CHAMELEON_BOOTS);
    }
}
