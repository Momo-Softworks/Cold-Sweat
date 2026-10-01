package com.momosoftworks.coldsweat.client.event;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.util.ARGB;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EventBusSubscriber(Dist.CLIENT)
public class HearthDebugRenderer
{
    public static Map<BlockPos, Map<BlockPos, Collection<Direction>>> HEARTH_LOCATIONS = new HashMap<>();

    /**
     * Drawn with gizmos, which must be emitted during the client tick
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        if (!Minecraft.getInstance().getDebugOverlay().showDebugScreen() || !ConfigSettings.HEARTH_DEBUG.get()) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        Level level = player.level();

        // Edges of a block, as pairs of corner offsets
        float[][] edges = {
            {0,0,0, 0,1,0}, {1,0,0, 1,1,0}, {0,0,1, 0,1,1}, {1,0,1, 1,1,1}, // vertical: nw, ne, sw, se
            {0,1,0, 1,1,0}, {0,0,0, 1,0,0}, {0,1,1, 1,1,1}, {0,0,1, 1,0,1}, // north/south: nu, nd, su, sd
            {1,1,0, 1,1,1}, {1,0,0, 1,0,1}, {0,1,0, 0,1,1}, {0,0,0, 0,0,1}  // east/west: eu, ed, wu, wd
        };
        final int NW = 0, NE = 1, SW = 2, SE = 3, NU = 4, ND = 5, SU = 6, SD = 7, EU = 8, ED = 9, WU = 10, WD = 11;

        ChunkAccess workingChunk = null;
        float viewDistance = Minecraft.getInstance().options.renderDistance().get() * 2f;

        List<BlockPos> invalidHearths = new ArrayList<>();
        for (Map.Entry<BlockPos, Map<BlockPos, Collection<Direction>>> entry : HEARTH_LOCATIONS.entrySet())
        {
            if (!(level.getBlockEntity(entry.getKey()) instanceof HearthBlockEntity))
            {   invalidHearths.add(entry.getKey());
                continue;
            }
            if (HearthSaveDataHandler.DISABLED_HEARTHS.contains(Pair.of(entry.getKey(), level.dimension().identifier().toString()))) continue;

            Map<BlockPos, Collection<Direction>> points = entry.getValue();
            for (Map.Entry<BlockPos, Collection<Direction>> pair : points.entrySet())
            {
                BlockPos pos = pair.getKey();
                Collection<Direction> directions = pair.getValue();

                float x = pos.getX();
                float y = pos.getY();
                float z = pos.getZ();

                float renderAlpha = CSMath.blend(1f, 0f, (float) CSMath.getDistance(player, x + 0.5f, y + 0.5f, z + 0.5f), 5, viewDistance);
                if (renderAlpha <= 0.01f) continue;
                int color = ARGB.colorFromFloat(renderAlpha, 1f, 0.7f, 0.6f);

                ChunkPos chunkPos = ChunkPos.containing(pos);
                if (workingChunk == null || !workingChunk.getPos().equals(chunkPos))
                    workingChunk = WorldHelper.getChunk(level, pos);
                if (workingChunk == null) continue;

                BlockState state = workingChunk.getBlockState(pos);
                VoxelShape blockShape = state.getShape(level, pos);
                if (!blockShape.isEmpty() && !state.getCollisionShape(level, pos).isEmpty())
                {
                    blockShape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    {
                        Gizmos.cuboid(new AABB(minX + x - 0.001, minY + y - 0.001, minZ + z - 0.001,
                                               maxX + x + 0.001, maxY + y + 0.001, maxZ + z + 0.001), GizmoStyle.stroke(color));
                    });
                    continue;
                }

                if (directions.size() == 6) continue;

                Set<Integer> lines = new HashSet<>(List.of(NW, NE, SW, SE, NU, ND, SU, SD, EU, ED, WU, WD));

                // Remove the lines if another point is on the adjacent face
                for (Direction direction : directions)
                {
                    switch (direction)
                    {
                        case DOWN -> lines.removeAll(List.of(ND, SD, ED, WD));
                        case UP -> lines.removeAll(List.of(NU, SU, EU, WU));
                        case NORTH -> lines.removeAll(List.of(NW, NE, NU, ND));
                        case SOUTH -> lines.removeAll(List.of(SW, SE, SU, SD));
                        case WEST -> lines.removeAll(List.of(NW, SW, WU, WD));
                        case EAST -> lines.removeAll(List.of(NE, SE, EU, ED));
                    }
                }

                for (int line : lines)
                {
                    float[] e = edges[line];
                    Gizmos.line(new Vec3(x + e[0], y + e[1], z + e[2]), new Vec3(x + e[3], y + e[4], z + e[5]), color);
                }
            }
        }
        invalidHearths.forEach(HEARTH_LOCATIONS::remove);
    }

    public static void updatePaths(HearthBlockEntity hearth)
    {
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
                return Map.entry(path, dirs);
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
    }
}
