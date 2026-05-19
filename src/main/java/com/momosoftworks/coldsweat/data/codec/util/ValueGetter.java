package com.momosoftworks.coldsweat.data.codec.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ValueGetter<T>
{
    private final Type type;
    private final String path;
    private final Function<Map<String, Object>, T> getter;

    public ValueGetter(Type type, String path, Function<Map<String, Object>, T> getter)
    {
        this.type = type;
        this.path = path;
        this.getter = getter;
    }

    public Type type()
    {   return type;
    }
    public String path()
    {   return path;
    }
    public Function<Map<String, Object>, T> getter()
    {   return getter;
    }

    public T get(Map<String, Object> sources)
    {   return getter.apply(sources);
    }

    public Set<String> parameters()
    {
        switch (type)
        {
            case CONSTANT : return new HashSet<>();
            case GETTER   : return new HashSet(){{
                                add(path.split(":", 2)[0].trim());
                            }};
            case EXPRESSION : return extractSourceNames(path);
        }
        return new HashSet<>();
    }

    public boolean hasParameter(String name)
    {   return parameters().contains(name);
    }

    private static Set<String> extractSourceNames(String expression)
    {
        if (expression == null) return new HashSet<>();
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
            if (val instanceof String)
            {
                String s = (String) val;
                ValueGetter<T> getter = parseString(s, codec, defaultValue);
                if (getter != null) return getter;
            }
            if (val instanceof Number)
            {
                Number n = (Number) val;
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
        if (value instanceof String)
        {
            String s = (String) value;
            ValueGetter<Optional<T>> getter = parseStringOptional(s, codec);
            if (getter != null) return getter;
        }
        return new ValueGetter<>(Type.CONSTANT, null, sources -> Optional.of((T) value));
    }

    public static <T> Codec<ValueGetter<T>> codec(Codec<T> valueCodec, T defaultValue)
    {
        return optionalCodec(valueCodec).xmap(
                optGetter -> new ValueGetter<>(optGetter.type, optGetter.path, sources -> optGetter.get(sources).orElse(defaultValue)),
                getter -> new ValueGetter<>(getter.type, getter.path, sources -> Optional.ofNullable(CSMath.orElse(getter.get(sources), defaultValue)))
        );
    }

    public static <T> Codec<ValueGetter<Optional<T>>> optionalCodec(Codec<T> valueCodec)
    {
        return new Codec<ValueGetter<Optional<T>>>()
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
                    return DataResult.error("Invalid ValueGetter: " + s);
                }
                return valueCodec.decode(ops, input)
                        .map(v -> Pair.of(new ValueGetter<>(Type.CONSTANT, null, sources -> Optional.of(v.getFirst())), input));
            }

            @Override
            public <A> DataResult<A> encode(ValueGetter<Optional<T>> getter, DynamicOps<A> ops, A prefix)
            {
                Type type = getter.type;
                switch (type)
                {
                    case CONSTANT   : return getter.get(new HashMap<>()).map(v -> valueCodec.encode(v, ops, prefix))
                                       .orElseGet(() -> DataResult.error("Empty constant ValueGetter"));
                    case GETTER     : return ops.mergeToPrimitive(prefix, ops.createString("{" + getter.path + "}"));
                    case EXPRESSION : return ops.mergeToPrimitive(prefix, ops.createString(getter.path));
                }
                return DataResult.error("Unknown ValueGetter type: " + type);
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
    {   return codec.decode(NBTDynamicOps.INSTANCE, NBTDynamicOps.INSTANCE.createNumeric(val)).result().map(Pair::getFirst);
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
            if (source instanceof BlockState)
            {
                BlockState state = (BlockState) source;
                StateContainer<Block, BlockState> stateDefinition = state.getBlock().getStateDefinition();
                Property prop = stateDefinition.getProperty(varPath);
                if (prop == null) return Optional.empty();
                Object value = state.getValue(prop);
                if (value instanceof Number)
                {   value = recode(codec, (Number) value).orElse(null);
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
            CompoundNBT nbt = getSourceNbt(source);
            if (nbt == null) return Optional.empty();
            return codec.decode(NBTDynamicOps.INSTANCE, findTag(varPath, nbt)).result().map(Pair::getFirst);
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

    private static CompoundNBT getSourceNbt(Object source)
    {
        if (source instanceof Entity) return EntityHelper.getFullData(((Entity) source));
        if (source instanceof ItemStack) return ((ItemStack) source).getOrCreateTag();
        if (source instanceof TileEntity) return WorldHelper.getFullData((TileEntity) source);
        return null;
    }

    private static INBT findTag(String path, CompoundNBT tag)
    {
        if (tag == null) return null;
        INBT current = tag;
        for (String part : path.split("\\."))
        {
            if (!(current instanceof CompoundNBT) || !((CompoundNBT) current).contains(part)) return null;
            current = ((CompoundNBT) current).get(part);
        }
        return current;
    }

    private static double tagToDouble(INBT tag)
    {
        if (tag == null) return 0.0;
        if (tag instanceof NumberNBT) return ((NumberNBT) tag).getAsDouble();
        try { return Double.parseDouble(tag.getAsString()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    @FunctionalInterface
    private interface Expression
    {
        double evaluate(Map<String, Object> sources);

        static Expression binary(Expression left, Expression right, char op)
        {
            switch (op)
            {
                case '+' : return sources -> left.evaluate(sources) + right.evaluate(sources);
                case '-' : return sources -> left.evaluate(sources) - right.evaluate(sources);
                case '*' : return sources -> left.evaluate(sources) * right.evaluate(sources);
                case '/' : return sources -> left.evaluate(sources) / right.evaluate(sources);
                case '%' : return sources -> left.evaluate(sources) % right.evaluate(sources);
                case '^' : return sources -> Math.pow(left.evaluate(sources), right.evaluate(sources));
                default  : throw new IllegalArgumentException("Unknown operator: " + op);
            }
        }
    }

    private static class ExpressionParser
    {
        private final String s;
        private int pos;

        ExpressionParser(String s) { this.s = s; }

        ValueGetter.Expression parse()
        {
            ValueGetter.Expression expr = parseAddSub();
            skipWS();
            if (pos != s.length()) throw new IllegalArgumentException("Unexpected '" + s.charAt(pos) + "' at position " + pos);
            return expr;
        }

        private void skipWS()
        {   while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        private ValueGetter.Expression parseAddSub()
        {
            ValueGetter.Expression left = parseMulDivMod();
            while (true)
            {   skipWS();
                if (pos >= s.length()) break;
                char op = s.charAt(pos);
                if (op != '+' && op != '-') break;
                pos++;
                left = ValueGetter.Expression.binary(left, parseMulDivMod(), op);
            }
            return left;
        }

        private ValueGetter.Expression parseMulDivMod()
        {
            ValueGetter.Expression left = parsePower();
            while (true)
            {   skipWS();
                if (pos >= s.length()) break;
                char op = s.charAt(pos);
                if (op != '*' && op != '/' && op != '%') break;
                pos++;
                left = ValueGetter.Expression.binary(left, parsePower(), op);
            }
            return left;
        }

        private ValueGetter.Expression parsePower()
        {
            ValueGetter.Expression base = parseUnary();
            skipWS();
            if (pos < s.length() && s.charAt(pos) == '^')
            {   pos++;
                return ValueGetter.Expression.binary(base, parsePower(), '^');
            }
            return base;
        }

        private ValueGetter.Expression parseUnary()
        {
            skipWS();
            if (pos < s.length() && s.charAt(pos) == '-')
            {   pos++;
                ValueGetter.Expression inner = parsePrimary();
                return sources -> -inner.evaluate(sources);
            }
            return parsePrimary();
        }

        private ValueGetter.Expression parsePrimary()
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
                ValueGetter.Expression inner = parseAddSub();
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
        if (!(other instanceof ValueGetter<?>)) return false;
        ValueGetter<?> that = (ValueGetter<?>) other;
        if (this.type != that.type) return false;
        if (this.type == Type.CONSTANT)
            return Objects.equals(this.get(new HashMap<>()), that.get(new HashMap<>()));
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
