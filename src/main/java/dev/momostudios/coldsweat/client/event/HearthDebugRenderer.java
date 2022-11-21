package dev.momostudios.coldsweat.client.event;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import dev.momostudios.coldsweat.common.event.HearthPathManagement;
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
    public static Map<BlockPos, Set<Pair<BlockPos, ArrayList<Direction>>>> HEARTH_LOCATIONS = new HashMap<>();

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
            BiConsumer<Vector3f, Float> nw = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 1, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 1, 0).endVertex();
            };
            BiConsumer<Vector3f, Float> ne = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 1, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 1, 0).endVertex();
            };
            BiConsumer<Vector3f, Float> sw = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, -1, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, -1, 0).endVertex();
            };
            BiConsumer<Vector3f, Float> se = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 1, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 1, 0).endVertex();
            };
            BiConsumer<Vector3f, Float> nu = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, -1, 0, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, -1, 0, 0).endVertex();
            };
            BiConsumer<Vector3f, Float> nd = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 1, 0, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 1, 0, 0).endVertex();
            };
            BiConsumer<Vector3f, Float> su = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 1, 0, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 1, 0, 0).endVertex();
            };
            BiConsumer<Vector3f, Float> sd = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 1, 0, 0).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 1, 0, 0).endVertex();
            };
            BiConsumer<Vector3f, Float> eu = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 0, 1).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 0, 1).endVertex();
            };
            BiConsumer<Vector3f, Float> ed = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 0, -1).endVertex();
                vertexes.vertex(matrix4f, pos.x()+1, pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 0, -1).endVertex();
            };
            BiConsumer<Vector3f, Float> wu = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 0, 1).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y()+1, pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 0, 1).endVertex();
            };
            BiConsumer<Vector3f, Float> wd = (pos, renderAlpha) ->
            {
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 0, 1).endVertex();
                vertexes.vertex(matrix4f, pos.x(), pos.y(), pos.z()+1).color(1f, 0.7f, 0.6f, renderAlpha).normal(matrix3f, 0, 0, 1).endVertex();
            };

            LevelChunk workingChunk = (LevelChunk) level.getChunk(new BlockPos(0, 0, 0));

            for (Map.Entry<BlockPos, Set<Pair<BlockPos, ArrayList<Direction>>>> entry : HEARTH_LOCATIONS.entrySet())
            {
                if (HearthPathManagement.DISABLED_HEARTHS.contains(Pair.of(entry.getKey(), level.dimension().location().toString()))) continue;

                Set<Pair<BlockPos, ArrayList<Direction>>> points = entry.getValue();
                for (Pair<BlockPos, ArrayList<Direction>> pair : points)
                {
                    BlockPos pos = pair.getFirst();
                    ArrayList<Direction> directions = pair.getSecond();

                    float x = pos.getX();
                    float y = pos.getY();
                    float z = pos.getZ();
                    float renderAlpha = CSMath.blend(0.2f, 0f, (float) CSMath.getDistance(player, x + 0.5f, y + 0.5f, z + 0.5f), 5, 16);

                    if (renderAlpha > 0.01f && frustum.isVisible(new AABB(pos)))
                    {
                        ChunkPos chunkPos = new ChunkPos(pos);
                        if (!workingChunk.getPos().equals(chunkPos))
                            workingChunk = (LevelChunk) level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);

                        if (WorldHelper.getBlockState(workingChunk, pos).getShape(level, pos).equals(Shapes.block()))
                        {
                            LevelRenderer.renderLineBox(ps, vertexes, x, y, z, x + 1, y + 1, z + 1, 1f, 0.7f, 0.6f, renderAlpha);
                            continue;
                        }

                        if (directions.size() == 6) continue;

                        Set<BiConsumer<Vector3f, Float>> lines = Sets.newHashSet(nw, ne, sw, se, nu, nd, su, sd, eu, ed, wu, wd);

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

                        lines.forEach(line -> line.accept(new Vector3f(x, y, z), renderAlpha));
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
            HEARTH_LOCATIONS.clear();
            for (Map.Entry<BlockPos, Integer> entry : HearthPathManagement.HEARTH_POSITIONS.entrySet())
            {
                BlockPos pos = entry.getKey();
                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);
                if (blockEntity instanceof HearthBlockEntity hearth)
                {
                    ArrayList<BlockPos> paths = hearth.getPaths().stream().map(SpreadPath::getPos).collect(Collectors.toCollection(ArrayList::new));
                    HEARTH_LOCATIONS.put(pos, paths.stream().map(bp ->
                    {
                        ArrayList<Direction> dirs = new ArrayList<>();
                        for (Direction dir : Direction.values())
                        {
                            if (paths.contains(bp.relative(dir)))
                                dirs.add(dir);
                        }
                        return Pair.of(bp, dirs);
                    }).collect(Collectors.toSet()));
                }
            }
        }
    }
}
