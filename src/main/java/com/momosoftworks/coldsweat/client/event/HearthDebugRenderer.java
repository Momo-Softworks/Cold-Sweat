package com.momosoftworks.coldsweat.client.event;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HearthDebugRenderer
{
    public static Map<BlockPos, Map<BlockPos, Collection<Direction>>> HEARTH_LOCATIONS = new HashMap<>();

    @SubscribeEvent
    public static void onLevelRendered(RenderLevelStageEvent event)
    {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES
        && Minecraft.getInstance().options.renderDebug && ConfigSettings.HEARTH_DEBUG.get())
        {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            Frustum frustum = event.getFrustum();
            PoseStack ps = event.getPoseStack();
            Vec3 camPos = event.getCamera().getPosition();
            Level level = player.level;

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
            BiConsumer<Vector3f, Vector4f> nw = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 1, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 1, 0).endVertex();
            };
            BiConsumer<Vector3f, Vector4f> ne = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 1, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 1, 0).endVertex();
            };
            BiConsumer<Vector3f, Vector4f> sw = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, -1, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, -1, 0).endVertex();
            };
            BiConsumer<Vector3f, Vector4f> se = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 1, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 1, 0).endVertex();
            };
            BiConsumer<Vector3f, Vector4f> nu = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, -1, 0, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, -1, 0, 0).endVertex();
            };
            BiConsumer<Vector3f, Vector4f> nd = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 1, 0, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 1, 0, 0).endVertex();
            };
            BiConsumer<Vector3f, Vector4f> su = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 1, 0, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 1, 0, 0).endVertex();
            };
            BiConsumer<Vector3f, Vector4f> sd = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 1, 0, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 1, 0, 0).endVertex();
            };
            BiConsumer<Vector3f, Vector4f> eu = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 0, 1).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 0, 1).endVertex();
            };
            BiConsumer<Vector3f, Vector4f> ed = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 0, -1).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 0, -1).endVertex();
            };
            BiConsumer<Vector3f, Vector4f> wu = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 0, 1).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 0, 1).endVertex();
            };
            BiConsumer<Vector3f, Vector4f> wd = (pos, color) -> {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 0, 1).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()+1).color(color.x(), color.y(), color.z(), color.w()).normal(matrix3f, 0, 0, 1).endVertex();
            };

            ChunkAccess workingChunk = null;
            float viewDistance = Minecraft.getInstance().options.renderDistance().get() * 2f;

            for (Map.Entry<BlockPos, Map<BlockPos, Collection<Direction>>> entry : HEARTH_LOCATIONS.entrySet())
            {
                if (HearthSaveDataHandler.DISABLED_HEARTHS.contains(Pair.of(entry.getKey(), level.dimension().location().toString()))) continue;

                Map<BlockPos, Collection<Direction>> points = entry.getValue();
                for (Map.Entry<BlockPos, Collection<Direction>> pair : points.entrySet())
                {
                    BlockPos pos = pair.getKey();
                    Collection<Direction> directions = pair.getValue();

                    float x = pos.getX();
                    float y = pos.getY();
                    float z = pos.getZ();

                    float r = 1f;
                    float g = 0.7f;
                    float b = 0.6f;

                    float renderAlpha = CSMath.blend(1f, 0f, (float) CSMath.getDistance(player, x + 0.5f, y + 0.5f, z + 0.5f), 5, viewDistance);

                    if (renderAlpha > 0.01f && frustum.isVisible(new AABB(pos)))
                    {
                        ChunkPos chunkPos = new ChunkPos(pos);
                        if (workingChunk == null || !workingChunk.getPos().equals(chunkPos))
                            workingChunk = WorldHelper.getChunk(level, pos);
                        if (workingChunk == null) continue;

                        if (workingChunk.getBlockState(pos).getShape(level, pos).equals(Shapes.block()))
                        {   LevelRenderer.renderLineBox(ps, vertexes, x, y, z, x + 1, y + 1, z + 1, r, g, b, renderAlpha);
                            continue;
                        }

                        if (directions.size() == 6) continue;

                        Set<BiConsumer<Vector3f, Vector4f>> lines = Sets.newHashSet(nw, ne, sw, se, nu, nd, su, sd, eu, ed, wu, wd);

                        // Remove the lines if another point is on the adjacent face
                        if (directions.contains(Direction.DOWN))
                            Stream.of(nd, sd, ed, wd).forEach(lines::remove);
                        if (directions.contains(Direction.UP))
                            Stream.of(nu, su, eu, wu).forEach(lines::remove);
                        if (directions.contains(Direction.NORTH))
                            Stream.of(nw, ne, nu, nd).forEach(lines::remove);
                        if (directions.contains(Direction.SOUTH))
                            Stream.of(sw, se, su, sd).forEach(lines::remove);
                        if (directions.contains(Direction.WEST))
                            Stream.of(nw, sw, wu, wd).forEach(lines::remove);
                        if (directions.contains(Direction.EAST))
                            Stream.of(ne, se, eu, ed).forEach(lines::remove);

                        lines.forEach(line -> line.accept(new Vector3f(x, y, z), new Vector4f(r, g, b, renderAlpha)));
                    }
                }
            }
            RenderSystem.disableBlend();
            ps.popPose();
            buffer.endBatch(RenderType.LINES);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        ClientLevel level = Minecraft.getInstance().level;
        if (event.phase == TickEvent.Phase.END && level != null
        && level.getGameTime() % 20 == 0 && Minecraft.getInstance().options.renderDebug
        && ConfigSettings.HEARTH_DEBUG.get())
        {
            for (Pair<BlockPos, ResourceLocation> entry : HearthSaveDataHandler.HEARTH_POSITIONS)
            {
                if (!level.dimension().location().equals(entry.getSecond())) continue;

                BlockPos pos = entry.getFirst();
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof HearthBlockEntity hearth)
                {
                    Set<BlockPos> lookup = hearth.getPathLookup();

                    Map<BlockPos, Collection<Direction>> pathMap = HEARTH_LOCATIONS.computeIfAbsent(pos, k -> Maps.newHashMap());
                    if (pathMap.size() != lookup.size())
                    {
                        HEARTH_LOCATIONS.put(pos, lookup.stream().map(bp ->
                        {
                            ArrayList<Direction> dirs = new ArrayList<>();
                            for (Direction dir : Direction.values())
                            {
                                if (lookup.contains(bp.relative(dir)))
                                    dirs.add(dir);
                            }
                            return Map.entry(bp, dirs);
                        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, Maps::newHashMap)));
                    }
                }
            }
        }
    }
}
