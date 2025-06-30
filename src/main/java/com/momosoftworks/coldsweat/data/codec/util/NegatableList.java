package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

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
                codec.listOf().optionalFieldOf("require", Arrays.asList()).forGetter(predicate -> predicate.requirements),
                codec.listOf().optionalFieldOf("exclude", Arrays.asList()).forGetter(predicate -> predicate.exclusions),
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
        this.requirements = new ArrayList<>(Arrays.asList(requirement));
        this.exclusions = new ArrayList<>();
        this.singleton = true;
        this.requireAll = false;
        this.excludeAll = false;
    }

    public NegatableList(Collection<T> requirements, boolean requireAll, boolean excludeAll)
    {
        this.requirements = new ArrayList<>(requirements);
        this.exclusions = new ArrayList<>();
        this.singleton = requirements.size() == 1;
        this.requireAll = requireAll;
        this.excludeAll = excludeAll;
    }
    public NegatableList(Collection<T> requirements)
    {   this(requirements, false, false);
    }

    public NegatableList(Collection<T> requirements, Collection<T> exclusions, boolean requireAll, boolean excludeAll)
    {
        this.requirements = new ArrayList<>(requirements);
        this.exclusions = new ArrayList<>(exclusions);
        this.singleton = exclusions.isEmpty() && requirements.size() == 1;
        this.requireAll = requireAll;
        this.excludeAll = excludeAll;
    }
    public NegatableList(Collection<T> requirements, Collection<T> exclusions)
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

    public <N> NegatableList<N> flatten(Function<T, NegatableList<N>> mapper)
    {
        List<N> requirements = this.requirements.stream()
                .map(mapper)
                .flatMap(list -> list.requirements.stream())
                .collect(Collectors.toList());
        List<N> exclusions = this.exclusions.stream()
                .map(mapper)
                .flatMap(list -> list.exclusions.stream())
                .collect(Collectors.toList());
        return new NegatableList<>(requirements, exclusions, this.requireAll, this.excludeAll);
    }

    public List<T> flatList()
    {
        List<T> flatList = new ArrayList<>(this.requirements);
        flatList.removeAll(this.exclusions);
        return flatList;
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
