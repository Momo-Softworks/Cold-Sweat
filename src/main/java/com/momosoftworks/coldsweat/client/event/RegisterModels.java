package com.momosoftworks.coldsweat.client.event;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.renderer.block.HearthBlockEntityRenderer;
import com.momosoftworks.coldsweat.client.renderer.block.IceboxBlockEntityRenderer;
import com.momosoftworks.coldsweat.client.renderer.entity.ChameleonEntityRenderer;
import com.momosoftworks.coldsweat.client.renderer.item.SoulSpringLampRenderer;
import com.momosoftworks.coldsweat.client.renderer.layer.ChameleonArmorLayer;
import com.momosoftworks.coldsweat.client.renderer.model.armor.*;
import com.momosoftworks.coldsweat.client.renderer.model.entity.ChameleonModel;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.EntityInit;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegisterModels
{
    public static HoglinHelmetModel<?> HOGLIN_HELMET_MODEL = null;
    public static HoglinChestplateModel<?> HOGLIN_CHESTPLATE_MODEL = null;
    public static HoglinLeggingsModel<?> HOGLIN_LEGGINGS_MODEL = null;
    public static HoglinBootsModel<?> HOGLIN_BOOTS_MODEL = null;

    public static GoatHelmetModel<?> GOAT_HELMET_MODEL = null;
    public static GoatChestplateModel<?> GOAT_CHESTPLATE_MODEL = null;
    public static GoatLeggingsModel<?> GOAT_LEGGINGS_MODEL = null;
    public static GoatBootsModel<?> GOAT_BOOTS_MODEL = null;

    public static ChameleonHelmetModel<?> CHAMELEON_HELMET_MODEL = null;
    public static ChameleonChestplateModel<?> CHAMELEON_CHESTPLATE_MODEL = null;
    public static ChameleonLeggingsModel<?> CHAMELEON_LEGGINGS_MODEL = null;
    public static ChameleonBootsModel<?> CHAMELEON_BOOTS_MODEL = null;

    public static EmptyArmorModel<?> EMPTY_ARMOR_MODEL = null;

    public static BlockEntityWithoutLevelRenderer SOULSPRING_LAMP_RENDERER = null;

    private static Map<ResourceLocation, BakedModel> BAKED_MODELS = new HashMap<>();

    public static void checkForInitModels()
    {
        if (HOGLIN_HELMET_MODEL != null) return;

        EntityModelSet mcModels = Minecraft.getInstance().getEntityModels();

        HOGLIN_HELMET_MODEL = new HoglinHelmetModel<>(mcModels.bakeLayer(HoglinHelmetModel.LAYER_LOCATION));
        HOGLIN_CHESTPLATE_MODEL = new HoglinChestplateModel<>(mcModels.bakeLayer(HoglinChestplateModel.LAYER_LOCATION));
        HOGLIN_BOOTS_MODEL = new HoglinBootsModel<>(mcModels.bakeLayer(HoglinBootsModel.LAYER_LOCATION));
        HOGLIN_LEGGINGS_MODEL = new HoglinLeggingsModel<>(mcModels.bakeLayer(HoglinLeggingsModel.LAYER_LOCATION));

        GOAT_HELMET_MODEL = new GoatHelmetModel<>(mcModels.bakeLayer(GoatHelmetModel.LAYER_LOCATION));
        GOAT_CHESTPLATE_MODEL = new GoatChestplateModel<>(mcModels.bakeLayer(GoatChestplateModel.LAYER_LOCATION));
        GOAT_LEGGINGS_MODEL = new GoatLeggingsModel<>(mcModels.bakeLayer(GoatLeggingsModel.LAYER_LOCATION));
        GOAT_BOOTS_MODEL = new GoatBootsModel<>(mcModels.bakeLayer(GoatBootsModel.LAYER_LOCATION));

        CHAMELEON_HELMET_MODEL = new ChameleonHelmetModel<>(mcModels.bakeLayer(ChameleonHelmetModel.LAYER_LOCATION));
        CHAMELEON_CHESTPLATE_MODEL = new ChameleonChestplateModel<>(mcModels.bakeLayer(ChameleonChestplateModel.LAYER_LOCATION));
        CHAMELEON_LEGGINGS_MODEL = new ChameleonLeggingsModel<>(mcModels.bakeLayer(ChameleonLeggingsModel.LAYER_LOCATION));
        CHAMELEON_BOOTS_MODEL = new ChameleonBootsModel<>(mcModels.bakeLayer(ChameleonBootsModel.LAYER_LOCATION));

        EMPTY_ARMOR_MODEL = new EmptyArmorModel<>(mcModels.bakeLayer(EmptyArmorModel.LAYER_LOCATION));

        SOULSPRING_LAMP_RENDERER = new SoulSpringLampRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), mcModels);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(ChameleonModel.LAYER_LOCATION, ChameleonModel::createBodyLayer);

        event.registerLayerDefinition(HoglinHelmetModel.LAYER_LOCATION, HoglinHelmetModel::createArmorLayer);
        event.registerLayerDefinition(HoglinChestplateModel.LAYER_LOCATION, HoglinChestplateModel::createArmorLayer);
        event.registerLayerDefinition(HoglinBootsModel.LAYER_LOCATION, HoglinBootsModel::createArmorLayer);
        event.registerLayerDefinition(HoglinLeggingsModel.LAYER_LOCATION, HoglinLeggingsModel::createArmorLayer);

        event.registerLayerDefinition(GoatHelmetModel.LAYER_LOCATION, GoatHelmetModel::createArmorLayer);
        event.registerLayerDefinition(GoatChestplateModel.LAYER_LOCATION, GoatChestplateModel::createArmorLayer);
        event.registerLayerDefinition(GoatLeggingsModel.LAYER_LOCATION, GoatLeggingsModel::createArmorLayer);
        event.registerLayerDefinition(GoatBootsModel.LAYER_LOCATION, GoatBootsModel::createArmorLayer);

        event.registerLayerDefinition(ChameleonHelmetModel.LAYER_LOCATION, ChameleonHelmetModel::createArmorLayer);
        event.registerLayerDefinition(ChameleonChestplateModel.LAYER_LOCATION, ChameleonChestplateModel::createArmorLayer);
        event.registerLayerDefinition(ChameleonLeggingsModel.LAYER_LOCATION, ChameleonLeggingsModel::createArmorLayer);
        event.registerLayerDefinition(ChameleonBootsModel.LAYER_LOCATION, ChameleonBootsModel::createArmorLayer);

        event.registerLayerDefinition(EmptyArmorModel.LAYER_LOCATION, EmptyArmorModel::createArmorLayer);

        event.registerLayerDefinition(IceboxBlockEntityRenderer.LAYER_LOCATION, IceboxBlockEntityRenderer::createBodyLayer);
        event.registerLayerDefinition(HearthBlockEntityRenderer.LAYER_LOCATION, HearthBlockEntityRenderer::createBodyLayer);
        event.registerLayerDefinition(SoulSpringLampRenderer.LAYER_LOCATION, SoulSpringLampRenderer::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(EntityInit.CHAMELEON.get(), ChameleonEntityRenderer::new);
    }

    @SubscribeEvent
    public static void overrideModels(ModelEvent.BakingCompleted event)
    {
        BAKED_MODELS = event.getModels();
        forceCustomItemModel(ModItems.SOULSPRING_LAMP, ConfigSettings.ANIMATED_SOULSPRING_LAMP_MODEL);
    }

    public static Map.Entry<ResourceLocation, BakedModel> getBakedModel(Item item)
    {
        ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(item);
        return BAKED_MODELS.entrySet().stream()
                .filter(entry -> entry.getKey().toString().contains(itemID.toString()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Forces the item's model to return {@code true} for {@link BakedModel#isCustomRenderer()}.
     * Used for custom item BEWLRs.
     */
    public static void forceCustomItemModel(Item item, Supplier<Boolean> custom)
    {
        Optional<Map.Entry<ResourceLocation, BakedModel>> modelOpt = Optional.ofNullable(getBakedModel(item));

        if (modelOpt.isPresent() && modelOpt.get().getValue() instanceof SimpleBakedModel model)
        {
            RandomSource random = RandomSource.create();
            List<BakedQuad> unculledFaces = model.getQuads(null, null, random);
            Map<Direction, List<BakedQuad>> culledFaces = Arrays.stream(Direction.values())
                    .map(dir -> Pair.of(dir, model.getQuads(null, dir, random)))
                    .collect(() -> new EnumMap<>(Direction.class), (map, pair) -> map.put(pair.getFirst(), pair.getSecond()), EnumMap::putAll);

            SimpleBakedModel customModel = new SimpleBakedModel(unculledFaces, culledFaces, model.useAmbientOcclusion(),
                                                                model.usesBlockLight(), model.isGui3d(), model.getParticleIcon(),
                                                                model.getTransforms(), model.getOverrides(), new RenderTypeGroup(null, null, null))
            {
                @Override
                public boolean isCustomRenderer()
                {   return custom.get();
                }
            };
            BAKED_MODELS.put(modelOpt.get().getKey(), customModel);
        }
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event)
    {
        if (event.getSkin("default") instanceof PlayerRenderer playerRenderer)
        {   playerRenderer.addLayer(new ChameleonArmorLayer<>(playerRenderer));
        }
        if (event.getSkin("slim") instanceof PlayerRenderer playerRenderer)
        {   playerRenderer.addLayer(new ChameleonArmorLayer<>(playerRenderer));
        }
    }
}
