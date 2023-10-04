package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.serialization.ObjectBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegisterCreativeTab
{
    @SubscribeEvent
    public static void onCreativeTabRegister(CreativeModeTabEvent.Register event)
    {
        event.registerCreativeModeTab(new ResourceLocation(ColdSweat.MOD_ID, "cold_sweat"), builder ->
        {
            builder.title(Component.translatable("itemGroup.cold_sweat"))
                   .icon(() -> ModItems.FILLED_WATERSKIN.getDefaultInstance())
                   .displayItems((params, list) ->
                   {
                       list.acceptAll(List.of(
                               ModItems.WATERSKIN.getDefaultInstance(),
                               ModItems.FILLED_WATERSKIN.getDefaultInstance(),
                               ModItems.FUR.getDefaultInstance(),
                               ModItems.HOGLIN_HIDE.getDefaultInstance(),
                               ModItems.CHAMELEON_MOLT.getDefaultInstance(),
                               ModItems.MINECART_INSULATION.getDefaultInstance(),
                               ModItems.INSULATED_MINECART.getDefaultInstance(),
                               ObjectBuilder.build(() ->
                               {   ItemStack stack = ModItems.SOULSPRING_LAMP.getDefaultInstance();
                                   stack.getOrCreateTag().putBoolean("isOn", true);
                                   stack.getOrCreateTag().putDouble("fuel", 64);
                                   return stack;
                               }),
                               ModItems.SOUL_SPROUT.getDefaultInstance(),
                               ModItems.THERMOMETER.getDefaultInstance(),
                               ModItems.THERMOLITH.getDefaultInstance(),
                               ModItems.HEARTH.getDefaultInstance(),
                               ModItems.BOILER.getDefaultInstance(),
                               ModItems.ICEBOX.getDefaultInstance(),
                               ModItems.SEWING_TABLE.getDefaultInstance(),
                               ModItems.HOGLIN_HEADPIECE.getDefaultInstance(),
                               ModItems.HOGLIN_TUNIC.getDefaultInstance(),
                               ModItems.HOGLIN_TROUSERS.getDefaultInstance(),
                               ModItems.HOGLIN_HOOVES.getDefaultInstance(),
                               ModItems.FUR_CAP.getDefaultInstance(),
                               ModItems.FUR_PARKA.getDefaultInstance(),
                               ModItems.FUR_PANTS.getDefaultInstance(),
                               ModItems.FUR_BOOTS.getDefaultInstance(),
                               ModItems.CHAMELEON_SPAWN_EGG.getDefaultInstance()
                       ));
                   }).build();
        });
    }
}
