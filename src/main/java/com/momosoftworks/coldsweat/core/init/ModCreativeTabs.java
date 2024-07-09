package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.type.Insulator;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.serialization.ObjectBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.stream.Stream;

public class ModCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> ITEM_GROUPS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ColdSweat.MOD_ID);

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> COLD_SWEAT_TAB = ITEM_GROUPS.register("cold_sweat", () -> CreativeModeTab.builder()
            .icon(() -> ModItems.FILLED_WATERSKIN.value().getDefaultInstance())
            .displayItems((params, list) ->
            {
                list.acceptAll(List.of(
                        ModItems.WATERSKIN.value().getDefaultInstance(),
                        ObjectBuilder.build(() ->
                        {   ItemStack stack = ModItems.FILLED_WATERSKIN.value().getDefaultInstance();
                            stack = CompatManager.setWaterPurity(stack, 3);
                            return stack;
                        }),
                        ModItems.FUR.value().getDefaultInstance(),
                        ModItems.HOGLIN_HIDE.value().getDefaultInstance(),
                        ModItems.CHAMELEON_MOLT.value().getDefaultInstance(),
                        ModItems.MINECART_INSULATION.value().getDefaultInstance(),
                        ModItems.INSULATED_MINECART.value().getDefaultInstance(),
                        ObjectBuilder.build(() ->
                        {   ItemStack stack = ModItems.SOULSPRING_LAMP.value().getDefaultInstance();
                            stack.set(ModItemComponents.SOULSPRING_LAMP_LIT, true);
                            stack.set(ModItemComponents.SOULSPRING_LAMP_FUEL, 64d);
                            return stack;
                        }),
                        ModItems.SOUL_SPROUT.value().getDefaultInstance(),
                        ModItems.THERMOMETER.value().getDefaultInstance(),
                        ModItems.THERMOLITH.value().getDefaultInstance(),
                        ModItems.HEARTH.value().getDefaultInstance(),
                        ModItems.BOILER.value().getDefaultInstance(),
                        ModItems.ICEBOX.value().getDefaultInstance(),
                        ModItems.SMOKESTACK.value().getDefaultInstance(),
                        ModItems.SEWING_TABLE.value().getDefaultInstance(),
                        ModItems.HOGLIN_HEADPIECE.value().getDefaultInstance(),
                        ModItems.HOGLIN_TUNIC.value().getDefaultInstance(),
                        ModItems.HOGLIN_TROUSERS.value().getDefaultInstance(),
                        ModItems.HOGLIN_HOOVES.value().getDefaultInstance(),
                        ModItems.FUR_CAP.value().getDefaultInstance(),
                        ModItems.FUR_PARKA.value().getDefaultInstance(),
                        ModItems.FUR_PANTS.value().getDefaultInstance(),
                        ModItems.FUR_BOOTS.value().getDefaultInstance(),
                        ModItems.CHAMELEON_SPAWN_EGG.value().getDefaultInstance()
                ));
            })
            .title(Component.translatable("itemGroup.cold_sweat"))
            .build());

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> INSULATION_ITEMS_TAB = ITEM_GROUPS.register("cs_insulation_items", () -> CreativeModeTab.builder()
            .icon(() -> ModItems.CHAMELEON_MOLT.value().getDefaultInstance())
            .displayItems((params, list) ->
            {
                List<ItemStack> allInsulators = new ArrayList<>();
                Stream.of(
                        sort(ConfigSettings.INSULATION_ITEMS.get().entrySet()),
                        sort(ConfigSettings.INSULATING_ARMORS.get().entrySet()),
                        sort(ConfigSettings.INSULATING_CURIOS.get().entrySet())
                ).flatMap(Collection::stream).forEach(stack ->
                {
                    if (allInsulators.stream().noneMatch(s -> s.getItem() == stack.getItem()))
                    {   allInsulators.add(stack);
                    }
                });

                list.acceptAll(allInsulators);
            })
            .title(Component.translatable("itemGroup.cs_insulation_items"))
            .build());

    private static List<ItemStack> sort(Set<Map.Entry<Item, Insulator>> items)
    {   List<Map.Entry<Item, Insulator>> list = new ArrayList<>(items);
        // Sort by name first
        list.sort(Comparator.comparing(item -> item.getKey().getDefaultInstance().getDisplayName().getString()));
        // Sort by tags the items are in
        list.sort(Comparator.comparing(item -> item.getKey().getDefaultInstance().getTags().map(tag -> tag.location().toString()).reduce("", (a, b) -> a + b)));
        // Sort by armor material and slot
        list.sort(Comparator.comparing(item -> item.getKey() instanceof ArmorItem armor
                                               ? armor.getMaterial().getRegisteredName() + (3 - armor.getEquipmentSlot().getIndex())
                                               : ""));
        return list.stream().map(data -> new ItemStack(Holder.direct(data.getKey()), 1, data.getValue().data().components().components().asPatch())).toList();
    }
}
