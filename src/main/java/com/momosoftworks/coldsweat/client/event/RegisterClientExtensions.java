package com.momosoftworks.coldsweat.client.event;

import java.util.function.Function;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.HumanoidModel;
import com.momosoftworks.coldsweat.util.item.ItemStackHelper;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.core.init.ModFluids;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;

/**
 * Client-side rendering hooks that used to live in {@code Item/FluidType#initializeClient}
 */
@EventBusSubscriber(modid = ColdSweat.MOD_ID, value = Dist.CLIENT)
public class RegisterClientExtensions
{
    private static final FluidTintSource SLUSH_TINT = new FluidTintSource()
    {
        @Override
        public int color(FluidState state)
        {   return ARGB.color(240, 210, 240, 255);
        }

        @Override
        public int colorInWorld(FluidState fluidState, BlockState blockState, BlockAndTintGetter level, BlockPos pos)
        {
            int color = BiomeColors.getAverageWaterColor(level, pos);
            int alphaColor = ARGB.color(240, ARGB.red(color), ARGB.green(color), ARGB.blue(color));
            int white = ARGB.color(240, 240, 255, 255);
            return CSMath.blendColors(alphaColor, white, 0.5f);
        }
    };

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event)
    {
        event.register(new FluidModel.Unbaked(new Material(ColdSweat.createKey("block/slush_still"), true),
                                              new Material(ColdSweat.createKey("block/slush_flow"), true),
                                              null, SLUSH_TINT),
                       ModFluids.SLUSH, ModFluids.FLOWING_SLUSH);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event)
    {
        event.registerItem(armorModel(RegisterModels::getHoglinArmorModel),
                           ModItems.HOGLIN_HELMET.get(), ModItems.HOGLIN_CHESTPLATE.get(), ModItems.HOGLIN_LEGGINGS.get(), ModItems.HOGLIN_BOOTS.get());
        event.registerItem(armorModel(RegisterModels::getGoatArmorModel),
                           ModItems.GOAT_FUR_HELMET.get(), ModItems.GOAT_FUR_CHESTPLATE.get(), ModItems.GOAT_FUR_LEGGINGS.get(), ModItems.GOAT_FUR_BOOTS.get());
    }

    private static IClientItemExtensions armorModel(Function<EquipmentSlot, HumanoidModel<HumanoidRenderState>> modelGetter)
    {
        return new IClientItemExtensions()
        {
            @Override
            public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original)
            {
                Model model = modelGetter.apply(ItemStackHelper.getEquipmentSlot(stack));
                return model != null ? model : original;
            }
        };
    }
}
