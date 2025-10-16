package com.momosoftworks.coldsweat.util.registries;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.tag.ModItemTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial
{
    HOGLIN("hoglin", 14, new int[]{2, 5, 6, 3}, 25, () -> SoundEvents.ARMOR_EQUIP_LEATHER,
           1.5F, 0.0F, () -> Ingredient.of(ModItemTags.HOGLIN_LEATHERS)),
    GOAT_FUR("goat_fur", 10, new int[]{1, 4, 5, 2}, 15, () -> SoundEvents.ARMOR_EQUIP_LEATHER,
             0F, 0.0F, () -> Ingredient.of(ModItemTags.GOAT_FURS)),
    CHAMELEON("chameleon", 12, new int[]{2, 5, 6, 2}, 15, () -> ModSounds.ARMOR_EQUIP_CHAMELEON,
                0F, 0.0F, () -> Ingredient.of(ModItemTags.CHAMELEON_SCALES));

    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantmentValue;
    private final Supplier<SoundEvent> sound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durability, int[] protection, int enchantability, Supplier<SoundEvent> equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durability;
        this.slotProtections = protection;
        this.enchantmentValue = enchantability;
        this.sound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
    }

    public int getDurabilityForSlot(EquipmentSlot p_40484_) {
        return HEALTH_PER_SLOT[p_40484_.getIndex()] * this.durabilityMultiplier;
    }

    public int getDefenseForSlot(EquipmentSlot p_40487_) {
        return this.slotProtections[p_40487_.getIndex()];
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public SoundEvent getEquipSound() {
        return this.sound.get();
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public String getName() {
        return ColdSweat.MOD_ID + ":" + this.name;
    }

    public float getToughness() {
        return this.toughness;
    }

    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}