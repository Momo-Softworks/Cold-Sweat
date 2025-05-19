package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.*;

public class NegatableList<T>
{
    private final List<T> requirements;
    private final List<T> exclusions;
    private final boolean singleton;
    private final boolean requireAll;
    private final boolean excludeAll;

    private static <T> Codec<NegatableList<T>> getCodec(Codec<T> codec)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                codec.listOf().optionalFieldOf("require", List.of()).forGetter(predicate -> predicate.requirements),
                codec.listOf().optionalFieldOf("exclude", List.of()).forGetter(predicate -> predicate.exclusions),
                Codec.BOOL.optionalFieldOf("require_all", false).forGetter(predicate -> predicate.requireAll),
                Codec.BOOL.optionalFieldOf("exclude_all", false).forGetter(predicate -> predicate.excludeAll)
        ).apply(instance, NegatableList::new));
    }

    /**
     * Provides a codec that can be either a qualified list or a single element.
     */
    public static <T> Codec<NegatableList<T>> codec(Codec<T> codec)
    {
        Codec<NegatableList<T>> listCodec = getCodec(codec);

        return Codec.either(codec, listCodec).comapFlatMap(
                either -> {
                    if (either.right().isPresent())
                    {   return DataResult.success(either.right().get());
                    }
                    else return DataResult.success(new NegatableList<>(either.left().get()));
                },
                list -> {
                    if (list.singleton && list.exclusions.isEmpty())
                    {   return Either.left(list.requirements.get(0));
                    }
                    else return Either.right(list);
                });
    }

    /**
     * Provides a codec that can be either a qualified list or a list of elements.
     */
    public static <T> Codec<NegatableList<T>> listCodec(Codec<T> codec)
    {
        Codec<NegatableList<T>> listCodec = getCodec(codec);

        return Codec.either(codec.listOf(), listCodec).comapFlatMap(
                either -> {
                      if (either.right().isPresent())
                      {   return DataResult.success(either.right().get());
                      }
                      else return DataResult.success(new NegatableList<>(either.left().get(), false, false));
                },
                list -> {
                      if (list.singleton && list.exclusions.isEmpty())
                      {   return Either.left(list.requirements);
                      }
                      else return Either.right(list);
                });
    }

    public NegatableList()
    {
        this.requirements = new ArrayList<>();
        this.exclusions = new ArrayList<>();
        this.singleton = false;
        this.requireAll = false;
        this.excludeAll = false;
    }

    public NegatableList(T requirement)
    {
        this.requirements = new ArrayList<>(List.of(requirement));
        this.exclusions = new ArrayList<>();
        this.singleton = true;
        this.requireAll = false;
        this.excludeAll = false;
    }

    public NegatableList(List<T> requirements, boolean requireAll, boolean excludeAll)
    {
        this.requirements = new ArrayList<>(requirements);
        this.exclusions = new ArrayList<>();
        this.singleton = requirements.size() == 1;
        this.requireAll = requireAll;
        this.excludeAll = excludeAll;
    }
    public NegatableList(List<T> requirements)
    {   this(requirements, false, false);
    }

    public NegatableList(List<T> requirements, List<T> exclusions, boolean requireAll, boolean excludeAll)
    {
        this.requirements = new ArrayList<>(requirements);
        this.exclusions = new ArrayList<>(exclusions);
        this.singleton = exclusions.isEmpty() && requirements.size() == 1;
        this.requireAll = requireAll;
        this.excludeAll = excludeAll;
    }
    public NegatableList(List<T> requirements, List<T> exclusions)
    {   this(requirements, exclusions, false, false);
    }

    public List<T> requirements()
    {   return this.requirements;
    }
    public List<T> exclusions()
    {   return this.exclusions;
    }

    public void add(T element, boolean negate)
    {
        if (negate)
        {   this.exclusions.add(element);
        }
        else this.requirements.add(element);
    }

    public boolean isEmpty()
    {   return this.requirements.isEmpty() && this.exclusions.isEmpty();
    }

    public <N> Optional<N> flatMap(Function<T, N> mapper, BinaryOperator<N> reducer, BiConsumer<N, N> remover)
    {
        // First map and reduce the requirements
        Optional<N> requiredResult = this.requirements.stream()
                .map(mapper)
                .reduce(reducer);

        // Then map and reduce the exclusions
        Optional<N> exclusionResult = this.exclusions.stream()
                .map(mapper)
                .reduce(reducer);

        if (requiredResult.isPresent() && exclusionResult.isPresent())
        {   remover.accept(requiredResult.get(), exclusionResult.get());
        }
        return requiredResult;
    }

    public <N> Optional<N> flatMap(Function<T, N> mapper, BinaryOperator<N> reducer)
    {   return this.flatMap(mapper, reducer, (a, b) -> {});
    }

    public <N> List<N> listMap(Function<T, N> mapper)
    {   return this.flatMap(mapper.andThen(p -> CSMath.mutable(List.of(p))), CSMath::append, List::removeAll).orElse(List.of());
    }

    public List<T> flatten()
    {   return this.listMap(p -> p);
    }

    public <N> List<N> flatListMap(Function<T, List<N>> mapper)
    {   return listMap(mapper).stream().flatMap(List::stream).toList();
    }

    public <N> List<N> nestedFlatMap(Function<T, NegatableList<N>> mapper)
    {   return listMap(mapper).stream().map(NegatableList::flatten).flatMap(List::stream).toList();
    }

    public boolean test(Predicate<T> test)
    {
        if (!this.requirements.isEmpty())
        {
            require:
            {
                for (int i = 0; i < this.requirements.size(); i++)
                {
                    boolean result = test.test(this.requirements.get(i));
                    if (this.requireAll && !result)
                    {   return false;
                    }
                    if (!this.requireAll && result)
                    {   break require;
                    }
                }
                return this.requireAll;
            }
        }
        if (!this.exclusions.isEmpty())
        {
            for (int i = 0; i < this.exclusions.size(); i++)
            {
                boolean result = test.test(this.exclusions.get(i));
                if (this.excludeAll && !result)
                {   break;
                }
                if (!this.excludeAll && result)
                {   return false;
                }
            }
            return !this.excludeAll;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        NegatableList<?> that = (NegatableList<?>) obj;
        return this.requirements.equals(that.requirements) && this.exclusions.equals(that.exclusions);
    }

    @Override
    public String toString()
    {
        return "NegatableList{" +
                "requirements=" + requirements +
                ", exclusions=" + exclusions +
                '}';
    }
}
