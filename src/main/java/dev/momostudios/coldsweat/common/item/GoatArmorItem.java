package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.client.event.RegisterModels;
import dev.momostudios.coldsweat.client.renderer.model.GoatParkaModel;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;

public class GoatArmorItem extends ArmorItem
{
    public GoatArmorItem(ArmorMaterial material, EquipmentSlot slot, Properties properties)
    {   super(material, slot, properties);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer)
    {
        consumer.accept(new IItemRenderProperties()
        {
            @Override
            public HumanoidModel<?> getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> playerModel)
            {
                RegisterModels.checkForInitModels();
                return switch (armorSlot)
                {
                    case HEAD -> RegisterModels.GOAT_CAP_MODEL;
                    case CHEST ->
                    {
                        GoatParkaModel<?> model = RegisterModels.GOAT_PARKA_MODEL;
                        ModelPart fluff = model.body.getChild("fluff");
                        float headPitch = entityLiving.getXRot();

                        if (!CSMath.withinRange(headPitch, -20f, 20f))
                        {   fluff.xRot = CSMath.toRadians(CSMath.shrink(CSMath.clamp(headPitch, -60f, 60f), 20));
                        }
                        else fluff.xRot = 0;
                        fluff.y = 1;

                        yield model;
                    }
                    case LEGS -> RegisterModels.GOAT_PANTS_MODEL;
                    case FEET -> RegisterModels.GOAT_BOOTS_MODEL;
                    default -> null;
                };
            }
        });
    }
}
