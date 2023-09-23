package dev.momostudios.coldsweat.common.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.world.gen.blockstateprovider.BlockStateProvider;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class SoulStalkFeatureConfig implements IFeatureConfig
{
    int tries;
    int minCount;
    int maxCount;
    int spreadXZ;
    int spreadY;
    int diskWidth;
    int diskHeight;
    BlockStateProvider diskStateProvider;
    ITag<Block> replaceBlocks;

    public SoulStalkFeatureConfig(int tries, int minCount, int maxCount, int spreadXZ, int spreadY, int diskWidth, int diskHeight, BlockStateProvider diskStateProvider, ITag<Block> replaceBlocks)
    {   this.tries = tries;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.spreadXZ = spreadXZ;
        this.spreadY = spreadY;
        this.diskWidth = diskWidth;
        this.diskHeight = diskHeight;
        this.diskStateProvider = diskStateProvider;
        this.replaceBlocks = replaceBlocks;
    }

    public static final Codec<SoulStalkFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                // Required fields
                Codec.INT.fieldOf("tries").orElse(1).forGetter(config -> config.tries),
                Codec.INT.fieldOf("min_count").orElse(0).forGetter(config -> config.minCount),
                Codec.INT.fieldOf("max_count").orElse(1).forGetter(config -> config.maxCount),
                Codec.INT.fieldOf("spread_xz").orElse(1).forGetter(config -> config.spreadXZ),
                Codec.INT.fieldOf("spread_y").orElse(1).forGetter(config -> config.spreadY),
                // Optional fields
                Codec.INT.fieldOf("disk_width").orElse(0).forGetter(config -> config.diskWidth),
                Codec.INT.fieldOf("disk_height").orElse(0).forGetter(config -> config.diskHeight),
                BlockStateProvider.CODEC.fieldOf("disk_state_provider").orElse(new SimpleBlockStateProvider(Blocks.AIR.defaultBlockState())).forGetter(config -> config.diskStateProvider),
                ITag.codec(() -> TagCollectionManager.getInstance().getBlocks()).fieldOf("replace_blocks").orElseGet(() -> BlockTags.SAND).forGetter(config -> config.replaceBlocks)
                
        ).apply(instance, SoulStalkFeatureConfig::new);
    });
        
    public int getTries()
    {   return tries;
    }
}
