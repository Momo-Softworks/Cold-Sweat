package com.momosoftworks.coldsweat.client.event;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.client.renderer.layer.ChameleonArmorLayer;
import com.momosoftworks.coldsweat.client.renderer.model.armor.*;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegisterModels
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

    private static Map<ResourceLocation, IBakedModel> BAKED_MODELS = new HashMap<>();

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

    @SubscribeEvent
    public static void overrideModels(ModelBakeEvent event)
    {
        BAKED_MODELS = event.getModelRegistry();
        forceCustomItemModel(ModItems.SOULSPRING_LAMP, ConfigSettings.ANIMATED_SOULSPRING_LAMP_MODEL);
    }

    public static Map.Entry<ResourceLocation, IBakedModel> getBakedModel(Item item)
    {
        ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(item);
        return BAKED_MODELS.entrySet().stream()
                .filter(entry -> entry.getKey().toString().contains(itemID.toString()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Forces the item's model to return {@code true} for {@link IBakedModel#isCustomRenderer()}.
     * Used for custom item ISTERs.
     */
    public static void forceCustomItemModel(Item item, Supplier<Boolean> custom)
    {
        Optional<Map.Entry<ResourceLocation, IBakedModel>> modelOpt = Optional.ofNullable(getBakedModel(item));

        if (modelOpt.isPresent() && modelOpt.get().getValue() instanceof SimpleBakedModel)
        {
            SimpleBakedModel model = ((SimpleBakedModel) modelOpt.get().getValue());
            Random random = new Random();
            List<BakedQuad> unculledFaces = model.getQuads(null, null, random);
            Map<Direction, List<BakedQuad>> culledFaces = Arrays.stream(Direction.values())
                    .map(dir -> Pair.of(dir, model.getQuads(null, dir, random)))
                    .collect(() -> new EnumMap<>(Direction.class), (map, pair) -> map.put(pair.getFirst(), pair.getSecond()), EnumMap::putAll);

            SimpleBakedModel customModel = new SimpleBakedModel(unculledFaces, culledFaces, model.useAmbientOcclusion(),
                                                                model.usesBlockLight(), model.isGui3d(), model.getParticleIcon(),
                                                                model.getTransforms(), model.getOverrides())
            {
                @Override
                public boolean isCustomRenderer()
                {   return custom.get();
                }
            };
            BAKED_MODELS.put(modelOpt.get().getKey(), customModel);
        }
    }
}
