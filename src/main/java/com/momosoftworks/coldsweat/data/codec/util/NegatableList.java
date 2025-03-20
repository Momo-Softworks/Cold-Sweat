package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Collectors;

public class NegatableList<T>
{
    private final List<T> requirements;
    private final List<T> exclusions;
    private final boolean singleton;

    public static <T> Codec<NegatableList<T>> codec(Codec<T> codec) {
        Codec<NegatableList<T>> listCodec = RecordCodecBuilder.create(instance -> instance.group(
                codec.listOf().fieldOf("require").forGetter(predicate -> predicate.requirements),
                codec.listOf().optionalFieldOf("exclude", Arrays.asList()).forGetter(predicate -> predicate.exclusions)
        ).apply(instance, NegatableList::new));

        return ExtraCodecs.anyOf(listCodec, codec)
                .comapFlatMap(obj -> {
                          if (obj instanceof NegatableList<?>)
                          {   return DataResult.success((NegatableList<T>) obj);
                          }
                          else return DataResult.success(new NegatableList<>(Arrays.asList((T) obj)));
                      },
                      list -> {
                          if (list.singleton && list.exclusions.isEmpty())
                          {   return Either.right(list.requirements.get(0));
                          }
                          else return Either.left(list);
                      });
    }

    public NegatableList()
    {
        this.requirements = new ArrayList<>();
        this.exclusions = new ArrayList<>();
        this.singleton = false;
    }

    public NegatableList(T requirement)
    {
        this.requirements = new ArrayList<>(Arrays.asList(requirement));
        this.exclusions = new ArrayList<>();
        this.singleton = true;
    }

    public NegatableList(List<T> requirements)
    {
        this.requirements = new ArrayList<>(requirements);
        this.exclusions = new ArrayList<>();
        this.singleton = requirements.size() == 1;
    }

    public NegatableList(List<T> requirements, List<T> exclusions)
    {
        this.requirements = new ArrayList<>(requirements);
        this.exclusions = new ArrayList<>(exclusions);
        this.singleton = exclusions.isEmpty() && requirements.size() == 1;
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
    {   return this.flatMap(mapper.andThen(p -> CSMath.mutable(Arrays.asList(p))), CSMath::merge, List::removeAll).orElse(Arrays.asList());
    }

    public <N> List<N> flatListMap(Function<T, List<N>> mapper)
    {   return listMap(mapper).stream().flatMap(List::stream).collect(Collectors.toList());
    }

    public boolean test(Predicate<T> test)
    {
        if (!this.requirements.isEmpty())
        {
            for (int i = 0; i < this.requirements.size(); i++)
            {
                if (!test.test(this.requirements.get(i)))
                {   return false;
                }
            }
        }
        if (!this.exclusions.isEmpty())
        {
            for (int i = 0; i < this.exclusions.size(); i++)
            {
                if (test.test(this.exclusions.get(i)))
                {   return false;
                }
            }
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
