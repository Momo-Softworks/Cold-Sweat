package dev.momostudios.coldsweat.client.renderer.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.client.renderer.ChameleonAnimations;
import dev.momostudios.coldsweat.client.renderer.animation.AnimationManager;
import dev.momostudios.coldsweat.common.entity.Chameleon;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ChameleonModel<T extends Chameleon> extends EntityModel<T>
{
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ColdSweat.MOD_ID, "chameleon"), "main");

	final Map<String, ModelPart> modelParts;
	final ModelPart body;
	Chameleon chameleon;
	boolean tongueVisible = false;

	public ChameleonModel(ModelPart root)
	{
		super(RenderType::entityTranslucent);
		this.body = root.getChild("Body");
		modelParts = AnimationManager.getChildrenMap(root);

		AnimationManager.storeDefaultPoses(ModEntities.CHAMELEON, modelParts);
	}

	public static LayerDefinition createBodyLayer()
	{
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(1, 16).addBox(-2.0F, -8.0F, -3.0F, 4.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition Head = Body.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -7.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, -3.0F));

		PartDefinition TopFrill_r1 = Head.addOrReplaceChild("TopFrill_r1", CubeListBuilder.create().texOffs(30, 0).addBox(-1.0F, 1.4F, 0.05F, 2.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -5.0F, 0.7418F, 0.0F, 0.0F));

		PartDefinition Jaw = Head.addOrReplaceChild("Jaw", CubeListBuilder.create().texOffs(15, 7).addBox(-2.0F, 0.0F, -7.0F, 4.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
																		  .texOffs(18, 15).addBox(-1.0F, 1.0F, -6.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition RightEye = Head.addOrReplaceChild("RightEye", CubeListBuilder.create().texOffs(15, 1).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 0.0F, -3.5F));

		PartDefinition LeftEye = Head.addOrReplaceChild("LeftEye", CubeListBuilder.create().texOffs(15, 1).mirror().addBox(-1.0005F, -1.4782F, -1.634F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(1.0005F, -0.0218F, -3.366F));

		PartDefinition Tongue1 = Head.addOrReplaceChild("Tongue1", CubeListBuilder.create().texOffs(27, 25).addBox(-1.0F, 0.0F, -7.0F, 2.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition Tongue2 = Tongue1.addOrReplaceChild("Tongue2", CubeListBuilder.create().texOffs(27, 25).addBox(-1.0F, -3.0F, -10.0F, 2.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 3.0F));

		PartDefinition Tongue3 = Tongue2.addOrReplaceChild("Tongue3", CubeListBuilder.create().texOffs(27, 25).addBox(-1.0F, -3.0F, -10.0F, 2.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition RightFrontLeg = Body.addOrReplaceChild("RightFrontLeg", CubeListBuilder.create().texOffs(38, 25).mirror().addBox(-4.0F, -1.0F, -1.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, -3.0F, -1.0F));

		PartDefinition RightFrontLeg2 = RightFrontLeg.addOrReplaceChild("RightFrontLeg2", CubeListBuilder.create().texOffs(38, 29).mirror().addBox(-4.0F, -1.0F, -1.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.0F, 0.0F, 0.0F));

		PartDefinition RightFrontFoot1 = RightFrontLeg2.addOrReplaceChild("RightFrontFoot1", CubeListBuilder.create().texOffs(28, 27).mirror().addBox(0.0F, -3.0F, -1.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.0F, 0.0F, 0.0F));

		PartDefinition RightFrontFoot2 = RightFrontLeg2.addOrReplaceChild("RightFrontFoot2", CubeListBuilder.create().texOffs(28, 24).mirror().addBox(0.0F, 0.0F, -1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.0F, 0.0F, 0.0F));

		PartDefinition LeftFrontLeg = Body.addOrReplaceChild("LeftFrontLeg", CubeListBuilder.create().texOffs(38, 25).addBox(0.0F, -1.0F, -1.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -3.0F, -1.0F));

		PartDefinition LeftFrontLeg2 = LeftFrontLeg.addOrReplaceChild("LeftFrontLeg2", CubeListBuilder.create().texOffs(38, 29).addBox(0.0F, -1.0F, -1.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, 0.0F));

		PartDefinition LeftFrontFoot1 = LeftFrontLeg2.addOrReplaceChild("LeftFrontFoot1", CubeListBuilder.create().texOffs(28, 27).addBox(0.0F, -3.0F, -1.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, 0.0F));

		PartDefinition LeftFrontFoot2 = LeftFrontLeg2.addOrReplaceChild("LeftFrontFoot2", CubeListBuilder.create().texOffs(28, 24).addBox(0.0F, 0.0F, -1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, 0.0F));

		PartDefinition RightBackLeg = Body.addOrReplaceChild("RightBackLeg", CubeListBuilder.create().texOffs(38, 25).mirror().addBox(-4.0F, -1.0F, -1.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, -3.0F, 4.0F));

		PartDefinition RightBackLeg2 = RightBackLeg.addOrReplaceChild("RightBackLeg2", CubeListBuilder.create().texOffs(38, 29).mirror().addBox(-4.0F, -1.0F, -1.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.0F, 0.0F, 0.0F));

		PartDefinition RightBackFoot1 = RightBackLeg2.addOrReplaceChild("RightBackFoot1", CubeListBuilder.create().texOffs(28, 27).mirror().addBox(0.0F, -3.0F, -1.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.0F, 0.0F, 0.0F));

		PartDefinition RightBackFoot2 = RightBackLeg2.addOrReplaceChild("RightBackFoot2", CubeListBuilder.create().texOffs(28, 24).mirror().addBox(0.0F, 0.0F, -1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.0F, 0.0F, 0.0F));

		PartDefinition LeftBackLeg = Body.addOrReplaceChild("LeftBackLeg", CubeListBuilder.create().texOffs(38, 25).addBox(0.0F, -1.0F, -1.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -3.0F, 4.0F));

		PartDefinition LeftBackLeg2 = LeftBackLeg.addOrReplaceChild("LeftBackLeg2", CubeListBuilder.create().texOffs(38, 29).addBox(0.0F, -1.0F, -1.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, 0.0F));

		PartDefinition LeftBackFoot1 = LeftBackLeg2.addOrReplaceChild("LeftBackFoot1", CubeListBuilder.create().texOffs(28, 27).addBox(0.0F, -3.0F, -1.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, 0.0F));

		PartDefinition LeftBackFoot2 = LeftBackLeg2.addOrReplaceChild("LeftBackFoot2", CubeListBuilder.create().texOffs(27, 24).addBox(0.0F, 0.0F, -1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, 0.0F));

		PartDefinition Tail = Body.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(34, 18).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, 6.0F));

		PartDefinition Tail2 = Tail.addOrReplaceChild("Tail2", CubeListBuilder.create().texOffs(38, 13).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 4.0F));

		PartDefinition Tail3 = Tail2.addOrReplaceChild("Tail3", CubeListBuilder.create().texOffs(0, 17).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.0F));

		return LayerDefinition.create(meshdefinition, 48, 32);
	}

	@Override
	public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if (!Minecraft.getInstance().isPaused())
		{
			AnimationManager.loadAnimationStates(entity, modelParts);

			float tickDelta = Minecraft.getInstance().getDeltaFrameTime();
			ModelPart head = modelParts.get("Head");
			ModelPart rightEye = modelParts.get("RightEye");
			ModelPart leftEye = modelParts.get("LeftEye");

			// get the pitch (up/down) of the head in Radians
			float desiredXHead = CSMath.toRadians(headPitch);
			boolean hasLookTarget = entity.isWalking() || entity.getEatTimer() < entity.getEatAnimLength();
			// Blend smoothly to the desired pitch using frameTime
			// There is no slop in head rotation if the Chameleon is moving
			entity.xRotHead += ((hasLookTarget ? desiredXHead : CSMath.clamp(entity.xRotHead, desiredXHead - 0.4f, desiredXHead + 0.4f)) - entity.xRotHead) * tickDelta;

			// Get the yaw (left/right) of the head in Radians
			float desiredYHead = CSMath.toRadians(netHeadYaw);
			// Blend smoothly to the desired yaw using frameTime
			// There is no slop in head rotation if the Chameleon is moving
			entity.yRotHead += ((hasLookTarget ? desiredYHead : CSMath.clamp(entity.yRotHead, desiredYHead - 0.6f, desiredYHead + 0.6f)) - entity.yRotHead) * tickDelta;

			// Move the right eye if the Chameleon is looking at an object to its right
			if (head.yRot < desiredYHead)
			{
				// If there is slop in the head rotation, make up the difference with the eye
				entity.yRotRightEye += (-CSMath.clamp((desiredYHead - head.yRot), -0.5f, 0.5f) - entity.yRotRightEye) * tickDelta;
				entity.xRotRightEye += (-CSMath.clamp((desiredXHead - head.xRot), -0.5f, 0.5f) - entity.xRotRightEye) * tickDelta;
			}
			else
			{
				// Reset the eye rotation
				entity.yRotRightEye += (0 - entity.yRotRightEye) * tickDelta;
				entity.xRotRightEye += (0 - entity.xRotRightEye) * tickDelta;
			}
			// Move the left eye if the Chameleon is looking at an object to its left
			if (head.yRot > desiredYHead)
			{
				// If there is slop in the head rotation, make up the difference with the eye
				entity.yRotLeftEye += (CSMath.clamp((head.yRot - desiredYHead), -0.5f, 0.5f) - entity.yRotLeftEye) * tickDelta;
				entity.xRotLeftEye += (CSMath.clamp((desiredXHead - head.xRot), -0.5f, 0.5f) - entity.xRotLeftEye) * tickDelta;
			}
			else
			{
				// Reset the eye rotation
				entity.yRotLeftEye += (-entity.yRotLeftEye) * tickDelta;
				entity.xRotLeftEye += (-entity.xRotLeftEye) * tickDelta;
			}

			// Set the model part rotations to the values stored in the Chameleon entity
			head.xRot = entity.xRotHead;
			head.yRot = entity.yRotHead;
			rightEye.yRot = entity.yRotRightEye;
			rightEye.zRot = entity.xRotRightEye;
			leftEye.yRot = entity.yRotLeftEye;
			leftEye.zRot = entity.xRotLeftEye;

			AnimationManager.saveAnimationStates(entity, modelParts);

			AnimationManager.animateEntity(entity, (animTime, frameTime) ->
			{
				Map manualParts = new HashMap(modelParts);
				manualParts.remove("Head");
				manualParts.remove("LeftEye");
				manualParts.remove("RightEye");

				// Riding player animation
				if (this.riding && entity.getVehicle() instanceof Player player)
				{
					float partialTick = Minecraft.getInstance().getFrameTime();
					float playerYaw = CSMath.blend(player.yHeadRotO, player.yHeadRot, partialTick, 0, 1);

					ChameleonAnimations.RIDE.animateAll(manualParts, animTime, false);

					body.y -= (player.getBbHeight() / 2) * 16 - 4;
					body.yRot = CSMath.toRadians(playerYaw) - CSMath.toRadians(CSMath.blend(player.yBodyRotO, player.yBodyRot, partialTick, 0, 1));
					head.yRot = CSMath.toRadians(entity.getViewYRot(partialTick) - player.getViewYRot(partialTick));
					head.xRot = CSMath.toRadians(entity.getViewXRot(partialTick) - player.getViewXRot(partialTick));
				}
				// Walk animation
				else if (entity.isWalking())
				{
					float walkSpeed = Math.min(0.15f, new Vec2((float) entity.getDeltaMovement().x, (float) entity.getDeltaMovement().z).length());
					animTime += (frameTime * walkSpeed * 45);
					ChameleonAnimations.WALK.animateAll(manualParts, animTime, false);
					ChameleonAnimations.WALK.animate("Head", modelParts.get("Head"), animTime, true);
				}
				// Idle animation
				else
				{
					animTime += frameTime;
					ChameleonAnimations.IDLE.animateAll(manualParts, animTime, false);
				}

				// Eat animation (applied on top of other anims)
				if (entity.getEatTimer() < entity.getEatAnimLength() - 1)
				{
					tongueVisible = true;
					ChameleonAnimations.EAT.animateAll(modelParts, (float) CSMath.blend(0, 0.5, entity.getEatTimer() + Minecraft.getInstance().getFrameTime(), 0, entity.getEatAnimLength()), true);
				}
				else
				{
					tongueVisible = false;
				}

				return animTime;
			});

			// Move the body down to the ground
			body.y += 23.5;
		}
		chameleon = entity;
	}

	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha, false);
	}

	public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, boolean isOverlay)
	{
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		float partialTick = Minecraft.getInstance().getFrameTime();
		long tickCount = chameleon.tickCount;
		long hurtTime = chameleon.getHurtTimestamp();

		// Make the chameleon invisible after it gets hurt
		// Don't do this if alpha is overridden
		if (!isOverlay)
		{
			if (CSMath.isInRange(tickCount - hurtTime, 10, 120) && hurtTime != 0)
			{
				chameleon.opacity += (alpha * 0.15f - chameleon.opacity) * Minecraft.getInstance().getDeltaFrameTime() / 10;
			}
			else if (chameleon.opacity < alpha)
			{
				chameleon.opacity = CSMath.blend(alpha * 0.15f, alpha, tickCount - hurtTime + partialTick, 120, 180);
			}
		}

		ModelPart tongue1 = modelParts.get("Tongue1");

		tongue1.visible = false;

		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, isOverlay ? alpha : chameleon.opacity);

		// Render the tongue with a different VertexConsumer that culls backfaces
		if (tongueVisible && !isOverlay)
		{
			poseStack.pushPose();
			ModelPart head = modelParts.get("Head");
			ModelPart jaw = modelParts.get("Jaw");

			// vertex consumer for entityTranslucentCull
			VertexConsumer tongueConsumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.entityCutout(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(chameleon).getTextureLocation(chameleon)));
			tongue1.visible = true;
			poseStack.translate(0, 1.1555, -0.18755);
			tongue1.xRot = head.xRot + jaw.xRot / 2;
			tongue1.yRot = head.yRot;
			tongue1.render(poseStack, tongueConsumer, packedLight, packedOverlay, red, green, blue, chameleon.opacity);
			poseStack.popPose();
		}
		RenderSystem.disableBlend();
	}
}