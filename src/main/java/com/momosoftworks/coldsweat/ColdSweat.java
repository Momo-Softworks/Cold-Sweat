package com.momosoftworks.coldsweat;

import com.momosoftworks.coldsweat.client.renderer.entity.ChameleonEntityRenderer;
import com.momosoftworks.coldsweat.common.capability.*;
import com.momosoftworks.coldsweat.common.capability.insulation.IInsulatableCap;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.common.capability.shearing.IShearableCap;
import com.momosoftworks.coldsweat.common.capability.shearing.ShearableFurCap;
import com.momosoftworks.coldsweat.common.capability.temperature.EntityTempCap;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.common.command.argument.AbilityOrTempTypeArgument;
import com.momosoftworks.coldsweat.common.command.argument.TempModifierTypeArgument;
import com.momosoftworks.coldsweat.common.command.argument.TemperatureTypeArgument;
import com.momosoftworks.coldsweat.config.*;
import com.momosoftworks.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import com.momosoftworks.coldsweat.core.init.*;
import com.momosoftworks.coldsweat.core.itemgroup.InsulationItemsGroup;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.configuration.*;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.registries.ModBlocks;
import com.momosoftworks.coldsweat.util.registries.ModEntities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod(ColdSweat.MOD_ID)
public class ColdSweat
{
    public static final Logger LOGGER = LogManager.getLogger("Cold Sweat");

    public static final String MOD_ID = "cold_sweat";

    public ColdSweat()
    {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::registerCaps);
        if (CompatManager.isCuriosLoaded()) bus.addListener(this::registerCurioSlots);

        // Register stuff
        BlockInit.BLOCKS.register(bus);
        ItemInit.ITEMS.register(bus);
        EntityInit.ENTITY_TYPES.register(bus);
        BlockEntityInit.BLOCK_ENTITY_TYPES.register(bus);
        ContainerInit.MENU_TYPES.register(bus);
        EffectInit.EFFECTS.register(bus);
        ParticleTypesInit.PARTICLES.register(bus);
        PotionInit.POTIONS.register(bus);
        SoundInit.SOUNDS.register(bus);
        FeatureInit.FEATURES.register(bus);
        AttributeInit.ATTRIBUTES.register(bus);

        // Setup configs
        WorldSettingsConfig.setup();
        ItemSettingsConfig.setup();
        ColdSweatConfig.setup();
        ClientSettingsConfig.setup();
        EntitySettingsConfig.setup();

        // Setup JSON data-driven handlers
        bus.addListener((RegistryEvent.NewRegistry event) -> {
            ModRegistries.INSULATOR_DATA = new RegistryBuilder<InsulatorData>().setName(new ResourceLocation(ColdSweat.MOD_ID, "item/insulator")).setType(InsulatorData.class).create();
            ModRegistries.FUEL_DATA = new RegistryBuilder<FuelData>().setName(new ResourceLocation(ColdSweat.MOD_ID, "item/fuel")).setType(FuelData.class).create();
            ModRegistries.FOOD_DATA = new RegistryBuilder<ItemData>().setName(new ResourceLocation(ColdSweat.MOD_ID, "item/food")).setType(ItemData.class).create();
            ModRegistries.BLOCK_TEMP_DATA = new RegistryBuilder<BlockTempData>().setName(new ResourceLocation(ColdSweat.MOD_ID, "block/block_temp")).setType(BlockTempData.class).create();
            ModRegistries.BIOME_TEMP_DATA = new RegistryBuilder<BiomeTempData>().setName(new ResourceLocation(ColdSweat.MOD_ID, "world/biome_temp")).setType(BiomeTempData.class).create();
            ModRegistries.DIMENSION_TEMP_DATA = new RegistryBuilder<DimensionTempData>().setName(new ResourceLocation(ColdSweat.MOD_ID, "world/dimension_temp")).setType(DimensionTempData.class).create();
            ModRegistries.STRUCTURE_TEMP_DATA = new RegistryBuilder<StructureTempData>().setName(new ResourceLocation(ColdSweat.MOD_ID, "world/structure_temp")).setType(StructureTempData.class).create();
            ModRegistries.MOUNT_DATA = new RegistryBuilder<MountData>().setName(new ResourceLocation(ColdSweat.MOD_ID, "entity/mount")).setType(MountData.class).create();
            ModRegistries.ENTITY_SPAWN_BIOME_DATA = new RegistryBuilder<SpawnBiomeData>().setName(new ResourceLocation(ColdSweat.MOD_ID, "entity/spawn_biome")).setType(SpawnBiomeData.class).create();
        });
    }

    public void commonSetup(final FMLCommonSetupEvent event)
    {
        // Setup packets
        ColdSweatPacketHandler.init();
        event.enqueueWork(() ->
        {
            // Register advancement triggers
            CriteriaTriggers.register(ModAdvancementTriggers.TEMPERATURE_CHANGED);
            CriteriaTriggers.register(ModAdvancementTriggers.SOUL_LAMP_FUELLED);
            CriteriaTriggers.register(ModAdvancementTriggers.BLOCK_AFFECTS_TEMP);
            CriteriaTriggers.register(ModAdvancementTriggers.ARMOR_INSULATED);

            // Load configs to memory
            ConfigSettings.load();

            // Register insulation items tab
            InsulationItemsGroup.INSULATION_ITEMS.register();

            // Register custom command arguments
            ArgumentTypes.register("temperature", TemperatureTypeArgument.class, new TemperatureTypeArgument.Serializer());
            ArgumentTypes.register("temp_attribute", AbilityOrTempTypeArgument.class, new AbilityOrTempTypeArgument.Serializer());
            ArgumentTypes.register("temp_modifier", TempModifierTypeArgument.class, new TempModifierTypeArgument.Serializer());
        });
    }

    public void registerCaps(FMLCommonSetupEvent event)
    {
        /* Entity temperature */
        CapabilityManager.INSTANCE.register(ITemperatureCap.class, new DummyCapStorage<>(), EntityTempCap::new);

        /* Llama fur */
        CapabilityManager.INSTANCE.register(IShearableCap.class, new DummyCapStorage<>(), ShearableFurCap::new);

        /* Armor insulation */
        CapabilityManager.INSTANCE.register(IInsulatableCap.class, new DummyCapStorage<>(), ItemInsulationCap::new);
    }

    public void clientSetup(final FMLClientSetupEvent event)
    {
        RenderTypeLookup.setRenderLayer(ModBlocks.SOUL_STALK, RenderType.cutoutMipped());
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.CHAMELEON, ChameleonEntityRenderer::new);
    }

    public void registerCurioSlots(InterModEnqueueEvent event)
    {
        event.enqueueWork(() ->
        {   InterModComms.sendTo(ColdSweat.MOD_ID, CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
                                 () -> SlotTypePreset.CHARM.getMessageBuilder().build());
        });
    }
}
