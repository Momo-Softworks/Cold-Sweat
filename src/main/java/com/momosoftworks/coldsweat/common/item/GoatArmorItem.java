package com.momosoftworks.coldsweat.common.item;

import com.momosoftworks.coldsweat.client.event.RegisterModels;
import com.momosoftworks.coldsweat.client.renderer.model.armor.GoatParkaModel;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class GoatArmorItem extends ArmorItem
{
    public GoatArmorItem(ArmorMaterial material, EquipmentSlot slot, Properties properties)
    {   super(material, slot, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions()
        {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> playerModel)
            {
                RegisterModels.checkForInitModels();
                return switch (armorSlot)
                {
                    case HEAD -> RegisterModels.GOAT_CAP_MODEL;
                    case CHEST ->
                    {
                        GoatParkaModel<?> model = RegisterModels.GOAT_PARKA_MODEL;
                        ModelPart fluff = model.body.getChild("fluff");
                        float headPitch = entityLiving.getViewXRot(Minecraft.getInstance().getFrameTime());
                        float headYaw = CSMath.blend(entityLiving.yRotO, entityLiving.getYRot(), Minecraft.getInstance().getFrameTime(), 0, 1);
                        float bodyYaw = entityLiving.yBodyRot;
                        float netHeadYaw = CSMath.clamp(headYaw - bodyYaw, -30, 30);

                        fluff.xRot = CSMath.toRadians(CSMath.clamp(headPitch, 0, 60f)) / 2;
                        fluff.zRot = -CSMath.toRadians(netHeadYaw) * fluff.xRot / 2;
                        fluff.x = fluff.zRot * 2;
                        yield model;
                    }
                    case LEGS -> RegisterModels.GOAT_PANTS_MODEL;
                    case FEET -> RegisterModels.GOAT_BOOTS_MODEL;
                    default -> null;
                };
            }
        });
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer)
    {
        return stack.is(ModItems.FUR_BOOTS);
    }
}
