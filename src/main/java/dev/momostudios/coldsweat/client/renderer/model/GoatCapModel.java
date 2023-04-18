package dev.momostudios.coldsweat.client.renderer.model;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class GoatCapModel<T extends LivingEntity> extends HumanoidModel<T>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ColdSweat.MOD_ID, "goat_cap"), "main");


    public GoatCapModel(ModelPart root)
    {   super(root);
    }

    public static LayerDefinition createArmorLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0f), 1.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        float scale = 0.8f;

        PartDefinition head = partdefinition.getChild("head");

        PartDefinition cap = head.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(16, 80).addBox(-4.0F, -6.5F, -7.0F, 8.0F, 8.0F, 8.0F,
                                                                                                           new CubeDeformation(scale)), PartPose.offset(0.0F, -1.5F, 3.0F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }
}
