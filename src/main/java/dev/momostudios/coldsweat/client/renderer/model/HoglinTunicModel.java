package dev.momostudios.coldsweat.client.renderer.model;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class HoglinTunicModel<T extends LivingEntity> extends HumanoidModel<T>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ColdSweat.MOD_ID, "hoglin_tunic"), "main");

    public HoglinTunicModel(ModelPart root)
    {   super(root);
    }

    public static LayerDefinition createArmorLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0f), 1.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        float scale = 0.8f;

        PartDefinition body = partdefinition.getChild("body");
        PartDefinition right_arm = partdefinition.getChild("right_arm");
        PartDefinition left_arm = partdefinition.getChild("left_arm");

        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
                               .texOffs(0, 112).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(scale)),
                                                               PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition right_armor = right_arm.addOrReplaceChild("right_armor", CubeListBuilder.create(),
                                                                    PartPose.offset(-4.0F, 0.0F, 0.0F));

        PartDefinition right_sleeve = right_armor.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                                      .texOffs(24, 112).addBox(-7.75F, -23.75F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(scale * 0.5f, scale * 1.5f, scale))
                                      .texOffs(2, 90).addBox(-11.0F, -28F, 0.0F, 7.0F, 6.0F, 0.0F, new CubeDeformation(scale, scale, 0)),
                                                                  PartPose.offset(8.25F, 22.0F, 0.0F));

        PartDefinition left_armor = left_arm.addOrReplaceChild("left_armor", CubeListBuilder.create(),
                                                                   PartPose.offset(4.0F, 0.0F, 0.0F));

        PartDefinition left_sleeve = left_armor.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                                     .texOffs(24, 112).mirror().addBox(8.75F, -23.75F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(scale * 0.5f, scale * 1.5f, scale)).mirror(false)
                                     .texOffs(2, 90).mirror().addBox(9.0F, -28F, 0.0F, 7.0F, 6.0F, 0.0F, new CubeDeformation(scale, scale, 0)).mirror(false),
                                                                PartPose.offset(-13.25F, 22.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }
}
