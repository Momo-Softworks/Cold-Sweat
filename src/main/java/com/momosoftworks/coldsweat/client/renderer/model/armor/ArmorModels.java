package com.momosoftworks.coldsweat.client.renderer.model.armor;

import com.momosoftworks.coldsweat.client.renderer.layer.ChameleonArmorLayer;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;

public class ArmorModels
{
    public static HoglinHeadpieceModel<?> HOGLIN_HEADPIECE_MODEL = new HoglinHeadpieceModel<>();
    public static HoglinTunicModel<?> HOGLIN_TUNIC_MODEL = new HoglinTunicModel<>();
    public static HoglinHoovesModel<?> HOGLIN_HOOVES_MODEL = new HoglinHoovesModel<>();
    public static HoglinTrousersModel<?> HOGLIN_TROUSERS_MODEL = new HoglinTrousersModel<>();

    public static LlamaCapModel<?> LLAMA_CAP_MODEL = new LlamaCapModel<>();
    public static LlamaParkaModel<?> LLAMA_PARKA_MODEL = new LlamaParkaModel<>();
    public static LlamaPantsModel<?> LLAMA_PANTS_MODEL = new LlamaPantsModel<>();
    public static LlamaBootsModel<?> LLAMA_BOOTS_MODEL = new LlamaBootsModel<>();

    public static ChameleonHelmetModel<?> CHAMELEON_HELMET_MODEL = new ChameleonHelmetModel<>();
    public static ChameleonChestplateModel<?> CHAMELEON_CHESTPLATE_MODEL = new ChameleonChestplateModel<>();
    public static ChameleonLeggingsModel<?> CHAMELEON_LEGGINGS_MODEL = new ChameleonLeggingsModel<>();
    public static ChameleonBootsModel<?> CHAMELEON_BOOTS_MODEL = new ChameleonBootsModel<>();

    public static EmptyArmorModel<?> EMPTY_ARMOR_MODEL = new EmptyArmorModel<>();

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents
    {
        @SubscribeEvent
        public static void addLayers (FMLClientSetupEvent event)
        {
            Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
            CSMath.doIfNotNull(skinMap.get("default"), playerRenderer ->
            {
                playerRenderer.addLayer(new ChameleonArmorLayer<>(playerRenderer));
            });
            CSMath.doIfNotNull(skinMap.get("slim"), playerRenderer ->
            {
                playerRenderer.addLayer(new ChameleonArmorLayer<>(playerRenderer));
            });
        }
    }
}
