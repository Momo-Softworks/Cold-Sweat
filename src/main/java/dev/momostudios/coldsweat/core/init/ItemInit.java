package dev.momostudios.coldsweat.core.init;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.block.*;
import dev.momostudios.coldsweat.common.item.*;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.util.registries.ModArmorMaterials;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemInit
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ColdSweat.MOD_ID);

    // Items
    public static final RegistryObject<Item> WATERSKIN = ITEMS.register("waterskin", WaterskinItem::new);
    public static final RegistryObject<Item> FILLED_WATERSKIN = ITEMS.register("filled_waterskin", FilledWaterskinItem::new);
    public static final RegistryObject<Item> MINECART_INSULATION = ITEMS.register("minecart_insulation", MinecartInsulationItem::new);
    public static final RegistryObject<Item> THERMOMETER = ITEMS.register("thermometer", () ->
            new Item(new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT).rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> SOULSPRING_LAMP = ITEMS.register("soulspring_lamp", SoulspringLampItem::new);
    public static final RegistryObject<Item> LLAMA_FUR = ITEMS.register("llama_fur", () ->
            new Item(new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));
    public static final RegistryObject<Item> HOGLIN_HIDE = ITEMS.register("hoglin_hide", () ->
            new Item(new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));
    public static final RegistryObject<Item> INSULATED_MINECART = ITEMS.register("insulated_minecart", () ->
            new InsulatedMinecartItem(new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1)));
    public static final RegistryObject<Item> CHAMELEON_MOLT = ITEMS.register("chameleon_molt", () ->
            new Item(new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));

    // Armor Items
    public static final RegistryObject<Item> HOGLIN_HEADPIECE = ITEMS.register("hoglin_headpiece", () ->
            new HoglinArmorItem(ModArmorMaterials.HOGLIN, EquipmentSlotType.HEAD, new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));

    public static final RegistryObject<Item> HOGLIN_TUNIC = ITEMS.register("hoglin_tunic", () ->
            new HoglinArmorItem(ModArmorMaterials.HOGLIN, EquipmentSlotType.CHEST, new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));

    public static final RegistryObject<Item> HOGLIN_TROUSERS = ITEMS.register("hoglin_trousers", () ->
            new HoglinArmorItem(ModArmorMaterials.HOGLIN, EquipmentSlotType.LEGS, new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));

    public static final RegistryObject<Item> HOGLIN_HOOVES = ITEMS.register("hoglin_hooves", () ->
            new HoglinArmorItem(ModArmorMaterials.HOGLIN, EquipmentSlotType.FEET, new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));

    public static final RegistryObject<Item> LLAMA_FUR_CAP = ITEMS.register("llama_fur_cap", () ->
            new LlamaArmorItem(ModArmorMaterials.LLAMA, EquipmentSlotType.HEAD, new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));
    public static final RegistryObject<Item> LLAMA_FUR_PARKA = ITEMS.register("llama_fur_parka", () ->
            new LlamaArmorItem(ModArmorMaterials.LLAMA, EquipmentSlotType.CHEST, new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));
    public static final RegistryObject<Item> LLAMA_FUR_PANTS = ITEMS.register("llama_fur_pants", () ->
            new LlamaArmorItem(ModArmorMaterials.LLAMA, EquipmentSlotType.LEGS, new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));
    public static final RegistryObject<Item> LLAMA_FUR_BOOTS = ITEMS.register("llama_fur_boots", () ->
            new LlamaArmorItem(ModArmorMaterials.LLAMA, EquipmentSlotType.FEET, new Item.Properties().tab(ColdSweatGroup.COLD_SWEAT)));

    // Block Items
    public static final RegistryObject<BlockItem> BOILER = ITEMS.register("boiler", () -> new BlockItem(BlockInit.BOILER.get(), BoilerBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> ICEBOX = ITEMS.register("icebox", () -> new BlockItem(BlockInit.ICEBOX.get(), IceboxBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> SEWING_TABLE = ITEMS.register("sewing_table", () -> new BlockItem(BlockInit.SEWING_TABLE.get(), SewingTableBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> HEARTH = ITEMS.register("hearth", () -> new BlockItem(BlockInit.HEARTH_BOTTOM.get(), HearthBottomBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> THERMOLITH = ITEMS.register("thermolith", () -> new BlockItem(BlockInit.THERMOLITH.get(), ThermolithBlock.getItemProperties()));
    public static final RegistryObject<BlockItem> SOUL_SPROUT = ITEMS.register("soul_sprout", () -> new SoulSproutItem(BlockInit.SOUL_STALK.get(),
            SoulStalkBlock.getItemProperties().food(new Food.Builder().nutrition(4).saturationMod(1).alwaysEat().fast().build())));

    // Spawn Eggs
    public static final RegistryObject<ForgeSpawnEggItem> CHAMELEON_SPAWN_EGG = ITEMS.register("chameleon_spawn_egg", () ->
            new ForgeSpawnEggItem(EntityInit.CHAMELEON, 0x82C841, 0x1C9170, new Item.Properties().tab(ItemGroup.TAB_MISC)));
}
