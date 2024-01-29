package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.serialization.ObjectBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class CreativeTabInit
{
    public static final DeferredRegister<CreativeModeTab> ITEM_GROUPS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ColdSweat.MOD_ID);

    public static RegistryObject<CreativeModeTab> COLD_SWEAT_TAB = ITEM_GROUPS.register("cold_sweat", () -> CreativeModeTab.builder()
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
                        ModItems.SMOKESTACK.getDefaultInstance(),
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
            })
            .title(Component.translatable("itemGroup.cold_sweat"))
            .build());
}
