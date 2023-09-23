package dev.momosoftworks.coldsweat.common.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;

import java.util.List;

public record SoulStalkFeatureConfig(int tries, int minCount, int maxCount, int spreadXZ, int spreadY, int diskWidth, int diskHeight, RuleBasedBlockStateProvider diskStateProvider, BlockPredicate replaceBlocks) implements FeatureConfiguration
{
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
                RuleBasedBlockStateProvider.CODEC.fieldOf("disk_state_provider").orElse(new RuleBasedBlockStateProvider(BlockStateProvider.simple(Blocks.AIR), List.of())).forGetter(config -> config.diskStateProvider),
                BlockPredicate.CODEC.fieldOf("disk_replace_target").orElse(BlockPredicate.not(BlockPredicate.alwaysTrue())).forGetter(config -> config.replaceBlocks)
        ).apply(instance, SoulStalkFeatureConfig::new);
    });
}
