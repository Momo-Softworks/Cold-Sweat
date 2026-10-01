package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

public class ModArmorMaterials
{
    public static final ResourceKey<EquipmentAsset> HOGLIN_ASSET = createAsset("hoglin");
    public static final ResourceKey<EquipmentAsset> GOAT_FUR_ASSET = createAsset("goat_fur");
    public static final ResourceKey<EquipmentAsset> CHAMELEON_ASSET = createAsset("chameleon");

    public static final ArmorMaterial HOGLIN = new ArmorMaterial(14, defense(3, 6, 5, 2), 25, SoundEvents.ARMOR_EQUIP_LEATHER,
                                                                 1.5F, 0.0F, ModItemTags.HOGLIN_LEATHERS, HOGLIN_ASSET);

    public static final ArmorMaterial GOAT_FUR = new ArmorMaterial(10, defense(2, 5, 4, 1), 15, SoundEvents.ARMOR_EQUIP_LEATHER,
                                                                   0.0F, 0.0F, ModItemTags.GOAT_FURS, GOAT_FUR_ASSET);

    public static final ArmorMaterial CHAMELEON = new ArmorMaterial(12, defense(2, 6, 5, 2), 15, ModSounds.ARMOR_EQUIP_CHAMELEON,
                                                                    0.0F, 0.0F, ModItemTags.CHAMELEON_SCALES, CHAMELEON_ASSET);

    private static Map<ArmorType, Integer> defense(int helmet, int chestplate, int leggings, int boots)
    {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.HELMET, helmet);
        map.put(ArmorType.CHESTPLATE, chestplate);
        map.put(ArmorType.LEGGINGS, leggings);
        map.put(ArmorType.BOOTS, boots);
        map.put(ArmorType.BODY, chestplate);
        return map;
    }

    private static ResourceKey<EquipmentAsset> createAsset(String name)
    {   return ResourceKey.create(EquipmentAssets.ROOT_ID, ColdSweat.createKey(name));
    }
}
