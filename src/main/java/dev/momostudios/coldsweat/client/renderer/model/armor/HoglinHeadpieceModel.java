package dev.momostudios.coldsweat.client.renderer.model.armor;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class HoglinHeadpieceModel<T extends LivingEntity> extends HumanoidModel<T>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ColdSweat.MOD_ID, "hoglin_headpiece"), "main");

    public HoglinHeadpieceModel(ModelPart root)
    {   super(root);
    }

    public static LayerDefinition createArmorLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0f), 1.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.getChild("head");

        PartDefinition mainHead = head.addOrReplaceChild("main_head", CubeListBuilder.create()
                .texOffs(0, 64).addBox(-7.0F, -14.075F, -5.675F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 64).mirror().addBox(5.0F, -14.075F, -5.675F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 78).addBox(0.0F, -16.075F, -1.65F, 0.0F, 17.0F, 12.0F, new CubeDeformation(0.0F)),
                                                         PartPose.offsetAndRotation(0.0F, -1.5F, 3.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition left_ear = mainHead.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(0, 107).addBox(0.0F, -0.5F, -1.5F, 5.0F, 1.0F, 3.0F,
                                        new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -10.575F, 2.85F, 0.0F, 0.0F, 0.6981F));

        PartDefinition right_ear = mainHead.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(0, 107).mirror().addBox(-5.0F, -0.5F, -1.5F, 5.0F, 1.0F, 3.0F,
                                        new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.0F, -10.575F, 2.85F, 0.0F, 0.0F, -0.6981F));

        PartDefinition headpiece = mainHead.addOrReplaceChild("headpiece", CubeListBuilder.create()
                .texOffs(0, 64).addBox(-5.0F, -6.5F, -1.5F, 10.0F, 13.0F, 13.0F,
                                        new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.5717F, -2.1651F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }
}
