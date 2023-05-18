package dev.momostudios.coldsweat.client.event;

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
import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import dev.momostudios.coldsweat.common.event.HearthSaveDataHandler;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.world.SpreadPath;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
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

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HearthDebugRenderer
{
    public static Map<BlockPos, Map<SpreadPath, Collection<Direction>>> HEARTH_LOCATIONS = new HashMap<>();

    @SubscribeEvent
    public static void onLevelRendered(RenderLevelStageEvent event)
    {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES
        && Minecraft.getInstance().options.renderDebug && ClientSettingsConfig.getInstance().hearthDebug())
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

            LevelChunk workingChunk = (LevelChunk) level.getChunk(new BlockPos(0, 0, 0));
            float viewDistance = Minecraft.getInstance().options.renderDistance * 2f;

            for (Map.Entry<BlockPos, Map<SpreadPath, Collection<Direction>>> entry : HEARTH_LOCATIONS.entrySet())
            {
                if (HearthSaveDataHandler.DISABLED_HEARTHS.contains(Pair.of(entry.getKey(), level.dimension().location().toString()))) continue;

                Map<SpreadPath, Collection<Direction>> points = entry.getValue();
                for (Map.Entry<SpreadPath, Collection<Direction>> pair : points.entrySet())
                {
                    SpreadPath path = pair.getKey();
                    BlockPos pos = path.getPos();
                    Collection<Direction> directions = pair.getValue();

                    float x = path.getX();
                    float y = path.getY();
                    float z = path.getZ();

                    boolean spreading = !path.isFrozen();
                    float r = spreading ? 0.2f : 1f;
                    float g = spreading ? 1f : 0.7f;
                    float b = spreading ? 0.4f : 0.6f;

                    float renderAlpha = CSMath.blend(1f, 0f, (float) CSMath.getDistance(player, x + 0.5f, y + 0.5f, z + 0.5f), 5, viewDistance);

                    if (renderAlpha > 0.01f && frustum.isVisible(new AABB(pos)))
                    {
                        ChunkPos chunkPos = new ChunkPos(pos);
                        if (!workingChunk.getPos().equals(chunkPos))
                            workingChunk = (LevelChunk) level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);

                        if (WorldHelper.getBlockState(workingChunk, pos).getShape(level, pos).equals(Shapes.block()))
                        {
                            LevelRenderer.renderLineBox(ps, vertexes, x, y, z, x + 1, y + 1, z + 1, r, g, b, renderAlpha);
                            continue;
                        }

                        if (directions.size() == 6) continue;

                        Set<BiConsumer<Vector3f, Vector4f>> lines = Sets.newHashSet(nw, ne, sw, se, nu, nd, su, sd, eu, ed, wu, wd);

                        // Remove the lines if another point is on the adjacent face
                        if (directions.contains(Direction.DOWN))
                            Arrays.asList(nd, sd, ed, wd).forEach(lines::remove);
                        if (directions.contains(Direction.UP))
                            Arrays.asList(nu, su, eu, wu).forEach(lines::remove);
                        if (directions.contains(Direction.NORTH))
                            Arrays.asList(nw, ne, nu, nd).forEach(lines::remove);
                        if (directions.contains(Direction.SOUTH))
                            Arrays.asList(sw, se, su, sd).forEach(lines::remove);
                        if (directions.contains(Direction.WEST))
                            Arrays.asList(nw, sw, wu, wd).forEach(lines::remove);
                        if (directions.contains(Direction.EAST))
                            Arrays.asList(ne, se, eu, ed).forEach(lines::remove);

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
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null
        && Minecraft.getInstance().level.getGameTime() % 20 == 0 && Minecraft.getInstance().options.renderDebug
        && ClientSettingsConfig.getInstance().hearthDebug())
        {
            for (BlockPos pos : HearthSaveDataHandler.HEARTH_POSITIONS)
            {
                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);
                if (blockEntity instanceof HearthBlockEntity hearth)
                {
                    Collection<SpreadPath> paths = hearth.getPaths();
                    Set<BlockPos> lookup = hearth.getPathLookup();

                    Map<SpreadPath, Collection<Direction>> pathMap = HEARTH_LOCATIONS.computeIfAbsent(pos, k -> Maps.newHashMap());
                    if (pathMap.size() != paths.size())
                    {
                        HEARTH_LOCATIONS.put(pos, paths.stream().map(path ->
                        {
                            ArrayList<Direction> dirs = new ArrayList<>();
                            for (Direction dir : Direction.values())
                            {
                                if (lookup.contains(path.getPos().relative(dir)))
                                    dirs.add(dir);
                            }
                            return Map.entry(path, dirs);
                        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, Maps::newHashMap)));
                    }
                }
            }
        }
    }
}
