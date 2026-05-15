package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public record ValueGetter<T>(Type type, String path, Function<Map<String, Object>, T> getter)
{
    public T get(Map<String, Object> sources)
    {   return getter.apply(sources);
    }

    public Set<String> parameters()
    {
        return switch (type)
        {
            case CONSTANT -> Set.of();
            case GETTER   -> Set.of(path.split(":", 2)[0].trim());
            case EXPRESSION -> extractSourceNames(path);
        };
    }

    public boolean hasParameter(String name)
    {   return parameters().contains(name);
    }

    private static Set<String> extractSourceNames(String expression)
    {
        if (expression == null) return Set.of();
        Set<String> names = new HashSet<>();
        int i = 0;
        while (i < expression.length())
        {
            if (expression.charAt(i) == '{')
            {
                int end = expression.indexOf('}', i);
                if (end == -1) break;
                String[] parts = expression.substring(i + 1, end).split(":", 2);
                if (parts.length == 2) names.add(parts[0].trim());
                i = end + 1;
            }
            else i++;
        }
        return names;
    }

    public static <T> ValueGetter<T> constant(T value)
    {   return new ValueGetter<>(Type.CONSTANT, null, sources -> value);
    }

    public static <T> ValueGetter<T> of(Function<Map<String, Object>, T> getter)
    {   return new ValueGetter<>(Type.EXPRESSION, "custom", getter);
    }

    public static <T> ValueGetter<T> parse(Supplier<Object> value, Codec<T> codec, T defaultValue)
    {
        try
        {
            Object val = value.get();
            if (val instanceof String s)
            {
                ValueGetter<T> getter = parseString(s, codec, defaultValue);
                if (getter != null) return getter;
            }
            if (val instanceof Number n)
            {
                // Decode through the codec so widening conversions happen (e.g. TOML Integer → Double).
                T decoded = recode(codec, n).orElse(defaultValue);
                return new ValueGetter<>(Type.CONSTANT, null, sources -> decoded);
            }
            return new ValueGetter<>(Type.CONSTANT, null, sources -> (T) val);
        }
        catch (Exception e)
        {   return new ValueGetter<>(Type.CONSTANT, null, sources -> defaultValue);
        }
    }

    public static <T> ValueGetter<Optional<T>> parseOptional(Object value, Codec<T> codec)
    {
        if (value instanceof String s)
        {
            ValueGetter<Optional<T>> getter = parseStringOptional(s, codec);
            if (getter != null) return getter;
        }
        return new ValueGetter<>(Type.CONSTANT, null, sources -> Optional.of((T) value));
    }

    public static <T> Codec<ValueGetter<T>> codec(Codec<T> valueCodec, T defaultValue)
    {
        return optionalCodec(valueCodec).xmap(
                optGetter -> new ValueGetter<>(optGetter.type, optGetter.path, sources -> optGetter.get(sources).orElse(defaultValue)),
                getter -> new ValueGetter<>(getter.type, getter.path, sources -> Optional.ofNullable(getter.get(sources)).or(() -> Optional.of(defaultValue)))
        );
    }

    public static <T> Codec<ValueGetter<Optional<T>>> optionalCodec(Codec<T> valueCodec)
    {
        return new Codec<>()
        {
            @Override
            public <A> DataResult<Pair<ValueGetter<Optional<T>>, A>> decode(DynamicOps<A> ops, A input)
            {
                Optional<String> stringOpt = ops.getStringValue(input).result();
                if (stringOpt.isPresent() && stringOpt.get().contains("{"))
                {
                    String s = stringOpt.get();
                    ValueGetter<Optional<T>> getter = parseStringOptional(s, valueCodec);
                    if (getter != null) return DataResult.success(Pair.of(getter, input));
                    return DataResult.error(() -> "Invalid ValueGetter: " + s);
                }
                return valueCodec.decode(ops, input)
                        .map(v -> Pair.of(new ValueGetter<>(Type.CONSTANT, null, sources -> Optional.of(v.getFirst())), input));
            }

            @Override
            public <A> DataResult<A> encode(ValueGetter<Optional<T>> getter, DynamicOps<A> ops, A prefix)
            {
                Type type = getter.type;
                return switch (type)
                {
                    case CONSTANT   -> getter.get(Map.of()).map(v -> valueCodec.encode(v, ops, prefix))
                                       .orElseGet(() -> DataResult.error(() -> "Empty constant ValueGetter"));
                    case GETTER     -> ops.mergeToPrimitive(prefix, ops.createString("{" + getter.path + "}"));
                    case EXPRESSION -> ops.mergeToPrimitive(prefix, ops.createString(getter.path));
                };
            }
        };
    }

    public static <T> MapCodec<ValueGetter<T>> fieldCodec(String field, Codec<T> valueCodec, T defaultValue)
    {   return codec(valueCodec, defaultValue).fieldOf(field);
    }
    public static <T> MapCodec<ValueGetter<T>> optionalFieldCodec(String field, Codec<T> valueCodec, T defaultValue)
    {   return codec(valueCodec, defaultValue).optionalFieldOf(field, ValueGetter.constant(defaultValue));
    }

    private static <T> Optional<T> recode(Codec<T> codec, Number val)
    {   return codec.decode(NbtOps.INSTANCE, NbtOps.INSTANCE.createNumeric(val)).result().map(Pair::getFirst);
    }

    private static <T> ValueGetter<T> parseString(String s, Codec<T> codec, T defaultValue)
    {
        if (!s.contains("{")) return null;
        if (isSimpleGetter(s))
        {
            ValueGetter<Optional<T>> opt = simpleGetter(s, codec);
            if (opt == null) return null;
            return new ValueGetter<>(opt.type, opt.path, sources -> opt.get(sources).orElse(defaultValue));
        }
        ValueGetter<T> formula = formulaGetter(s);
        return formula != null ? formula : new ValueGetter<>(Type.CONSTANT, null, sources -> defaultValue);
    }

    private static <T> ValueGetter<Optional<T>> parseStringOptional(String s, Codec<T> codec)
    {
        if (!s.contains("{")) return null;
        if (isSimpleGetter(s)) return simpleGetter(s, codec);
        ValueGetter<T> formula = formulaGetter(s);
        if (formula == null) return null;
        return new ValueGetter<>(formula.type, formula.path, sources -> Optional.of(formula.get(sources)));
    }

    private static <T> ValueGetter<Optional<T>> simpleGetter(String s, Codec<T> codec)
    {
        String inner = s.substring(1, s.length() - 1);
        String[] parts = inner.split(":", 2);
        if (parts.length != 2) return null;
        String sourceName = parts[0].trim();
        String varPath    = parts[1].trim();
        return new ValueGetter<>(Type.GETTER, inner, sources ->
        {
            Object source = sources.get(sourceName);
            if (source instanceof BlockState state)
            {
                StateDefinition<Block, BlockState> stateDefinition = state.getBlock().getStateDefinition();
                Property prop = stateDefinition.getProperty(varPath);
                if (prop == null) return Optional.empty();
                Object value = state.getValue(prop);
                if (value instanceof Number n)
                {   value = recode(codec, n).orElse(null);
                }
                try
                {
                    T castValue = (T) value;
                    return Optional.ofNullable(castValue);
                }
                catch (ClassCastException e)
                {   return Optional.empty();
                }
            }
            CompoundTag nbt = getSourceNbt(source);
            if (nbt == null) return Optional.empty();
            return codec.decode(NbtOps.INSTANCE, findTag(varPath, nbt)).result().map(Pair::getFirst);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> ValueGetter<T> formulaGetter(String s)
    {
        try
        {
            Expression expr = new ExpressionParser(s).parse();
            return (ValueGetter<T>) (Object) new ValueGetter<Double>(Type.EXPRESSION, s, expr::evaluate);
        }
        catch (IllegalArgumentException ex)
        {
            ColdSweat.LOGGER.error("Invalid ValueGetter formula '{}': {}", s, ex.getMessage());
            return null;
        }
    }

    private static boolean isSimpleGetter(String s)
    {   return s.startsWith("{") && s.endsWith("}") && s.indexOf('{', 1) == -1;
    }

    private static CompoundTag getSourceNbt(Object source)
    {
        if (source instanceof Entity e)       return EntityHelper.getFullData(e);
        if (source instanceof ItemStack i)    return i.getOrCreateTag();
        if (source instanceof BlockEntity be) return be.getPersistentData();
        return null;
    }

    private static Tag findTag(String path, CompoundTag tag)
    {
        if (tag == null) return null;
        Tag current = tag;
        for (String part : path.split("\\."))
        {
            if (!(current instanceof CompoundTag compound) || !compound.contains(part)) return null;
            current = compound.get(part);
        }
        return current;
    }

    private static double tagToDouble(Tag tag)
    {
        if (tag == null) return 0.0;
        if (tag instanceof NumericTag num) return num.getAsDouble();
        try { return Double.parseDouble(tag.getAsString()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    @FunctionalInterface
    private interface Expression
    {
        double evaluate(Map<String, Object> sources);

        static Expression binary(Expression left, Expression right, char op)
        {
            return switch (op)
            {
                case '+' -> sources -> left.evaluate(sources) + right.evaluate(sources);
                case '-' -> sources -> left.evaluate(sources) - right.evaluate(sources);
                case '*' -> sources -> left.evaluate(sources) * right.evaluate(sources);
                case '/' -> sources -> left.evaluate(sources) / right.evaluate(sources);
                case '%' -> sources -> left.evaluate(sources) % right.evaluate(sources);
                case '^' -> sources -> Math.pow(left.evaluate(sources), right.evaluate(sources));
                default  -> throw new IllegalArgumentException("Unknown operator: " + op);
            };
        }
    }

    private static class ExpressionParser
    {
        private final String s;
        private int pos;

        ExpressionParser(String s) { this.s = s; }

        Expression parse()
        {
            Expression expr = parseAddSub();
            skipWS();
            if (pos != s.length()) throw new IllegalArgumentException("Unexpected '" + s.charAt(pos) + "' at position " + pos);
            return expr;
        }

        private void skipWS()
        {   while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        private Expression parseAddSub()
        {
            Expression left = parseMulDivMod();
            while (true)
            {   skipWS();
                if (pos >= s.length()) break;
                char op = s.charAt(pos);
                if (op != '+' && op != '-') break;
                pos++;
                left = Expression.binary(left, parseMulDivMod(), op);
            }
            return left;
        }

        private Expression parseMulDivMod()
        {
            Expression left = parsePower();
            while (true)
            {   skipWS();
                if (pos >= s.length()) break;
                char op = s.charAt(pos);
                if (op != '*' && op != '/' && op != '%') break;
                pos++;
                left = Expression.binary(left, parsePower(), op);
            }
            return left;
        }

        private Expression parsePower()
        {
            Expression base = parseUnary();
            skipWS();
            if (pos < s.length() && s.charAt(pos) == '^')
            {   pos++;
                return Expression.binary(base, parsePower(), '^');
            }
            return base;
        }

        private Expression parseUnary()
        {
            skipWS();
            if (pos < s.length() && s.charAt(pos) == '-')
            {   pos++;
                Expression inner = parsePrimary();
                return sources -> -inner.evaluate(sources);
            }
            return parsePrimary();
        }

        private Expression parsePrimary()
        {
            skipWS();
            if (pos >= s.length()) throw new IllegalArgumentException("Unexpected end of expression");
            char c = s.charAt(pos);

            if (c == '{')
            {
                int end = s.indexOf('}', pos);
                if (end == -1) throw new IllegalArgumentException("Unmatched '{' at position " + pos);
                String[] parts = s.substring(pos + 1, end).split(":", 2);
                if (parts.length != 2) throw new IllegalArgumentException("Invalid getter: " + s.substring(pos, end + 1));
                String sourceName = parts[0].trim();
                String nbtPath    = parts[1].trim();
                pos = end + 1;
                return sources -> tagToDouble(findTag(nbtPath, getSourceNbt(sources.get(sourceName))));
            }

            if (Character.isDigit(c) || c == '.')
            {
                int start = pos;
                while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) pos++;
                double value = Double.parseDouble(s.substring(start, pos));
                return sources -> value;
            }

            if (c == '(')
            {
                pos++;
                Expression inner = parseAddSub();
                skipWS();
                if (pos >= s.length() || s.charAt(pos) != ')')
                    throw new IllegalArgumentException("Expected closing ')'");
                pos++;
                return inner;
            }

            throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + pos);
        }
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other) return true;
        if (!(other instanceof ValueGetter<?> that)) return false;
        if (this.type != that.type) return false;
        if (this.type == Type.CONSTANT)
            return Objects.equals(this.get(Map.of()), that.get(Map.of()));
        return Objects.equals(this.path, that.path);
    }

    public enum Type
    {
        CONSTANT("constant"),
        GETTER("getter"),
        EXPRESSION("expression");

        private final String id;
        Type(String id) { this.id = id; }

        public static Type fromString(String string)
        {
            for (Type type : values())
            {   if (type.id.equalsIgnoreCase(string)) return type;
            }
            throw new IllegalArgumentException("Invalid ValueGetter type: " + string);
        }
    }
}
