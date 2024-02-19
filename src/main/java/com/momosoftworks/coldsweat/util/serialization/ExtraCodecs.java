package com.momosoftworks.coldsweat.util.serialization;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import org.apache.commons.lang3.mutable.MutableObject;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtraCodecs {
    public static final Codec<JsonElement> JSON = Codec.PASSTHROUGH.xmap((p_253507_) -> {
        return p_253507_.convert(JsonOps.INSTANCE).getValue();
    }, (p_253513_) -> {
        return new Dynamic<>(JsonOps.INSTANCE, p_253513_);
    });
    public static final Codec<Integer> NON_NEGATIVE_INT = intRangeWithMessage(0, Integer.MAX_VALUE, (p_275703_) -> {
        return "Value must be non-negative: " + p_275703_;
    });
    public static final Codec<Integer> POSITIVE_INT = intRangeWithMessage(1, Integer.MAX_VALUE, (p_274847_) -> {
        return "Value must be positive: " + p_274847_;
    });
    public static final Codec<Float> POSITIVE_FLOAT = floatRangeMinExclusiveWithMessage(0.0F, Float.MAX_VALUE, (p_274876_) -> {
        return "Value must be positive: " + p_274876_;
    });
    public static final Codec<Pattern> PATTERN = Codec.STRING.comapFlatMap((p_274857_) -> {
        try {
            return DataResult.success(Pattern.compile(p_274857_));
        } catch (PatternSyntaxException patternsyntaxexception) {
            return DataResult.error("Invalid regex pattern '" + p_274857_ + "': " + patternsyntaxexception.getMessage());
        }
    }, Pattern::pattern);
    public static final Codec<Instant> INSTANT_ISO8601 = instantCodec(DateTimeFormatter.ISO_INSTANT);
    public static final Codec<byte[]> BASE64_STRING = Codec.STRING.comapFlatMap((p_274852_) -> {
        try {
            return DataResult.success(Base64.getDecoder().decode(p_274852_));
        } catch (IllegalArgumentException illegalargumentexception) {
            return DataResult.error("Malformed base64 string");
        }
    }, (p_216180_) -> {
        return Base64.getEncoder().encodeToString(p_216180_);
    });
    public static final Function<Optional<Long>, OptionalLong> toOptionalLong = (p_216176_) -> {
        return p_216176_.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    };
    public static final Function<OptionalLong, Optional<Long>> fromOptionalLong = (p_216178_) -> {
        return p_216178_.isPresent() ? Optional.of(p_216178_.getAsLong()) : Optional.empty();
    };
    public static final Codec<BitSet> BIT_SET = Codec.LONG_STREAM.xmap((p_253514_) -> {
        return BitSet.valueOf(p_253514_.toArray());
    }, (p_253493_) -> {
        return Arrays.stream(p_253493_.toLongArray());
    });
    private static final Codec<Property> PROPERTY = RecordCodecBuilder.create((p_253491_) -> {
        return p_253491_.group(Codec.STRING.fieldOf("name").forGetter(Property::getName), Codec.STRING.fieldOf("value").forGetter(Property::getValue), Codec.STRING.optionalFieldOf("signature").forGetter((p_253490_) -> {
            return Optional.ofNullable(p_253490_.getSignature());
        })).apply(p_253491_, (p_253494_, p_253495_, p_253496_) -> {
            return new Property(p_253494_, p_253495_, p_253496_.orElse((String)null));
        });
    });
    @VisibleForTesting
    public static final Codec<PropertyMap> PROPERTY_MAP = Codec.either(Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()), PROPERTY.listOf()).xmap((p_253515_) -> {
        PropertyMap propertymap = new PropertyMap();
        p_253515_.ifLeft((p_253506_) -> {
            p_253506_.forEach((p_253500_, p_253501_) -> {
                for(String s : p_253501_) {
                    propertymap.put(p_253500_, new Property(p_253500_, s));
                }

            });
        }).ifRight((p_253509_) -> {
            for(Property property : p_253509_) {
                propertymap.put(property.getName(), property);
            }

        });
        return propertymap;
    }, (p_253504_) -> {
        return Either.right(new ArrayList<>(p_253504_.values()));
    });

    public static <F, S> Codec<Either<F, S>> xor(Codec<F> pFirst, Codec<S> pSecond) {
        return new ExtraCodecs.XorCodec<>(pFirst, pSecond);
    }

    public static <A> Codec.ResultFunction<A> orElsePartial(final A p_184382_) {
        return new Codec.ResultFunction<A>() {
            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> p_184466_, T p_184467_, DataResult<Pair<A, T>> p_184468_) {
                MutableObject<String> mutableobject = new MutableObject<>();
                Optional<Pair<A, T>> optional = p_184468_.resultOrPartial(mutableobject::setValue);
                return optional.isPresent() ? p_184468_ : DataResult.error("(" + (String)mutableobject.getValue() + " -> using default)", Pair.of(p_184382_, p_184467_));
            }

            public <T> DataResult<T> coApply(DynamicOps<T> p_184470_, A p_184471_, DataResult<T> p_184472_) {
                return p_184472_;
            }

            public String toString() {
                return "OrElsePartial[" + p_184382_ + "]";
            }
        };
    }

    public static <E> Codec<E> idResolverCodec(ToIntFunction<E> p_184422_, IntFunction<E> p_184423_, int p_184424_) {
        return Codec.INT.flatXmap((p_184414_) -> {
            return Optional.ofNullable(p_184423_.apply(p_184414_)).map(DataResult::success).orElseGet(() -> {
                return DataResult.error("Unknown element id: " + p_184414_);
            });
        }, (p_274850_) -> {
            int i = p_184422_.applyAsInt(p_274850_);
            return i == p_184424_ ? DataResult.error("Element with unknown id: " + p_274850_) : DataResult.success(i);
        });
    }

    public static <E> Codec<E> stringResolverCodec(Function<E, String> p_184406_, Function<String, E> p_184407_) {
        return Codec.STRING.flatXmap((p_184404_) -> {
            return Optional.ofNullable(p_184407_.apply(p_184404_)).map(DataResult::success).orElseGet(() -> {
                return DataResult.error("Unknown element name:" + p_184404_);
            });
        }, (p_184401_) -> {
            return Optional.ofNullable(p_184406_.apply(p_184401_)).map(DataResult::success).orElseGet(() -> {
                return DataResult.error("Element with unknown name: " + p_184401_);
            });
        });
    }

    public static <E> Codec<E> orCompressed(final Codec<E> pFirst, final Codec<E> pSecond) {
        return new Codec<E>() {
            public <T> DataResult<T> encode(E p_184483_, DynamicOps<T> p_184484_, T p_184485_) {
                return p_184484_.compressMaps() ? pSecond.encode(p_184483_, p_184484_, p_184485_) : pFirst.encode(p_184483_, p_184484_, p_184485_);
            }

            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> p_184480_, T p_184481_) {
                return p_184480_.compressMaps() ? pSecond.decode(p_184480_, p_184481_) : pFirst.decode(p_184480_, p_184481_);
            }

            public String toString() {
                return pFirst + " orCompressed " + pSecond;
            }
        };
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> pCodec, final Function<E, Lifecycle> p_184370_, final Function<E, Lifecycle> p_184371_) {
        return pCodec.mapResult(new Codec.ResultFunction<E>() {
            public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> p_184497_, T p_184498_, DataResult<Pair<E, T>> p_184499_) {
                return p_184499_.result().map((p_184495_) -> {
                    return p_184499_.setLifecycle(p_184370_.apply(p_184495_.getFirst()));
                }).orElse(p_184499_);
            }

            public <T> DataResult<T> coApply(DynamicOps<T> p_184501_, E p_184502_, DataResult<T> p_184503_) {
                return p_184503_.setLifecycle(p_184371_.apply(p_184502_));
            }

            public String toString() {
                return "WithLifecycle[" + p_184370_ + " " + p_184371_ + "]";
            }
        });
    }

    public static <T> Codec<T> validate(Codec<T> pCodec, Function<T, DataResult<T>> pValidator) {
        return pCodec.flatXmap(pValidator, pValidator);
    }

    public static <T> MapCodec<T> validate(MapCodec<T> pCodec, Function<T, DataResult<T>> pValidator) {
        return pCodec.flatXmap(pValidator, pValidator);
    }

    private static Codec<Integer> intRangeWithMessage(int pMin, int pMax, Function<Integer, String> pErrorMessage) {
        return validate(Codec.INT, (p_274889_) -> {
            return p_274889_.compareTo(pMin) >= 0 && p_274889_.compareTo(pMax) <= 0 ? DataResult.success(p_274889_) : DataResult.error(pErrorMessage.apply(p_274889_));
        });
    }

    public static Codec<Integer> intRange(int pMin, int pMax) {
        return intRangeWithMessage(pMin, pMax, (p_269784_) -> {
            return "Value must be within range [" + pMin + ";" + pMax + "]: " + p_269784_;
        });
    }

    private static Codec<Float> floatRangeMinExclusiveWithMessage(float pMin, float pMax, Function<Float, String> pErrorMessage) {
        return validate(Codec.FLOAT, (p_274865_) -> {
            return p_274865_.compareTo(pMin) > 0 && p_274865_.compareTo(pMax) <= 0 ? DataResult.success(p_274865_) : DataResult.error(pErrorMessage.apply(p_274865_));
        });
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> pCodec) {
        return validate(pCodec, (p_274853_) -> {
            return p_274853_.isEmpty() ? DataResult.error("List must have contents") : DataResult.success(p_274853_);
        });
    }

    public static <E> MapCodec<E> retrieveContext(final Function<DynamicOps<?>, DataResult<E>> p_203977_) {
        class ContextRetrievalCodec extends MapCodec<E> {
            public <T> RecordBuilder<T> encode(E p_203993_, DynamicOps<T> p_203994_, RecordBuilder<T> p_203995_) {
                return p_203995_;
            }

            public <T> DataResult<E> decode(DynamicOps<T> p_203990_, MapLike<T> p_203991_) {
                return p_203977_.apply(p_203990_);
            }

            public String toString() {
                return "ContextRetrievalCodec[" + p_203977_ + "]";
            }

            public <T> Stream<T> keys(DynamicOps<T> p_203997_) {
                return Stream.empty();
            }
        }

        return new ContextRetrievalCodec();
    }

    public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> ensureHomogenous(Function<E, T> p_203985_) {
        return (p_203980_) -> {
            Iterator<E> iterator = p_203980_.iterator();
            if (iterator.hasNext()) {
                T t = p_203985_.apply(iterator.next());

                while(iterator.hasNext()) {
                    E e = iterator.next();
                    T t1 = p_203985_.apply(e);
                    if (t1 != t) {
                        return DataResult.error("Mixed type list: element " + e + " had type " + t1 + ", but list is of type " + t);
                    }
                }
            }

            return DataResult.success(p_203980_, Lifecycle.stable());
        };
    }

    public static <A> Codec<A> catchDecoderException(final Codec<A> pCodec) {
        return Codec.of(pCodec, new Decoder<A>() {
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> p_216193_, T p_216194_) {
                try {
                    return pCodec.decode(p_216193_, p_216194_);
                } catch (Exception exception) {
                    return DataResult.error("Caught exception decoding " + p_216194_ + ": " + exception.getMessage());
                }
            }
        });
    }

    public static Codec<Instant> instantCodec(DateTimeFormatter pDateTimeFormatter) {
        return Codec.STRING.comapFlatMap((p_274881_) -> {
            try {
                return DataResult.success(Instant.from(pDateTimeFormatter.parse(p_274881_)));
            } catch (Exception exception) {
                return DataResult.error(exception.getMessage());
            }
        }, pDateTimeFormatter::format);
    }

    public static MapCodec<OptionalLong> asOptionalLong(MapCodec<Optional<Long>> pCodec) {
        return pCodec.xmap(toOptionalLong, fromOptionalLong);
    }

    private static DataResult<GameProfile> mapIdNameToGameProfile(Pair<Optional<UUID>, Optional<String>> p_253764_) {
        try {
            return DataResult.success(new GameProfile(p_253764_.getFirst().orElse((UUID)null), p_253764_.getSecond().orElse((String)null)));
        } catch (Throwable throwable) {
            return DataResult.error(throwable.getMessage());
        }
    }

    private static DataResult<Pair<Optional<UUID>, Optional<String>>> mapGameProfileToIdName(GameProfile p_254220_) {
        return DataResult.success(Pair.of(Optional.ofNullable(p_254220_.getId()), Optional.ofNullable(p_254220_.getName())));
    }

    public static Codec<String> sizeLimitedString(int pMinSize, int pMaxSize) {
        return validate(Codec.STRING, (p_274879_) -> {
            int i = p_274879_.length();
            if (i < pMinSize) {
                return DataResult.error("String \"" + p_274879_ + "\" is too short: " + i + ", expected range [" + pMinSize + "-" + pMaxSize + "]");
            } else {
                return i > pMaxSize ? DataResult.error("String \"" + p_274879_ + "\" is too long: " + i + ", expected range [" + pMinSize + "-" + pMaxSize + "]") : DataResult.success(p_274879_);
            }
        });
    }

    public static final class EitherCodec<F, S> implements Codec<Either<F, S>> {
        private final Codec<F> first;
        private final Codec<S> second;

        public EitherCodec(Codec<F> pFirst, Codec<S> pSecond) {
            this.first = pFirst;
            this.second = pSecond;
        }

        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> pOps, T pInput) {
            DataResult<Pair<Either<F, S>, T>> dataresult = this.first.decode(pOps, pInput).map((p_184524_) -> {
                return p_184524_.mapFirst(Either::left);
            });
            if (!dataresult.error().isPresent()) {
                return dataresult;
            } else {
                DataResult<Pair<Either<F, S>, T>> dataresult1 = this.second.decode(pOps, pInput).map((p_184515_) -> {
                    return p_184515_.mapFirst(Either::right);
                });
                return !dataresult1.error().isPresent() ? dataresult1 : dataresult.apply2((p_184517_, p_184518_) -> {
                    return p_184518_;
                }, dataresult1);
            }
        }

        public <T> DataResult<T> encode(Either<F, S> pInput, DynamicOps<T> pOps, T pPrefix) {
            return pInput.map((p_184528_) -> {
                return this.first.encode(p_184528_, pOps, pPrefix);
            }, (p_184522_) -> {
                return this.second.encode(p_184522_, pOps, pPrefix);
            });
        }

        public boolean equals(Object pOther) {
            if (this == pOther) {
                return true;
            } else if (pOther != null && this.getClass() == pOther.getClass()) {
                ExtraCodecs.EitherCodec<?, ?> eithercodec = (ExtraCodecs.EitherCodec)pOther;
                return Objects.equals(this.first, eithercodec.first) && Objects.equals(this.second, eithercodec.second);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(this.first, this.second);
        }

        public String toString() {
            return "EitherCodec[" + this.first + ", " + this.second + "]";
        }
    }

    static class LazyInitializedCodec<A> implements Codec<A> {
        Supplier<Codec<A>> delegate;
        LazyInitializedCodec()
        {
            delegate = Suppliers.memoize(delegate::get);
        }

        public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> pOps, T pInput) {
            return this.delegate.get().decode(pOps, pInput);
        }

        public <T> DataResult<T> encode(A pInput, DynamicOps<T> pOps, T pPrefix) {
            return this.delegate.get().encode(pInput, pOps, pPrefix);
        }
    }

    public static class TagOrElementLocation {
        ResourceLocation id;
        boolean tag;

        public String toString() {
            return this.decoratedId();
        }

        private String decoratedId() {
            return this.tag ? "#" + this.id : this.id.toString();
        }
    }

    static final class XorCodec<F, S> implements Codec<Either<F, S>> {
        private final Codec<F> first;
        private final Codec<S> second;

        public XorCodec(Codec<F> pFirst, Codec<S> pSecond) {
            this.first = pFirst;
            this.second = pSecond;
        }

        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> pOps, T pInput) {
            DataResult<Pair<Either<F, S>, T>> dataresult = this.first.decode(pOps, pInput).map((p_144673_) -> {
                return p_144673_.mapFirst(Either::left);
            });
            DataResult<Pair<Either<F, S>, T>> dataresult1 = this.second.decode(pOps, pInput).map((p_144667_) -> {
                return p_144667_.mapFirst(Either::right);
            });
            Optional<Pair<Either<F, S>, T>> optional = dataresult.result();
            Optional<Pair<Either<F, S>, T>> optional1 = dataresult1.result();
            if (optional.isPresent() && optional1.isPresent()) {
                return DataResult.error("Both alternatives read successfully, can not pick the correct one; first: \" + optional.get() + \" second: \" + optional1.get()");
            } else {
                return optional.isPresent() ? dataresult : dataresult1;
            }
        }

        public <T> DataResult<T> encode(Either<F, S> pInput, DynamicOps<T> pOps, T pPrefix) {
            return pInput.map((p_144677_) -> {
                return this.first.encode(p_144677_, pOps, pPrefix);
            }, (p_144671_) -> {
                return this.second.encode(p_144671_, pOps, pPrefix);
            });
        }

        public boolean equals(Object pOther) {
            if (this == pOther) {
                return true;
            } else if (pOther != null && this.getClass() == pOther.getClass()) {
                ExtraCodecs.XorCodec<?, ?> xorcodec = (ExtraCodecs.XorCodec)pOther;
                return Objects.equals(this.first, xorcodec.first) && Objects.equals(this.second, xorcodec.second);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(this.first, this.second);
        }

        public String toString() {
            return "XorCodec[" + this.first + ", " + this.second + "]";
        }
    }
}
