package com.momosoftworks.coldsweat.client.renderer.model.armor;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class GoatParkaModel<T extends LivingEntity> extends HumanoidModel<T>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "goat_parka"), "main");

    ModelPart fluff;

    public GoatParkaModel(ModelPart root)
    {   super(root);
        this.fluff = root.getChild("body").getChild("fluff");
    }

    public static LayerDefinition createArmorLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0f), 1.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        float scale = 0.8f;

        PartDefinition body = partdefinition.getChild("body");
        PartDefinition right_arm = partdefinition.getChild("right_arm");
        PartDefinition left_arm = partdefinition.getChild("left_arm");

        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
                               .texOffs(0, 64).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(scale)),
                                                             PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition fluff = body.addOrReplaceChild("fluff", CubeListBuilder.create()
                              .texOffs(24, 64).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 6.0F, 10.0F, new CubeDeformation(scale)),
                                                             PartPose.offset(0.0F, 1F, 0.0F));

        PartDefinition right_sleeve = right_arm.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                              .texOffs(0, 80).addBox(-7.75F, -23.75F, -2.0F, 4.0F, 12.0F, 4.0F,
                                                             new CubeDeformation(scale, scale * 1.5f, scale)), PartPose.offset(4.5F, 22.0F, 0.0F));

        PartDefinition left_sleeve = left_arm.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                              .texOffs(0, 80).mirror().addBox(8.75F, -23.75F, -2.0F, 4.0F, 12.0F, 4.0F,
                                                             new CubeDeformation(scale, scale * 1.5f, scale)).mirror(false), PartPose.offset(-9.5F, 22.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }
}
