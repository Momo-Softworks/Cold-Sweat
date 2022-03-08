package dev.momostudios.coldsweat.common.blockentity;

import dev.momostudios.coldsweat.util.world.SpreadPath;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;

public interface ISpreadingArea
{
    ConcurrentHashMap<BlockPos, SpreadPath> paths = new ConcurrentHashMap<>();

    @Nonnull
    ConcurrentHashMap<BlockPos, SpreadPath> getPathMap();

    void setPathMap(ConcurrentHashMap<BlockPos, SpreadPath> map);

    default void addPath(SpreadPath pos) {
        paths.put(pos.getPos(), pos);
    }

    void addPaths(ConcurrentHashMap<BlockPos, SpreadPath> map);

    void remove(SpreadPath pos);

    void clear();

    /**
     * Used for reading/writing the paths to NBT. This method is empty because the Hearth doesn't save its paths,
     * since it immediately rebuilds its AOE upon loading the world anyway.
     */
    default void savePaths() {
    }

    /**
     * Used for reading/writing the paths to NBT. This method is empty because the Hearth doesn't save its paths,
     * since it immediately rebuilds its AOE upon loading the world anyway.
     */
    default void loadPaths() {
    }
}
