package com.momosoftworks.coldsweat.client.event;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.event.client.RenderWorldEvent;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.api.distmarker.Dist;
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
    public static void onLevelRendered(RenderWorldEvent event)
    {
        if (Minecraft.getInstance().options.renderDebug && ConfigSettings.HEARTH_DEBUG.get())
        {
            PlayerEntity player = Minecraft.getInstance().player;
            if (player == null) return;


            MatrixStack ms = event.getMatrixStack();
            Vector3d camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            World world = player.level;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            IVertexBuilder vertexes = buffer.getBuffer(RenderType.LINES);

            ms.pushPose();
            ms.translate(-camPos.x, -camPos.y, -camPos.z);
            Matrix4f matrix4f = ms.last().pose();
            Matrix3f matrix3f = ms.last().normal();

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

            IChunk workingChunk = null;
            float viewDistance = Minecraft.getInstance().options.renderDistance * 2f;

            List<BlockPos> invalidHearths = new ArrayList<>();
            for (Map.Entry<BlockPos, Map<BlockPos, Collection<Direction>>> entry : HEARTH_LOCATIONS.entrySet())
            {
                if (!(world.getBlockEntity(entry.getKey()) instanceof HearthBlockEntity))
                {   invalidHearths.add(entry.getKey());
                    continue;
                }
                if (HearthSaveDataHandler.DISABLED_HEARTHS.contains(Pair.of(entry.getKey(), world.dimension().location().toString()))) continue;

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

                    float renderAlpha = CSMath.blend(1f, 0f, (float) CSMath.getDistance(player.position(), Vector3d.atCenterOf(pos)), 5, viewDistance);

                    if (renderAlpha > 0.01f && new ClippingHelper(matrix4f, event.getProjectionMatrix()).isVisible(new AxisAlignedBB(pos)))
                    {
                        ChunkPos chunkPos = new ChunkPos(pos);
                        if (workingChunk == null || !workingChunk.getPos().equals(chunkPos))
                            workingChunk = WorldHelper.getChunk(world, pos);
                        if (workingChunk == null) continue;

                        if (!WorldHelper.canSeeSky(world, pos, 1))
                        {
                            for (AxisAlignedBB aabb : world.getBlockState(pos).getShape(world, pos).toAabbs())
                            {   WorldRenderer.renderLineBox(ms, vertexes, aabb.minX + x, aabb.minY + y, aabb.minZ + z, aabb.maxX + x, aabb.maxY + y, aabb.maxZ + z, r, g, b, renderAlpha);
                            }
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
            invalidHearths.forEach(HEARTH_LOCATIONS::remove);
            RenderSystem.disableBlend();
            ms.popPose();
            buffer.endBatch(RenderType.LINES);
        }
    }

    public static void updatePaths(HearthBlockEntity hearth)
    {
        if (!hearth.isSpreading()) return;
        BlockPos pos = hearth.getBlockPos();
        Set<BlockPos> paths = hearth.getPaths().stream().map(path -> path.pos).collect(Collectors.toSet());

        Map<BlockPos, Collection<Direction>> pathMap = HEARTH_LOCATIONS.computeIfAbsent(pos, k -> Maps.newHashMap());
        if (pathMap.size() != paths.size())
        {
            HEARTH_LOCATIONS.put(pos, paths.stream().map(path ->
            {
                ArrayList<Direction> dirs = new ArrayList<>();
                for (int i = 0; i < Direction.values().length; i++)
                {
                    Direction dir = Direction.values()[i];
                    BlockPos dirPos = path.relative(dir);
                    if (paths.contains(dirPos))
                    {   dirs.add(dir);
                    }
                }
                return new AbstractMap.SimpleEntry<>(path, dirs);
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
    }
}
