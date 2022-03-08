package dev.momostudios.coldsweat.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class HearthBlockEntityRenderer<T extends HearthBlockEntity> implements BlockEntityRenderer<HearthBlockEntity>
{
    @Override
    public void render(@Nonnull HearthBlockEntity hearthEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay)
    {
        poseStack.pushPose();
        bufferSource.getBuffer(RenderType.cutout()).vertex(0, 0, 0);
        bufferSource.getBuffer(RenderType.cutout()).color(1, 0, 0, 1);
        bufferSource.getBuffer(RenderType.cutout()).vertex(1, 1, 1);
        poseStack.popPose();
    }
}
