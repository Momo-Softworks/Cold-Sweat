package dev.momostudios.coldsweat.common.item;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;

public class GoatArmorItem extends ArmorItem
{
    public GoatArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties properties)
    {   super(material, slot, properties);
    }

    /*@Override
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
    }*/
}
