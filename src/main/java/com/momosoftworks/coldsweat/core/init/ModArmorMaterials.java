package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials
{
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, ColdSweat.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HOGLIN = ARMOR_MATERIALS.register("hoglin", () -> new ArmorMaterial(
            Util.make(new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 2);
                map.put(ArmorItem.Type.CHESTPLATE, 5);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.BOOTS, 3);
            }), 25, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(ModItems.HOGLIN_HIDE),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "hoglin"))),
            1.5F, 0.0F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FUR = ARMOR_MATERIALS.register("fur", () -> new ArmorMaterial(
            Util.make(new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 1);
                map.put(ArmorItem.Type.CHESTPLATE, 4);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.BOOTS, 2);
            }), 15, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(ModItems.FUR),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "fur"))),
            0.0F, 0.0F));
}