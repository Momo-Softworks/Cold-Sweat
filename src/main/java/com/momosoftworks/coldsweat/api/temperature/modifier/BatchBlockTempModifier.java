package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

/**
 * A {@link TempModifier} adapter used by the batch temperature computation: its {@link #calculate} directly
 * returns a pre-computed block-temperature function instead of scanning the world.
 * <br/>
 * This is an internal wiring helper for {@code WorldHelper.getTemperaturesAt} and is not part of the registered
 * modifier set, so it is never looked up through {@code TempModifierRegistry}.
 *
 * @author liudongyu
 */
public final class BatchBlockTempModifier extends TempModifier {
	private final Function<Double, Double> temperatureGetter;

	/**
	 * Constructs a batch adapter backed by a pre-computed temperature function.
	 *
	 * @param temperatureGetter the pre-computed block-temperature function returned by {@code calculate}
	 */
	public BatchBlockTempModifier(Function<Double, Double> temperatureGetter) {
		this.temperatureGetter = temperatureGetter;
	}

	@Override
	protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait) {
		return this.temperatureGetter;
	}
}