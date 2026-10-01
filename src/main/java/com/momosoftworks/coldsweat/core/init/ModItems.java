package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.*;
import com.momosoftworks.coldsweat.common.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ColdSweat.MOD_ID);

    // Items
    public static final DeferredItem<Item> WATERSKIN = ITEMS.registerItem("waterskin", WaterskinItem::new, WaterskinItem::getDefaultProperties);
    public static final DeferredItem<Item> FILLED_WATERSKIN = ITEMS.registerItem("filled_waterskin", FilledWaterskinItem::new, FilledWaterskinItem::getDefaultProperties);
    public static final DeferredItem<Item> MINECART_INSULATION = ITEMS.registerItem("minecart_insulation", MinecartInsulationItem::new, MinecartInsulationItem::getDefaultProperties);
    public static final DeferredItem<Item> THERMOMETER = ITEMS.registerItem("thermometer", ThermometerItem::new, () ->
            new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
    public static final DeferredItem<Item> SOULSPRING_LAMP = ITEMS.registerItem("soulspring_lamp", SoulspringLampItem::new, SoulspringLampItem::getDefaultProperties);
    public static final DeferredItem<Item> GOAT_FUR = ITEMS.registerSimpleItem("goat_fur");
    public static final DeferredItem<Item> HOGLIN_HIDE = ITEMS.registerSimpleItem("hoglin_hide");
    public static final DeferredItem<Item> INSULATED_MINECART = ITEMS.registerItem("insulated_minecart", InsulatedMinecartItem::new, () ->
            new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> CHAMELEON_MOLT = ITEMS.registerSimpleItem("chameleon_molt");
    public static final DeferredItem<Item> SLUSH_BUCKET = ITEMS.registerItem("slush_bucket", props -> new BucketItem(ModFluids.SLUSH.value(), props), () ->
            new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET));

    // Armor Items
    public static final DeferredItem<Item> HOGLIN_HELMET = armor("hoglin_helmet", HoglinArmorItem::new, ModArmorMaterials.HOGLIN, ArmorType.HELMET);
    public static final DeferredItem<Item> HOGLIN_CHESTPLATE = armor("hoglin_chestplate", HoglinArmorItem::new, ModArmorMaterials.HOGLIN, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> HOGLIN_LEGGINGS = armor("hoglin_leggings", HoglinArmorItem::new, ModArmorMaterials.HOGLIN, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> HOGLIN_BOOTS = armor("hoglin_boots", HoglinArmorItem::new, ModArmorMaterials.HOGLIN, ArmorType.BOOTS);

    public static final DeferredItem<Item> GOAT_FUR_HELMET = armor("goat_fur_helmet", GoatArmorItem::new, ModArmorMaterials.GOAT_FUR, ArmorType.HELMET);
    public static final DeferredItem<Item> GOAT_FUR_CHESTPLATE = armor("goat_fur_chestplate", GoatArmorItem::new, ModArmorMaterials.GOAT_FUR, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> GOAT_FUR_LEGGINGS = armor("goat_fur_leggings", GoatArmorItem::new, ModArmorMaterials.GOAT_FUR, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> GOAT_FUR_BOOTS = armor("goat_fur_boots", GoatArmorItem::new, ModArmorMaterials.GOAT_FUR, ArmorType.BOOTS);

    public static final DeferredItem<Item> CHAMELEON_HELMET = armor("chameleon_helmet", ChameleonArmorItem::new, ModArmorMaterials.CHAMELEON, ArmorType.HELMET);
    public static final DeferredItem<Item> CHAMELEON_CHESTPLATE = armor("chameleon_chestplate", ChameleonArmorItem::new, ModArmorMaterials.CHAMELEON, ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> CHAMELEON_LEGGINGS = armor("chameleon_leggings", ChameleonArmorItem::new, ModArmorMaterials.CHAMELEON, ArmorType.LEGGINGS);
    public static final DeferredItem<Item> CHAMELEON_BOOTS = armor("chameleon_boots", ChameleonArmorItem::new, ModArmorMaterials.CHAMELEON, ArmorType.BOOTS);

    // Block Items
    public static final DeferredItem<BlockItem> BOILER = blockItem("boiler", ModBlocks.BOILER, BoilerBlock::getItemProperties);
    public static final DeferredItem<BlockItem> ICEBOX = blockItem("icebox", ModBlocks.ICEBOX, IceboxBlock::getItemProperties);
    public static final DeferredItem<BlockItem> SEWING_TABLE = blockItem("sewing_table", ModBlocks.SEWING_TABLE, SewingTableBlock::getItemProperties);
    public static final DeferredItem<BlockItem> HEARTH = blockItem("hearth", ModBlocks.HEARTH_BOTTOM, HearthBottomBlock::getItemProperties);
    public static final DeferredItem<BlockItem> THERMOLITH = blockItem("thermolith", ModBlocks.THERMOLITH, ThermolithBlock::getItemProperties);
    public static final DeferredItem<BlockItem> SOUL_SPROUT = ITEMS.registerItem("soul_sprout", props -> new SoulSproutItem(ModBlocks.SOUL_STALK.get(), props), () ->
            SoulStalkBlock.getItemProperties().useItemDescriptionPrefix().food(new FoodProperties.Builder()
                                                            .nutrition(3)
                                                            .saturationModifier(0.5f)
                                                            .alwaysEdible()
                                                            .build(),
                                                    Consumables.defaultFood().consumeSeconds(0.8F).build()));
    public static final DeferredItem<BlockItem> SMOKESTACK = blockItem("smokestack", ModBlocks.SMOKESTACK, SmokestackBlock::getItemProperties);

    // Spawn Eggs
    public static final DeferredItem<Item> CHAMELEON_SPAWN_EGG = ITEMS.registerItem("chameleon_spawn_egg", SpawnEggItem::new, () ->
            new Item.Properties().spawnEgg(ModEntities.CHAMELEON.get()));

    private static DeferredItem<Item> armor(String name, Function<Item.Properties, ? extends Item> factory, ArmorMaterial material, ArmorType type)
    {   return ITEMS.registerItem(name, factory, () -> new Item.Properties().humanoidArmor(material, type));
    }

    private static DeferredItem<BlockItem> blockItem(String name, DeferredBlock<? extends Block> block, Supplier<Item.Properties> properties)
    {   return ITEMS.registerItem(name, props -> new BlockItem(block.get(), props), () -> properties.get().useBlockDescriptionPrefix());
    }
}
