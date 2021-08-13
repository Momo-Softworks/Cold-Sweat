package net.momostudios.coldsweat.core.init;

import com.mojang.datafixers.DSL;
import com.mojang.serialization.Lifecycle;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.item.WaterskinItem;
import net.momostudios.coldsweat.common.temperature.modifier.BiomeTempModifier;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.common.temperature.modifier.TimeTempModifier;
import net.momostudios.coldsweat.common.temperature.modifier.WaterskinTempModifier;

import java.util.function.Supplier;

@Mod.EventBusSubscriber()
public class TempModifierInit
{
    public static IForgeRegistry<TempModifier> registry = null;
    @SubscribeEvent
    public static void registerTempModifiers(final RegistryEvent.NewRegistry event)
    {
        registry = new RegistryBuilder<TempModifier>()
                .setName(new ResourceLocation(ColdSweat.MOD_ID, "temp_modifiers_registry"))
                .setType(TempModifier.class)
                .create();

        registry.register(new BiomeTempModifier().setRegistryName(new ResourceLocation(ColdSweat.MOD_ID, "biome_modifier")));
        registry.register(new TimeTempModifier().setRegistryName(new ResourceLocation(ColdSweat.MOD_ID, "time_modifier")));
        registry.register(new WaterskinTempModifier().setRegistryName(new ResourceLocation(ColdSweat.MOD_ID, "waterskin_modifier")));
        System.out.println("BiomeTempModifier's registry name is " + registry.getKey(new BiomeTempModifier()));
    }
    /*
    public static final DeferredRegister<TempModifierT> TEMP_MODIFIER_TYPE = DeferredRegister.create(TempModifier.class, ColdSweat.MOD_ID);
    public static final Lazy<IForgeRegistry<TempModifier>> REGISTRY = Lazy.of(TEMP_MODIFIERS .makeRegistry("example_registry", RegistryBuilder::new));

    public static final RegistryObject<TempModifier> BIOME_TEMP_MODIFIER = TEMP_MODIFIERS.register("biome_modifier", BiomeTempModifier::new);
    public static final RegistryObject<TempModifier> TIME_TEMP_MODIFIER = TEMP_MODIFIERS.register("time_modifier", TimeTempModifier::new);
    public static final RegistryObject<TempModifier> WATERSKIN_TEMP_MODIFIER = TEMP_MODIFIERS.register("waterskin_modifier", WaterskinTempModifier::new);
    */
}
