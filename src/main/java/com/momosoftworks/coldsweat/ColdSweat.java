package com.momosoftworks.coldsweat;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.common.capability.shearing.ShearableFurCap;
import com.momosoftworks.coldsweat.common.capability.temperature.EntityTempCap;
import com.momosoftworks.coldsweat.common.capability.temperature.PlayerTempCap;
import com.momosoftworks.coldsweat.common.command.argument.*;
import com.momosoftworks.coldsweat.common.event.ConfigPostProcessor;
import com.momosoftworks.coldsweat.config.*;
import com.momosoftworks.coldsweat.config.spec.*;
import com.momosoftworks.coldsweat.core.advancement.trigger.ModAdvancementTriggers;
import com.momosoftworks.coldsweat.core.init.*;
import com.momosoftworks.coldsweat.core.itemgroup.InsulationItemsGroup;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.compat.CompatManager;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

import java.lang.reflect.Method;

@Mod(ColdSweat.MOD_ID)
public class ColdSweat
{
    public static final Logger LOGGER = LogManager.getLogger("Cold Sweat");

    public static final String MOD_ID = "cold_sweat";
    public static final IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();

    public ColdSweat()
    {
        MinecraftForge.EVENT_BUS.register(this);

        MOD_BUS.addListener(this::commonSetup);
        MOD_BUS.addListener(this::clientSetup);
        MOD_BUS.addListener(this::registerCaps);
        MOD_BUS.addListener(this::updateConfigs);
        if (CompatManager.isCuriosLoaded()) MOD_BUS.addListener(this::registerCurioSlots);
        MOD_BUS.addListener(this::createRegistries);

        // Register stuff
        BlockInit.BLOCKS.register(MOD_BUS);
        ItemInit.ITEMS.register(MOD_BUS);
        EntityInit.ENTITY_TYPES.register(MOD_BUS);
        BlockEntityInit.BLOCK_ENTITY_TYPES.register(MOD_BUS);
        MenuInit.MENU_TYPES.register(MOD_BUS);
        EffectInit.EFFECTS.register(MOD_BUS);
        ParticleTypesInit.PARTICLES.register(MOD_BUS);
        PotionInit.POTIONS.register(MOD_BUS);
        SoundInit.SOUNDS.register(MOD_BUS);
        FeatureInit.FEATURES.register(MOD_BUS);
        AttributeInit.ATTRIBUTES.register(MOD_BUS);
        TempEffectInit.TEMP_EFFECTS.register(MOD_BUS);

        // Handle config updates
        ModUpdater.updateFileNames();

        // Setup configs
        MainSettingsConfig.setup();
        ClientSettingsConfig.setup();
        WorldSettingsConfig.setup();
        ItemSettingsConfig.setup();
        EntitySettingsConfig.setup();

        // Setup compat
        CompatManager.registerEventHandlers();
    }

    public static ResourceLocation createKey(String path)
    {   return new ResourceLocation(MOD_ID, path);
    }

    public static String getVersion()
    {   return FMLLoader.getLoadingModList().getModFileById(ColdSweat.MOD_ID).versionString();
    }

    public void createRegistries(FMLLoadCompleteEvent event)
    {
        NewRegistryEvent dummyEvent = new NewRegistryEvent();
        for (ModRegistries.RegistryHolder<?> holder : ModRegistries.getRegistries().values())
        {   dummyEvent.create(new RegistryBuilder<>().setType((Class) holder.type()).setName(holder.registry().location()).dataPackRegistry(holder.codec(), (Codec) holder.codec()));
        }
        try
        {
            Method process = NewRegistryEvent.class.getDeclaredMethod("process");
            process.setAccessible(true);
            process.invoke(dummyEvent);
        }
        catch (Exception ignored) {}
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

            // Register insulation items tab
            InsulationItemsGroup.INSULATION_ITEMS.register();

            // Register custom command arguments
            ArgumentTypes.register("temperature_trait", TemperatureTraitArgument.class, new TemperatureTraitArgument.Serializer());
            ArgumentTypes.register("temp_attribute_trait", TempAttributeTraitArgument.class, new TempAttributeTraitArgument.Serializer());
            ArgumentTypes.register("temp_modifier_trait", TempModifierTraitArgument.class, new TempModifierTraitArgument.Serializer());
            ArgumentTypes.register("temp_modifier", TempModifierArgument.class, new TempModifierArgument.Serializer());
            ArgumentTypes.register("enum", NicerEnumArgument.class, new NicerEnumArgument.Serializer());
        });
    }

    public void clientSetup(final FMLClientSetupEvent event)
    {
        // Fix hearth transparency
        ItemBlockRenderTypes.setRenderLayer(BlockInit.HEARTH_BOTTOM.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(BlockInit.SOUL_STALK.get(), RenderType.cutoutMipped());
    }

    public void registerCaps(RegisterCapabilitiesEvent event)
    {   event.register(PlayerTempCap.class);
        event.register(EntityTempCap.class);
        event.register(ItemInsulationCap.class);
        event.register(ShearableFurCap.class);
    }

    public void updateConfigs(FMLLoadCompleteEvent event)
    {   ModUpdater.updateConfigs();
    }

    public void registerCurioSlots(InterModEnqueueEvent event)
    {
        event.enqueueWork(() ->
        {   InterModComms.sendTo(ColdSweat.MOD_ID, CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
                                 () -> SlotTypePreset.CHARM.getMessageBuilder().build());
        });
    }
}
