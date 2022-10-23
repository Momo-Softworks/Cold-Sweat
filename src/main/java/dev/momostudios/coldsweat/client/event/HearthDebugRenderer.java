package dev.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import dev.momostudios.coldsweat.api.event.client.RenderLevelEvent;
import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import dev.momostudios.coldsweat.common.event.HearthPathManagement;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HearthDebugRenderer
{
    public static Map<BlockPos, Set<BlockPos>> HEARTH_LOCATIONS = new HashMap<>();

    @SubscribeEvent
    public static void onLevelRendered(RenderLevelEvent event)
    {
        if (Minecraft.getInstance().options.renderDebug && ClientSettingsConfig.getInstance().hearthDebug())
        {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            Frustum frustum = event.getFrustum();
            PoseStack ps = event.getPoseStack();
            Vec3 camPos = event.getCamera().getPosition();

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer vertexes = buffer.getBuffer(RenderType.LINES);

            ps.pushPose();
            ps.translate(-camPos.x, -camPos.y, -camPos.z);
            Matrix4f matrix4f = ps.last().pose();
            Matrix3f matrix3f = ps.last().normal();

            // Points to draw lines
            BiConsumer<Vector3f, Float> nw = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };
            BiConsumer<Vector3f, Float> ne = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };
            BiConsumer<Vector3f, Float> sw = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };
            BiConsumer<Vector3f, Float> se = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };
            BiConsumer<Vector3f, Float> nu = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };
            BiConsumer<Vector3f, Float> nd = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };
            BiConsumer<Vector3f, Float> su = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };
            BiConsumer<Vector3f, Float> sd = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };
            BiConsumer<Vector3f, Float> eu = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };
            BiConsumer<Vector3f, Float> ed = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };
            BiConsumer<Vector3f, Float> wu = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };
            BiConsumer<Vector3f, Float> wd = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0f, 0f, 0f).endVertex();
            };

            for (Map.Entry<BlockPos, Set<BlockPos>> entry : HEARTH_LOCATIONS.entrySet())
            {
                Set<BlockPos> points = entry.getValue();
                for (BlockPos pos : points)
                {
                    float x = pos.getX();
                    float y = pos.getY();
                    float z = pos.getZ();
                    float renderAlpha = CSMath.blend(0.2f, 0f, (float) CSMath.getDistance(player, x + 0.5f, y + 0.5f, z + 0.5f), 5, 16);

                    if (renderAlpha > 0.01f && frustum.isVisible(new AABB(pos)))
                    {
                        Set<BiConsumer<Vector3f, Float>> lines = new HashSet<>(Arrays.asList(nw, ne, sw, se, nu, nd, su, sd, eu, ed, wu, wd));

                        // Remove the lines if another point is on the adjacent face
                        if (points.contains(pos.below()))
                            Arrays.asList(nd, sd, ed, wd).forEach(lines::remove);
                        if (points.contains(pos.above()))
                            Arrays.asList(nu, su, eu, wu).forEach(lines::remove);
                        if (points.contains(pos.north()))
                            Arrays.asList(nw, ne, nu, nd).forEach(lines::remove);
                        if (points.contains(pos.south()))
                            Arrays.asList(sw, se, su, sd).forEach(lines::remove);
                        if (points.contains(pos.west()))
                            Arrays.asList(nw, sw, wu, wd).forEach(lines::remove);
                        if (points.contains(pos.east()))
                            Arrays.asList(ne, se, eu, ed).forEach(lines::remove);

                        if (lines.isEmpty()) continue;

                        lines.forEach(line -> line.accept(new Vector3f(x, y, z), renderAlpha));
                    }
                }
            }
            RenderSystem.disableBlend();
            ps.popPose();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null
        && Minecraft.getInstance().level.getGameTime() % 20 == 0 && Minecraft.getInstance().options.renderDebug)
        {
            HEARTH_LOCATIONS.clear();
            for (Map.Entry<BlockPos, Integer> entry : HearthPathManagement.HEARTH_POSITIONS.entrySet())
            {
                BlockPos pos = entry.getKey();
                HEARTH_LOCATIONS.put(pos, new HashSet<>(((HearthBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos)).getPaths()));
            }
        }
    }
}
