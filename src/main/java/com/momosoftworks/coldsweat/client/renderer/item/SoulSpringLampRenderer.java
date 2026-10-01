package com.momosoftworks.coldsweat.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.vanilla.RenderLevelEvent;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector3fc;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Animated soulspring lamp model. Used by the {@code cold_sweat:soulspring_lamp} special item model
 * when the animated lamp model is enabled in the config.
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class SoulSpringLampRenderer implements SpecialModelRenderer<SoulSpringLampRenderer.LampState>
{
    public static final Identifier TEXTURE_FRAME = ColdSweat.createKey("textures/item/soulspring_lamp/render/soulspring_lamp_frame.png");
    public static final Identifier TEXTURE_0 = ColdSweat.createKey("textures/item/soulspring_lamp/render/soulspring_lamp_0.png");
    public static final Identifier TEXTURE_1 = ColdSweat.createKey("textures/item/soulspring_lamp/render/soulspring_lamp_1.png");
    public static final Identifier TEXTURE_2 = ColdSweat.createKey("textures/item/soulspring_lamp/render/soulspring_lamp_2.png");
    public static final Identifier TEXTURE_3 = ColdSweat.createKey("textures/item/soulspring_lamp/render/soulspring_lamp_3.png");

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ColdSweat.createKey("soulspring_lamp"), "main");

    private static float TIME = 0;

    private final ModelPart base;
    // Separate copies, since parts are posed when submitted but drawn later
    private final ModelPart animatedHeart;
    private final ModelPart staticHeart;

    public record LampState(double fuel, boolean lit) {}

    public SoulSpringLampRenderer(ModelPart root, ModelPart staticRoot)
    {
        this.base = root.getChild("base");
        this.animatedHeart = root.getChild("heart");
        this.staticHeart = staticRoot.getChild("heart");
        this.staticHeart.y = -14.0F;
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 11).addBox(-12.0F, -17.0F, 4.0F, 8.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(22, 21).addBox(-13.0F, -6.0F, 3.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(58, 21).addBox(-9.5F, -5.0F, 8.0F, 3.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(52, 18).addBox(-8.0F, -5.0F, 6.5F, 0.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(24, 0).addBox(-13.0F, -19.0F, 3.0F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(40, 13).addBox(-11.0F, -21.0F, 5.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, -7).addBox(-8.0F, -28.0F, 4.5F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        partdefinition.addOrReplaceChild("heart", CubeListBuilder.create().texOffs(14, 0).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public @Nullable LampState extractArgument(ItemStack stack)
    {   return new LampState(SoulspringLampItem.getFuel(stack), SoulspringLampItem.isLit(stack));
    }

    @Override
    public void submit(@Nullable LampState lamp, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor)
    {
        if (lamp == null) return;

        boolean isFirstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
        Identifier texture = getTexture(lamp);

        ModelPart heart;
        if (lamp.fuel() > 0)
        {
            heart = this.animatedHeart;
            heart.y = -14.0F + (float) Math.sin(TIME / 8) * 1.2f;
            heart.xRot = CSMath.toRadians((TIME * 2) % 360);
            heart.yRot = CSMath.toRadians((TIME * 2 + 10) % 360);
            heart.zRot = CSMath.toRadians((TIME * 0.5f + 5) % 360);
        }
        else heart = this.staticHeart;

        float emission = lamp.lit() ? (float) CSMath.blend(0, 1, lamp.fuel(), 0, 64) : 0;

        poseStack.pushPose();

        // Translate lamp correctly
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));

        // Render frame
        collector.submitModelPart(this.base, poseStack, RenderTypes.entityTranslucent(TEXTURE_FRAME), light, overlay, null);

        // Render heart
        poseStack.pushPose();
        double heartYOffset = isFirstPerson ? 1.65 : 1.55;
        float heartScale = isFirstPerson ? 0.925f : 0.9f;
        poseStack.translate(0, heartYOffset, 0);
        poseStack.scale(heartScale, heartScale, heartScale);
        collector.submitModelPart(heart, poseStack, RenderTypes.entityTranslucent(texture), light, overlay, null, ARGB.colorFromFloat(1 - emission, 1, 1, 1), null);
        // Emission
        collector.submitModelPart(heart, poseStack, RenderTypes.entityTranslucentEmissive(texture), light, overlay, null, ARGB.colorFromFloat(emission, 1, 1, 1), null);
        poseStack.popPose();

        // Render glass
        collector.submitModelPart(this.base, poseStack, RenderTypes.entityTranslucent(texture), light, overlay, null, ARGB.colorFromFloat(1 - emission, 1, 1, 1), null);
        // Emission
        collector.submitModelPart(this.base, poseStack, RenderTypes.entityTranslucentEmissive(texture), light, overlay, null, ARGB.colorFromFloat(emission, 1, 1, 1), null);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        this.base.getExtentsForGui(poseStack, output);
    }

    private static Identifier getTexture(LampState lamp)
    {
        double fuel = lamp.fuel();
        int state;
        if (lamp.lit())
        {
            state = fuel > 43 ? 3 :
                    fuel > 22 ? 2 : 1;
        }
        else state = 0;

        return switch (state)
        {
            case 1 -> TEXTURE_1;
            case 2 -> TEXTURE_2;
            case 3 -> TEXTURE_3;
            default -> TEXTURE_0;
        };
    }

    @SubscribeEvent
    public static void tickTimer(RenderLevelEvent.Post event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isPaused())
        {   TIME += mc.getDeltaTracker().getRealtimeDeltaTicks();
        }
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<LampState>
    {
        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public SpecialModelRenderer<LampState> bake(BakingContext context)
        {   return new SoulSpringLampRenderer(context.entityModelSet().bakeLayer(LAYER_LOCATION), context.entityModelSet().bakeLayer(LAYER_LOCATION));
        }

        @Override
        public MapCodec<Unbaked> type()
        {   return MAP_CODEC;
        }
    }
}
