package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.*;

public class NegatableList<T>
{
    private final List<T> requirements;
    private final List<T> exclusions;
    private final boolean singleton;

    /**
     * Provides a codec that can be either a qualified list or a single element.
     */
    public static <T> Codec<NegatableList<T>> codec(Codec<T> codec) {
        Codec<NegatableList<T>> listCodec = RecordCodecBuilder.create(instance -> instance.group(
                codec.listOf().fieldOf("require").forGetter(predicate -> predicate.requirements),
                codec.listOf().optionalFieldOf("exclude", List.of()).forGetter(predicate -> predicate.exclusions)
        ).apply(instance, NegatableList::new));

        return Codec.either(listCodec, codec)
                .comapFlatMap(either -> {
                          if (either.left().isPresent())
                          {   return DataResult.success(either.left().get());
                          }
                          else return DataResult.success(new NegatableList<>(List.of(either.right().get())));
                      },
                      list -> {
                          if (list.singleton && list.exclusions.isEmpty())
                          {   return Either.right(list.requirements.get(0));
                          }
                          else return Either.left(list);
                      });
    }

    /**
     * Provides a codec that can be either a qualified list or a list of elements.
     */
    public static <T> Codec<NegatableList<T>> listCodec(Codec<T> codec) {
        Codec<NegatableList<T>> listCodec = RecordCodecBuilder.create(instance -> instance.group(
                codec.listOf().fieldOf("require").forGetter(predicate -> predicate.requirements),
                codec.listOf().optionalFieldOf("exclude", List.of()).forGetter(predicate -> predicate.exclusions)
        ).apply(instance, NegatableList::new));

        return Codec.either(listCodec, codec.listOf())
                .comapFlatMap(either -> {
                                  if (either.left().isPresent())
                                  {   return DataResult.success(either.left().get());
                                  }
                                  else return DataResult.success(new NegatableList<>(either.right().get()));
                              },
                              list -> {
                                    if (list.singleton && list.exclusions.isEmpty())
                                    {   return Either.right(list.requirements);
                                    }
                                    else return Either.left(list);
                              });
    }

    public static <T>StreamCodec<RegistryFriendlyByteBuf, NegatableList<T>> streamCodec(StreamCodec<RegistryFriendlyByteBuf, T> codec)
    {
        return new StreamCodec<>()
        {
            @Override
            public NegatableList<T> decode(RegistryFriendlyByteBuf buf)
            {
                List<T> requirements = new ArrayList<>();
                List<T> exclusions = new ArrayList<>();
                int size = buf.readVarInt();
                for (int i = 0; i < size; i++)
                {   requirements.add(codec.decode(buf));
                }
                size = buf.readVarInt();
                for (int i = 0; i < size; i++)
                {   exclusions.add(codec.decode(buf));
                }
                return new NegatableList<>(requirements, exclusions);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, NegatableList<T> list)
            {
                buf.writeVarInt(list.requirements.size());
                list.requirements.forEach(e -> codec.encode(buf, e));
                buf.writeVarInt(list.exclusions.size());
                list.exclusions.forEach(e -> codec.encode(buf, e));
            }
        };
    }

    public NegatableList()
    {
        this.requirements = new ArrayList<>();
        this.exclusions = new ArrayList<>();
        this.singleton = false;
    }

    public NegatableList(T requirement)
    {
        this.requirements = new ArrayList<>(List.of(requirement));
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
    {   return this.flatMap(mapper.andThen(p -> CSMath.mutable(List.of(p))), CSMath::merge, List::removeAll).orElse(List.of());
    }

    public List<T> flatten()
    {   return this.listMap(p -> p);
    }

    public <N> List<N> flatListMap(Function<T, List<N>> mapper)
    {   return listMap(mapper).stream().flatMap(List::stream).toList();
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
