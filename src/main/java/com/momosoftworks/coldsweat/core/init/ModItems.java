package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.block.*;
import com.momosoftworks.coldsweat.common.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ColdSweat.MOD_ID);

    // Items
    public static final DeferredItem<Item> WATERSKIN = ITEMS.register("waterskin", WaterskinItem::new);
    public static final DeferredItem<Item> FILLED_WATERSKIN = ITEMS.register("filled_waterskin", FilledWaterskinItem::new);
    public static final DeferredItem<Item> MINECART_INSULATION = ITEMS.register("minecart_insulation", MinecartInsulationItem::new);
    public static final DeferredItem<Item> THERMOMETER = ITEMS.register("thermometer", () ->
            new ThermometerItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final DeferredItem<Item> SOULSPRING_LAMP = ITEMS.register("soulspring_lamp", SoulspringLampItem::new);
    public static final DeferredItem<Item> FUR = ITEMS.register("fur", () ->
            new Item(new Item.Properties()));
    public static final DeferredItem<Item> HOGLIN_HIDE = ITEMS.register("hoglin_hide", () ->
            new Item(new Item.Properties()));
    public static final DeferredItem<Item> INSULATED_MINECART = ITEMS.register("insulated_minecart", () ->
            new InsulatedMinecartItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CHAMELEON_MOLT = ITEMS.register("chameleon_molt", () ->
            new Item(new Item.Properties()));

    // Armor Items
    public static final DeferredItem<Item> HOGLIN_HEADPIECE = ITEMS.register("hoglin_headpiece", () ->
            new HoglinArmorItem(ModArmorMaterials.HOGLIN, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final DeferredItem<Item> HOGLIN_TUNIC = ITEMS.register("hoglin_tunic", () ->
            new HoglinArmorItem(ModArmorMaterials.HOGLIN, ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    public static final DeferredItem<Item> HOGLIN_TROUSERS = ITEMS.register("hoglin_trousers", () ->
            new HoglinArmorItem(ModArmorMaterials.HOGLIN, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final DeferredItem<Item> HOGLIN_HOOVES = ITEMS.register("hoglin_hooves", () ->
            new HoglinArmorItem(ModArmorMaterials.HOGLIN, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<Item> FUR_CAP = ITEMS.register("fur_cap", () ->
            new GoatArmorItem(ModArmorMaterials.FUR, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> FUR_PARKA = ITEMS.register("fur_parka", () ->
            new GoatArmorItem(ModArmorMaterials.FUR, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> FUR_PANTS = ITEMS.register("fur_pants", () ->
            new GoatArmorItem(ModArmorMaterials.FUR, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<Item> FUR_BOOTS = ITEMS.register("fur_boots", () ->
            new GoatArmorItem(ModArmorMaterials.FUR, ArmorItem.Type.BOOTS, new Item.Properties()));

    // Block Items
    public static final DeferredItem<BlockItem> BOILER = ITEMS.register("boiler", () -> new BlockItem(ModBlocks.BOILER.get(), BoilerBlock.getItemProperties()));
    public static final DeferredItem<BlockItem> ICEBOX = ITEMS.register("icebox", () -> new BlockItem(ModBlocks.ICEBOX.get(), IceboxBlock.getItemProperties()));
    public static final DeferredItem<BlockItem> SEWING_TABLE = ITEMS.register("sewing_table", () -> new BlockItem(ModBlocks.SEWING_TABLE.get(), SewingTableBlock.getItemProperties()));
    public static final DeferredItem<BlockItem> HEARTH = ITEMS.register("hearth", () -> new BlockItem(ModBlocks.HEARTH_BOTTOM.get(), HearthBottomBlock.getItemProperties()));
    public static final DeferredItem<BlockItem> THERMOLITH = ITEMS.register("thermolith", () -> new BlockItem(ModBlocks.THERMOLITH.get(), ThermolithBlock.getItemProperties()));
    public static final DeferredItem<BlockItem> SOUL_SPROUT = ITEMS.register("soul_sprout", () -> new SoulSproutItem(ModBlocks.SOUL_STALK.get(),
                                                                                                                     SoulStalkBlock.getItemProperties().food(new FoodProperties.Builder()
                                                                                                                                                             .nutrition(3)
                                                                                                                                                             .saturationModifier(0.5f)
                                                                                                                                                             .alwaysEdible()
                                                                                                                                                             .fast()
                                                                                                                                                             .build())));
    public static final DeferredItem<BlockItem> SMOKESTACK = ITEMS.register("smokestack", () -> new BlockItem(ModBlocks.SMOKESTACK.get(), SmokestackBlock.getItemProperties()));

    // Spawn Eggs
    public static final DeferredItem<DeferredSpawnEggItem> CHAMELEON_SPAWN_EGG = ITEMS.register("chameleon_spawn_egg", () ->
            new DeferredSpawnEggItem(ModEntities.CHAMELEON, 0x82C841, 0x1C9170, new Item.Properties()));
}
