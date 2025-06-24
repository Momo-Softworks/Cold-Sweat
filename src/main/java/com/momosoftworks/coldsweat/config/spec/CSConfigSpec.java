package com.momosoftworks.coldsweat.config.spec;

import com.electronwill.nightconfig.core.*;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.neoforged.fml.Logging;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class CSConfigSpec implements IConfigSpec
{
    private final Map<List<String>, String> levelComments;
    private final Map<List<String>, String> levelTranslationKeys;
    private final UnmodifiableConfig spec;
    private final UnmodifiableConfig values;
    @Nullable
    private IConfigSpec.@Nullable ILoadedConfig loadedConfig;

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Joiner LINE_JOINER = Joiner.on("\n");
    private static final Joiner DOT_JOINER = Joiner.on(".");
    private static final Splitter DOT_SPLITTER = Splitter.on(".");

    private CSConfigSpec(UnmodifiableConfig spec, UnmodifiableConfig values, Map<List<String>, String> levelComments, Map<List<String>, String> levelTranslationKeys) {
        this.spec = spec;
        this.values = values;
        this.levelComments = levelComments;
        this.levelTranslationKeys = levelTranslationKeys;
    }

    public boolean isEmpty() {
        return this.spec.isEmpty();
    }

    public String getLevelComment(List<String> path) {
        return levelComments.get(path);
    }

    public String getLevelTranslationKey(List<String> path) {
        return levelTranslationKeys.get(path);
    }

    @Override
    public void acceptConfig(@Nullable IConfigSpec.@Nullable ILoadedConfig config) {
        this.loadedConfig = config;
        if (config != null && !isCorrect(config.config())) {
            LOGGER.warn(Logging.CORE, "Configuration {} is not correct. Correcting", config);
            correct(config.config(),
                    (action, path, incorrectValue, correctedValue) ->
                            LOGGER.warn(Logging.CORE, "Incorrect key {} was corrected from {} to its default, {}. {}", DOT_JOINER.join(path), incorrectValue, correctedValue, incorrectValue == correctedValue ? "This seems to be an error." : ""),
                    (action, path, incorrectValue, correctedValue) ->
                            LOGGER.debug(Logging.CORE, "The comment on key {} does not match the spec. This may create a backup.", DOT_JOINER.join(path)));
            config.save();
        }
        this.afterReload();
    }

    public void validateSpec(ModConfig config) {
        this.forEachValue(getValues().valueMap().values(), configValue -> {
            if (!configValue.getSpec().restartType().isValid(config.getType())) {
                throw new IllegalArgumentException("Configuration value " + String.join(".", configValue.getPath()) +
                                                           " defined in config " + config.getFileName() +
                                                           " has restart of type " + configValue.getSpec().restartType() +
                                                           " which cannot be used for configs of type " + config.getType());
            }
        });
    }

    public boolean isLoaded() {
        return loadedConfig != null;
    }

    public UnmodifiableConfig getSpec() {
        return this.spec;
    }

    public UnmodifiableConfig getValues() {
        return this.values;
    }

    private void forEachValue(Iterable<Object> configValues, Consumer<ConfigValue<?>> consumer) {
        configValues.forEach(value -> {
            if (value instanceof ConfigValue<?> configValue) {
                consumer.accept(configValue);
            } else if (value instanceof Config innerConfig) {
                this.forEachValue(innerConfig.valueMap().values(), consumer);
            }
        });
    }

    public void afterReload() {
        this.resetCaches(RestartType.NONE);
    }

    public void resetCaches(RestartType restartType) {
        this.forEachValue(getValues().valueMap().values(), configValue -> {
            if (configValue.getSpec().restartType == restartType) {
                configValue.clearCache();
            }
        });
    }

    public void save() {
        Preconditions.checkNotNull(loadedConfig, "Cannot save config value without assigned Config object present");
        loadedConfig.save();
    }

    public boolean isCorrect(UnmodifiableCommentedConfig config) {
        LinkedList<String> parentPath = new LinkedList<>();
        return correct(this.spec, config, parentPath, Collections.unmodifiableList(parentPath), (a, b, c, d) -> {}, null, true) == 0;
    }

    public void correct(CommentedConfig config) {
        correct(config, (action, path, incorrectValue, correctedValue) -> {}, null);
    }

    public int correct(CommentedConfig config, ConfigSpec.CorrectionListener listener) {
        return correct(config, listener, null);
    }

    public int correct(CommentedConfig config, ConfigSpec.CorrectionListener listener, @Nullable ConfigSpec.@Nullable CorrectionListener commentListener) {
        LinkedList<String> parentPath = new LinkedList<>();
        return correct(this.spec, config, parentPath, Collections.unmodifiableList(parentPath), listener, commentListener, false);
    }

    private int correct(UnmodifiableConfig spec, UnmodifiableCommentedConfig config, LinkedList<String> parentPath, List<String> parentPathUnmodifiable, ConfigSpec.CorrectionListener listener, @Nullable ConfigSpec.@Nullable CorrectionListener commentListener, boolean dryRun) {
        int count = 0;
        Map<String, Object> specMap = spec.valueMap();
        Map<String, Object> configMap = config.valueMap();

        for (Map.Entry<String, Object> specEntry : specMap.entrySet()) {
            final String key = specEntry.getKey();
            final Object specValue = specEntry.getValue();
            final Object configValue = configMap.get(key);
            final ConfigSpec.CorrectionAction action = configValue == null ? ConfigSpec.CorrectionAction.ADD : ConfigSpec.CorrectionAction.REPLACE;

            parentPath.addLast(key);

            if (specValue instanceof Config) {
                if (configValue instanceof CommentedConfig) {
                    count += correct((Config)specValue, (CommentedConfig)configValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                    if (count > 0 && dryRun) {
                        return count;
                    }
                } else {
                    if (dryRun) {
                        return 1;
                    }

                    CommentedConfig newValue = ((CommentedConfig)config).createSubConfig();
                    configMap.put(key, newValue);
                    listener.onCorrect(action, parentPathUnmodifiable, configValue, newValue);
                    count++;
                    count += correct((Config)specValue, newValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                }

                String newComment = levelComments.get(parentPath);
                String oldComment = config.getComment(key);

                // CUSTOM: Allow drill_down comment formatting changes
                if (!isCommentFormattingChange(oldComment, newComment)) {
                    if (!stringsMatchIgnoringNewlines(oldComment, newComment)) {
                        if (commentListener != null)
                            commentListener.onCorrect(action, parentPathUnmodifiable, oldComment, newComment);

                        if (dryRun)
                            return 1;

                        ((CommentedConfig)config).setComment(key, newComment);
                    }
                }
            } else {
                ValueSpec valueSpec = (ValueSpec)specValue;

                // CUSTOM: Use formatting-aware value testing
                if (!isValueCorrectWithFormatting(valueSpec, configValue)) {
                    if (dryRun) {
                        return 1;
                    }

                    Object newValue = valueSpec.correct(configValue);
                    configMap.put(key, newValue);
                    listener.onCorrect(action, parentPathUnmodifiable, configValue, newValue);
                    count++;
                }

                String oldComment = config.getComment(key);
                String expectedComment = valueSpec.getComment();

                // CUSTOM: Allow drill_down comment formatting changes
                if (!isCommentFormattingChange(oldComment, expectedComment)) {
                    if (!stringsMatchIgnoringNewlines(oldComment, expectedComment)) {
                        if (commentListener != null)
                            commentListener.onCorrect(action, parentPathUnmodifiable, oldComment, expectedComment);

                        if (dryRun)
                            return 1;

                        ((CommentedConfig)config).setComment(key, expectedComment);
                    }
                }
            }

            parentPath.removeLast();
        }

        // Second step: removes the unspecified values
        Iterator<Map.Entry<String, Object>> ittr = configMap.entrySet().iterator();
        while (ittr.hasNext()) {
            Map.Entry<String, Object> entry = ittr.next();
            if (!specMap.containsKey(entry.getKey())) {
                if (dryRun) {
                    return 1;
                }

                ittr.remove();
                parentPath.addLast(entry.getKey());
                listener.onCorrect(ConfigSpec.CorrectionAction.REMOVE, parentPathUnmodifiable, entry.getValue(), null);
                parentPath.removeLast();
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if a comment change is just formatting (removing //drill_down or similar)
     * Returns true if this is a formatting change that should be allowed
     */
    private boolean isCommentFormattingChange(String oldComment, String newComment) {
        // Normalize both comments by removing drill_down directives
        String normalizedOld = normalizeComment(oldComment);
        String normalizedNew = normalizeComment(newComment);

        // If they're the same after normalization, this is just a formatting change
        return stringsMatchIgnoringNewlines(normalizedOld, normalizedNew);
    }

    /**
     * Normalizes a comment by removing drill_down directives and extra whitespace
     */
    private String normalizeComment(String comment) {
        if (comment == null) {
            return "";
        }

        // Remove //drill_down directive and clean up
        String normalized = comment.replace("//drill_down", "").trim();

        // If the comment becomes empty or just a #, return empty string
        if (normalized.equals("#") || normalized.isEmpty()) {
            return "";
        }

        return normalized;
    }

    /**
     * Enhanced value testing that considers formatting equivalence
     */
    private boolean isValueCorrectWithFormatting(ValueSpec valueSpec, Object configValue) {
        // First, use the standard test
        if (valueSpec.test(configValue)) {
            return true;
        }

        // If standard test fails, check if it's a formatting difference
        if (configValue instanceof List && isListFormattingEquivalent(valueSpec, (List<?>) configValue)) {
            return true;
        }

        // Add other formatting equivalence checks as needed
        return false;
    }

    /**
     * Checks if a list value is semantically equivalent despite formatting differences
     */
    private boolean isListFormattingEquivalent(ValueSpec valueSpec, List<?> configValue) {
        try {
            // Get the expected value after correction
            Object correctedValue = valueSpec.correct(configValue);

            // If correction doesn't change the value semantically, it's just formatting
            if (correctedValue instanceof List) {
                List<?> correctedList = (List<?>) correctedValue;

                // Compare sizes first
                if (configValue.size() != correctedList.size()) {
                    return false;
                }

                // Compare elements (this handles both single-line and multi-line arrays)
                for (int i = 0; i < configValue.size(); i++) {
                    Object configElement = configValue.get(i);
                    Object correctedElement = correctedList.get(i);

                    if (!Objects.equals(configElement, correctedElement)) {
                        return false;
                    }
                }

                return true; // All elements match, this is just formatting
            }
        } catch (Exception e) {
            // If anything goes wrong, fall back to standard behavior
        }

        return false;
    }

    /**
     * Enhanced string matching that's more lenient about whitespace and formatting
     */
    private boolean stringsMatchIgnoringNewlines(String str1, String str2) {
        if (str1 == str2) return true;
        if (str1 == null || str2 == null) return false;

        // Normalize whitespace and newlines
        String normalized1 = str1.replaceAll("\\s+", " ").trim();
        String normalized2 = str2.replaceAll("\\s+", " ").trim();

        return normalized1.equals(normalized2);
    }

    private static List<String> split(String path) {
        return Lists.newArrayList(DOT_SPLITTER.split(path));
    }

    public static class Builder {
        private final Config spec = Config.of(LinkedHashMap::new, InMemoryFormat.withUniversalSupport());
        private BuilderContext context = new BuilderContext();
        private final Map<List<String>, String> levelComments = new HashMap<>();
        private final Map<List<String>, String> levelTranslationKeys = new HashMap<>();
        private final List<String> currentPath = new ArrayList<>();
        private final List<ConfigValue<?>> values = new ArrayList<>();

        // Object
        public <T> ConfigValue<T> define(String path, T defaultValue) {
            return define(split(path), defaultValue);
        }
        public <T> ConfigValue<T> define(List<String> path, T defaultValue) {
            return define(path, defaultValue, o -> o != null && defaultValue.getClass().isAssignableFrom(o.getClass()));
        }
        public <T> ConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator) {
            return define(split(path), defaultValue, validator);
        }
        public <T> ConfigValue<T> define(List<String> path, T defaultValue, Predicate<Object> validator) {
            Objects.requireNonNull(defaultValue, "Default value can not be null");
            return define(path, () -> defaultValue, validator);
        }
        public <T> ConfigValue<T> define(String path, Supplier<T> defaultSupplier, Predicate<Object> validator) {
            return define(split(path), defaultSupplier, validator);
        }
        public <T> ConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier, Predicate<Object> validator) {
            return define(path, defaultSupplier, validator, Object.class);
        }
        public <T> ConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier, Predicate<Object> validator, Class<?> clazz) {
            context.setClazz(clazz);
            return define(path, new ValueSpec(defaultSupplier, validator, context, path), defaultSupplier);
        }
        public <T> ConfigValue<T> define(List<String> path, ValueSpec value, Supplier<T> defaultSupplier) {
            if (!currentPath.isEmpty()) {
                List<String> tmp = new ArrayList<>(currentPath.size() + path.size());
                tmp.addAll(currentPath);
                tmp.addAll(path);
                path = tmp;
            }
            spec.set(path, value);
            context = new BuilderContext();
            return new ConfigValue<>(this, path, defaultSupplier);
        }

        public <V extends Comparable<? super V>> ConfigValue<V> defineInRange(String path, V defaultValue, V min, V max, Class<V> clazz) {
            return defineInRange(split(path), defaultValue, min, max, clazz);
        }
        public <V extends Comparable<? super V>> ConfigValue<V> defineInRange(List<String> path, V defaultValue, V min, V max, Class<V> clazz) {
            return defineInRange(path, (Supplier<V>)() -> defaultValue, min, max, clazz);
        }
        public <V extends Comparable<? super V>> ConfigValue<V> defineInRange(String path, Supplier<V> defaultSupplier, V min, V max, Class<V> clazz) {
            return defineInRange(split(path), defaultSupplier, min, max, clazz);
        }
        public <V extends Comparable<? super V>> ConfigValue<V> defineInRange(List<String> path, Supplier<V> defaultSupplier, V min, V max, Class<V> clazz) {
            Range<V> range = new Range<>(clazz, min, max);
            context.setRange(range);
            comment(" Default: " + defaultSupplier.get());
            comment(" Range: " + range.toString());
            return define(path, defaultSupplier, range);
        }

        public <T> ConfigValue<T> defineInList(String path, T defaultValue, Collection<? extends T> acceptableValues) {
            return defineInList(split(path), defaultValue, acceptableValues);
        }
        public <T> ConfigValue<T> defineInList(String path, Supplier<T> defaultSupplier, Collection<? extends T> acceptableValues) {
            return defineInList(split(path), defaultSupplier, acceptableValues);
        }
        public <T> ConfigValue<T> defineInList(List<String> path, T defaultValue, Collection<? extends T> acceptableValues) {
            return defineInList(path, () -> defaultValue, acceptableValues);
        }
        public <T> ConfigValue<T> defineInList(List<String> path, Supplier<T> defaultSupplier, Collection<? extends T> acceptableValues) {
            Objects.requireNonNull(acceptableValues);
            return define(path, defaultSupplier, acceptableValues::contains);
        }

        // List methods (preserve original CSConfigSpec behavior)
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultValue, elementValidator);
        }
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineList(List<String> path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineList(path, () -> defaultValue, elementValidator);
        }
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineList(String path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultSupplier, elementValidator);
        }
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineList(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            context.setClazz(List.class);
            return define(path, new ValueSpec(defaultSupplier, x -> x instanceof List && ((List<?>) x).stream().allMatch(elementValidator), context, path) {
                @Override
                public Object correct(Object value) {
                    if (value == null || !(value instanceof List) || ((List<?>)value).isEmpty()) {
                        LOGGER.debug(Logging.CORE, "List on key {} is deemed to need correction. It is null, not a list, or an empty list. Modders, consider defineListAllowEmpty?", path.get(path.size() - 1));
                        return getDefault();
                    }
                    List<?> list = new ArrayList<>((List<?>) value);
                    list.removeIf(elementValidator.negate());
                    if (list.isEmpty()) {
                        LOGGER.debug(Logging.CORE, "List on key {} is deemed to need correction. It failed validation.", path.get(path.size() - 1));
                        return getDefault();
                    }
                    return list;
                }
            }, defaultSupplier);
        }

        public <T> ConfigValue<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultValue, newElementSupplier, elementValidator);
        }
        public <T> ConfigValue<List<? extends T>> defineList(String path, Supplier<List<? extends T>> defaultSupplier, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultSupplier, newElementSupplier, elementValidator);
        }
        public <T> ConfigValue<List<? extends T>> defineList(List<String> path, List<? extends T> defaultValue, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineList(path, () -> defaultValue, newElementSupplier, elementValidator);
        }
        public <T> ConfigValue<List<? extends T>> defineList(List<String> path, Supplier<List<? extends T>> defaultSupplier, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineList(path, defaultSupplier, newElementSupplier, elementValidator, ListValueSpec.NON_EMPTY);
        }

        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(split(path), defaultValue, elementValidator);
        }
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(path, () -> defaultValue, elementValidator);
        }
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(split(path), defaultSupplier, elementValidator);
        }
        @Deprecated
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            context.setClazz(List.class);
            return define(path, new ValueSpec(defaultSupplier, x -> x instanceof List && ((List<?>) x).stream().allMatch(elementValidator), context, path) {
                @Override
                public Object correct(Object value) {
                    if (value == null || !(value instanceof List)) {
                        LOGGER.debug(Logging.CORE, "List on key {} is deemed to need correction, as it is null or not a list.", path.get(path.size() - 1));
                        return getDefault();
                    }
                    List<?> list = new ArrayList<>((List<?>) value);
                    list.removeIf(elementValidator.negate());
                    // Allow empty lists in this variant
                    return list;
                }
            }, defaultSupplier);
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, List<? extends T> defaultValue, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(split(path), defaultValue, newElementSupplier, elementValidator);
        }
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String path, Supplier<List<? extends T>> defaultSupplier, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(split(path), defaultSupplier, newElementSupplier, elementValidator);
        }
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, List<? extends T> defaultValue, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(path, () -> defaultValue, newElementSupplier, elementValidator);
        }
        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, Supplier<List<? extends T>> defaultSupplier, Supplier<T> newElementSupplier, Predicate<Object> elementValidator) {
            context.setClazz(List.class);
            return define(path, new ListValueSpec(defaultSupplier, newElementSupplier, x -> x instanceof List && ((List<?>) x).stream().allMatch(elementValidator), elementValidator, context, path, null) {
                @Override
                public Object correct(Object value) {
                    if (value == null || !(value instanceof List)) {
                        LOGGER.debug(Logging.CORE, "List on key {} is deemed to need correction, as it is null or not a list.", path.get(path.size() - 1));
                        return getDefault();
                    }
                    List<?> list = new ArrayList<>((List<?>) value);
                    list.removeIf(elementValidator.negate());
                    // Allow empty lists in this variant
                    return list;
                }
            }, defaultSupplier);
        }

        public <T> ConfigValue<List<? extends T>> defineList(final List<String> path, Supplier<List<? extends T>> defaultSupplier, @Nullable Supplier<T> newElementSupplier, final Predicate<Object> elementValidator, @Nullable Range<Integer> sizeRange) {
            context.setClazz(List.class);
            return define(path, new ListValueSpec(defaultSupplier, newElementSupplier, x -> x instanceof List && ((List<?>) x).stream().allMatch(elementValidator), elementValidator, context, path, sizeRange) {
                @Override
                public Object correct(Object value) {
                    if (value == null || !(value instanceof List) || ((List<?>)value).isEmpty()) {
                        LOGGER.debug(Logging.CORE, "List on key {} is deemed to need correction. It is null, not a list, or an empty list. Modders, consider defineListAllowEmpty?", path.get(path.size() - 1));
                        return getDefault();
                    }
                    List<?> list = new ArrayList<>((List<?>) value);
                    list.removeIf(elementValidator.negate());
                    if (list.isEmpty()) {
                        LOGGER.debug(Logging.CORE, "List on key {} is deemed to need correction. It failed validation.", path.get(path.size() - 1));
                        return getDefault();
                    }
                    return list;
                }
            }, defaultSupplier);
        }

        // Enum
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue) {
            return defineEnum(split(path), defaultValue);
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter) {
            return defineEnum(split(path), defaultValue, converter);
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue) {
            return defineEnum(path, defaultValue, defaultValue.getDeclaringClass().getEnumConstants());
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter) {
            return defineEnum(path, defaultValue, converter, defaultValue.getDeclaringClass().getEnumConstants());
        }
        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, V... acceptableValues) {
            return defineEnum(split(path), defaultValue, acceptableValues);
        }
        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, V... acceptableValues) {
            return defineEnum(split(path), defaultValue, converter, acceptableValues);
        }
        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, V... acceptableValues) {
            return defineEnum(path, defaultValue, Arrays.asList(acceptableValues));
        }
        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, V... acceptableValues) {
            return defineEnum(path, defaultValue, converter, Arrays.asList(acceptableValues));
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, Collection<V> acceptableValues) {
            return defineEnum(split(path), defaultValue, acceptableValues);
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues) {
            return defineEnum(split(path), defaultValue, converter, acceptableValues);
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, Collection<V> acceptableValues) {
            return defineEnum(path, defaultValue, EnumGetMethod.NAME_IGNORECASE, acceptableValues);
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues) {
            return defineEnum(path, defaultValue, converter, obj -> {
                if (obj instanceof Enum) {
                    return acceptableValues.contains(obj);
                }
                if (obj == null) {
                    return false;
                }
                try {
                    return acceptableValues.contains(converter.get(obj, defaultValue.getDeclaringClass()));
                } catch (IllegalArgumentException | ClassCastException e) {
                    return false;
                }
            });
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, Predicate<Object> validator) {
            return defineEnum(split(path), defaultValue, validator);
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator) {
            return defineEnum(split(path), defaultValue, converter, validator);
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, Predicate<Object> validator) {
            return defineEnum(path, () -> defaultValue, validator, defaultValue.getDeclaringClass());
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator) {
            return defineEnum(path, () -> defaultValue, converter, validator, defaultValue.getDeclaringClass());
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz) {
            return defineEnum(split(path), defaultSupplier, validator, clazz);
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz) {
            return defineEnum(split(path), defaultSupplier, converter, validator, clazz);
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz) {
            return defineEnum(path, defaultSupplier, EnumGetMethod.NAME_IGNORECASE, validator, clazz);
        }
        public <V extends Enum<V>> EnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz) {
            context.setClazz(clazz);
            V[] allowedValues = clazz.getEnumConstants();
            comment("Allowed Values: " + Arrays.stream(allowedValues).filter(validator).map(Enum::name).collect(Collectors.joining(", ")));
            return new EnumValue<>(this, define(path, new ValueSpec(defaultSupplier, validator, context, path), defaultSupplier).getPath(), defaultSupplier, converter, clazz);
        }

        // Boolean
        public BooleanValue define(String path, boolean defaultValue) {
            return define(split(path), defaultValue);
        }
        public BooleanValue define(List<String> path, boolean defaultValue) {
            return define(path, (Supplier<Boolean>)() -> defaultValue);
        }
        public BooleanValue define(String path, Supplier<Boolean> defaultSupplier) {
            return define(split(path), defaultSupplier);
        }
        public BooleanValue define(List<String> path, Supplier<Boolean> defaultSupplier) {
            return new BooleanValue(this, define(path, defaultSupplier, o -> {
                if (o instanceof String) return ((String)o).equalsIgnoreCase("true") || ((String)o).equalsIgnoreCase("false");
                return o instanceof Boolean;
            }, Boolean.class).getPath(), defaultSupplier);
        }

        // Double
        public DoubleValue defineInRange(String path, double defaultValue, double min, double max) {
            return defineInRange(split(path), defaultValue, min, max);
        }
        public DoubleValue defineInRange(List<String> path, double defaultValue, double min, double max) {
            return defineInRange(path, () -> defaultValue, min, max);
        }
        public DoubleValue defineInRange(String path, Supplier<Double> defaultSupplier, double min, double max) {
            return defineInRange(split(path), defaultSupplier, min, max);
        }
        public DoubleValue defineInRange(List<String> path, Supplier<Double> defaultSupplier, double min, double max) {
            return new DoubleValue(this, defineInRange(path, defaultSupplier, min, max, Double.class).getPath(), defaultSupplier);
        }

        // Ints
        public IntValue defineInRange(String path, int defaultValue, int min, int max) {
            return defineInRange(split(path), defaultValue, min, max);
        }
        public IntValue defineInRange(List<String> path, int defaultValue, int min, int max) {
            return defineInRange(path, () -> defaultValue, min, max);
        }
        public IntValue defineInRange(String path, Supplier<Integer> defaultSupplier, int min, int max) {
            return defineInRange(split(path), defaultSupplier, min, max);
        }
        public IntValue defineInRange(List<String> path, Supplier<Integer> defaultSupplier, int min, int max) {
            return new IntValue(this, defineInRange(path, defaultSupplier, min, max, Integer.class).getPath(), defaultSupplier);
        }

        // Longs
        public LongValue defineInRange(String path, long defaultValue, long min, long max) {
            return defineInRange(split(path), defaultValue, min, max);
        }
        public LongValue defineInRange(List<String> path, long defaultValue, long min, long max) {
            return defineInRange(path, () -> defaultValue, min, max);
        }
        public LongValue defineInRange(String path, Supplier<Long> defaultSupplier, long min, long max) {
            return defineInRange(split(path), defaultSupplier, min, max);
        }
        public LongValue defineInRange(List<String> path, Supplier<Long> defaultSupplier, long min, long max) {
            return new LongValue(this, defineInRange(path, defaultSupplier, min, max, Long.class).getPath(), defaultSupplier);
        }

        public Builder comment(String comment) {
            context.addComment(comment);
            return this;
        }
        public Builder comment(String... comment) {
            for (int i = 0; i < comment.length; i++)
                Preconditions.checkNotNull(comment[i], "Comment string at " + i + " is null.");

            for (String s : comment)
                context.addComment(s);

            return this;
        }

        public Builder translation(String translationKey) {
            context.setTranslationKey(translationKey);
            return this;
        }

        public Builder worldRestart() {
            context.worldRestart();
            return this;
        }

        public Builder gameRestart() {
            context.gameRestart();
            return this;
        }

        public Builder push(String path) {
            return push(split(path));
        }

        public Builder push(List<String> path) {
            currentPath.addAll(path);
            if (context.hasComment()) {
                levelComments.put(new ArrayList<>(currentPath), context.buildComment(path));
                context.clearComment();
            }
            if (context.getTranslationKey() != null) {
                levelTranslationKeys.put(new ArrayList<>(currentPath), context.getTranslationKey());
                context.setTranslationKey(null);
            }
            context.ensureEmpty();
            return this;
        }

        public Builder pop() {
            return pop(1);
        }

        public Builder pop(int count) {
            if (count > currentPath.size())
                throw new IllegalArgumentException("Attempted to pop " + count + " elements when we only had: " + currentPath);
            for (int x = 0; x < count; x++)
                currentPath.remove(currentPath.size() - 1);
            return this;
        }

        public <T> Pair<T, CSConfigSpec> configure(Function<Builder, T> consumer) {
            T o = consumer.apply(this);
            return Pair.of(o, this.build());
        }

        public CSConfigSpec build() {
            context.ensureEmpty();
            Config valueCfg = Config.of(Config.getDefaultMapCreator(true, true), InMemoryFormat.withSupport(ConfigValue.class::isAssignableFrom));
            values.forEach(v -> valueCfg.set(v.getPath(), v));

            CSConfigSpec ret = new CSConfigSpec(spec.unmodifiable(), valueCfg.unmodifiable(), Collections.unmodifiableMap(levelComments), Collections.unmodifiableMap(levelTranslationKeys));
            values.forEach(v -> v.spec = ret);
            return ret;
        }
    }

    private static class BuilderContext {
        private final List<String> comment = new LinkedList<>();
        private @Nullable String langKey;
        private @Nullable Range<?> range;
        private RestartType restartType = RestartType.NONE;
        private @Nullable Class<?> clazz;

        public void addComment(String value) {
            Preconditions.checkNotNull(value, "Passed in null value for comment");
            comment.add(value);
        }

        public void clearComment() { comment.clear(); }
        public boolean hasComment() { return this.comment.size() > 0; }
        public String buildComment() { return buildComment(List.of("unknown", "unknown")); }
        public String buildComment(List<String> path) {
            if (comment.stream().allMatch(String::isBlank)) {
                if (FMLEnvironment.production) {
                    LOGGER.warn(Logging.CORE, "Detected a comment that is all whitespace for config option {}, which causes obscure bugs in NeoForge's config system and will cause a crash in the future. Please report this to the mod author.", DOT_JOINER.join(path));
                    return "A developer of this mod has defined this config option with a blank comment, which causes obscure bugs in NeoForge's config system and will cause a crash in the future. Please report this to the mod author.";
                } else {
                    throw new IllegalStateException("Can not build comment for config option " + DOT_JOINER.join(path) + " as it comprises entirely of blank lines/whitespace. This is not allowed as it causes a \"constantly correcting config\" bug with NightConfig in NeoForge's config system.");
                }
            }

            return LINE_JOINER.join(comment);
        }
        public void setTranslationKey(@Nullable String value) { this.langKey = value; }
        public @Nullable String getTranslationKey() { return this.langKey; }
        public <V extends Comparable<? super V>> void setRange(Range<V> value) {
            this.range = value;
            this.setClazz(value.getClazz());
        }
        @SuppressWarnings("unchecked")
        public <V extends Comparable<? super V>> @Nullable Range<V> getRange() { return (Range<V>)this.range; }
        public void worldRestart() { this.restartType = RestartType.WORLD; }
        public void gameRestart() { this.restartType = RestartType.GAME; }
        public RestartType restartType() { return this.restartType; }
        public void setClazz(Class<?> clazz) { this.clazz = clazz; }
        public @Nullable Class<?> getClazz() { return this.clazz; }

        public void ensureEmpty() {
            validate(hasComment(), "Non-empty comment when empty expected");
            validate(langKey, "Non-null translation key when null expected");
            validate(range, "Non-null range when null expected");
            validate(restartType != RestartType.NONE, "Dangling restart value set to " + restartType);
        }

        private void validate(@Nullable Object value, String message) {
            if (value != null)
                throw new IllegalStateException(message);
        }
        private void validate(boolean value, String message) {
            if (value)
                throw new IllegalStateException(message);
        }
    }

    public static class Range<V extends Comparable<? super V>> implements Predicate<Object> {
        private final Class<? extends V> clazz;
        private final V min;
        private final V max;

        private Range(Class<V> clazz, V min, V max) {
            this.clazz = clazz;
            this.min = min;
            this.max = max;
            if (min.compareTo(max) > 0) {
                throw new IllegalArgumentException("Range min must be less then max.");
            }
        }

        public static Range<Integer> of(int min, int max) {
            return new Range<>(Integer.class, min, max);
        }

        public Class<? extends V> getClazz() { return clazz; }
        public V getMin() { return min; }
        public V getMax() { return max; }

        private boolean isNumber(@Nullable Object other) {
            return Number.class.isAssignableFrom(clazz) && other instanceof Number;
        }

        @Override
        public boolean test(Object t) {
            if (isNumber(t)) {
                Number n = (Number) t;
                boolean result = ((Number)min).doubleValue() <= n.doubleValue() && n.doubleValue() <= ((Number)max).doubleValue();
                if (!result) {
                    LOGGER.debug(Logging.CORE, "Range value {} is not within its bounds {}-{}", n.doubleValue(), ((Number)min).doubleValue(), ((Number)max).doubleValue());
                }
                return result;
            }
            if (!clazz.isInstance(t)) return false;
            V c = clazz.cast(t);

            boolean result = c.compareTo(min) >= 0 && c.compareTo(max) <= 0;
            if (!result) {
                LOGGER.debug(Logging.CORE, "Range value {} is not within its bounds {}-{}", c, min, max);
            }
            return result;
        }

        public Object correct(@Nullable Object value, Object def) {
            if (isNumber(value)) {
                Number n = (Number) value;
                return n.doubleValue() < ((Number)min).doubleValue() ? min : n.doubleValue() > ((Number)max).doubleValue() ? max : value;
            }
            if (!clazz.isInstance(value)) return def;
            V c = clazz.cast(value);
            return c.compareTo(min) < 0 ? min : c.compareTo(max) > 0 ? max : value;
        }

        @Override
        public String toString() {
            if (clazz == Integer.class) {
                if (max.equals(Integer.MAX_VALUE)) {
                    return "> " + min;
                }
                if (min.equals(Integer.MIN_VALUE)) {
                    return "< " + max;
                }
            }
            return min + " ~ " + max;
        }
    }

    public static class ValueSpec {
        private final @Nullable String comment;
        private final @Nullable String langKey;
        private final @Nullable Range<?> range;
        private final @Nullable Class<?> clazz;
        private final Supplier<?> supplier;
        private final Predicate<Object> validator;
        private final RestartType restartType;

        private ValueSpec(Supplier<?> supplier, Predicate<Object> validator, BuilderContext context, List<String> path) {
            Objects.requireNonNull(supplier, "Default supplier can not be null");
            Objects.requireNonNull(validator, "Validator can not be null");

            this.comment = context.hasComment() ? context.buildComment(path) : null;
            this.langKey = context.getTranslationKey();
            this.range = context.getRange();
            this.restartType = context.restartType();
            this.clazz = context.getClazz();
            this.supplier = supplier;
            this.validator = validator;
        }

        public @Nullable String getComment() { return comment; }
        public @Nullable String getTranslationKey() { return langKey; }
        @SuppressWarnings("unchecked")
        public <V extends Comparable<? super V>> @Nullable Range<V> getRange() { return (Range<V>)this.range; }
        public RestartType restartType() { return this.restartType; }
        public @Nullable Class<?> getClazz() { return this.clazz; }
        public boolean test(@Nullable Object value) { return validator.test(value); }
        public Object correct(@Nullable Object value) { return range == null ? getDefault() : range.correct(value, getDefault()); }

        public Object getDefault() { return supplier.get(); }
    }

    public static class ListValueSpec extends ValueSpec {
        private static final Range<Integer> MAX_ELEMENTS = Range.of(0, Integer.MAX_VALUE);
        private static final Range<Integer> NON_EMPTY = Range.of(1, Integer.MAX_VALUE);

        private final @Nullable Supplier<?> newElementSupplier;
        private final @Nullable Range<Integer> sizeRange;
        private final Predicate<Object> elementValidator;

        private ListValueSpec(Supplier<?> supplier, @Nullable Supplier<?> newElementSupplier, Predicate<Object> listValidator, Predicate<Object> elementValidator, BuilderContext context, List<String> path, @Nullable Range<Integer> sizeRange) {
            super(supplier, listValidator, context, path);
            Objects.requireNonNull(elementValidator, "ElementValidator can not be null");
            this.newElementSupplier = newElementSupplier;
            this.elementValidator = elementValidator;
            this.sizeRange = Objects.requireNonNullElse(sizeRange, MAX_ELEMENTS);
        }

        public @Nullable Supplier<?> getNewElementSupplier() { return newElementSupplier; }
        public boolean testElement(Object value) { return elementValidator.test(value); }
        public Range<Integer> getSizeRange() { return sizeRange; }
    }

    public static class ConfigValue<T> implements Supplier<T> {
        private final Builder parent;
        private final List<String> path;
        private final Supplier<T> defaultSupplier;
        private @Nullable T cachedValue = null;
        private @Nullable CSConfigSpec spec;

        ConfigValue(Builder parent, List<String> path, Supplier<T> defaultSupplier) {
            this.parent = parent;
            this.path = path;
            this.defaultSupplier = defaultSupplier;
            this.parent.values.add(this);
        }

        public List<String> getPath() {
            return Lists.newArrayList(path);
        }

        @Override
        public T get() {
            if (cachedValue == null) {
                cachedValue = getRaw();
            }
            return cachedValue;
        }

        public T getRaw() {
            Preconditions.checkNotNull(spec, "Cannot get config value before spec is built");
            IConfigSpec.ILoadedConfig loadedConfig = spec.loadedConfig;
            Preconditions.checkState(loadedConfig != null, "Cannot get config value before config is loaded.");
            return getRaw(loadedConfig.config(), path, defaultSupplier);
        }

        public T getRaw(Config config, List<String> path, Supplier<T> defaultSupplier) {
            return config.getOrElse(path, defaultSupplier);
        }

        public T getDefault() {
            return defaultSupplier.get();
        }

        public Builder next() {
            return parent;
        }

        public void save() {
            Preconditions.checkNotNull(spec, "Cannot save config value before spec is built");
            Preconditions.checkNotNull(spec.loadedConfig, "Cannot save config value without assigned Config object present");
            spec.save();
        }

        public void set(T value) {
            Preconditions.checkNotNull(spec, "Cannot set config value before spec is built");
            IConfigSpec.ILoadedConfig loadedConfig = spec.loadedConfig;
            Preconditions.checkNotNull(loadedConfig, "Cannot set config value without assigned Config object present");
            loadedConfig.config().set(path, value);
            if (getSpec().restartType == RestartType.NONE) {
                this.cachedValue = value;
            }
        }

        public ValueSpec getSpec() {
            return (ValueSpec) parent.spec.get(path);
        }

        public void clearCache() {
            this.cachedValue = null;
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean> implements BooleanSupplier {
        BooleanValue(Builder parent, List<String> path, Supplier<Boolean> defaultSupplier) {
            super(parent, path, defaultSupplier);
        }

        @Override
        public boolean getAsBoolean() {
            return get();
        }

        public boolean isTrue() {
            return getAsBoolean();
        }

        public boolean isFalse() {
            return !getAsBoolean();
        }
    }

    public static class IntValue extends ConfigValue<Integer> implements IntSupplier {
        IntValue(Builder parent, List<String> path, Supplier<Integer> defaultSupplier) {
            super(parent, path, defaultSupplier);
        }

        @Override
        public Integer getRaw(Config config, List<String> path, Supplier<Integer> defaultSupplier) {
            return config.getIntOrElse(path, () -> defaultSupplier.get());
        }

        @Override
        public int getAsInt() {
            return get();
        }
    }

    public static class LongValue extends ConfigValue<Long> implements LongSupplier {
        LongValue(Builder parent, List<String> path, Supplier<Long> defaultSupplier) {
            super(parent, path, defaultSupplier);
        }

        @Override
        public Long getRaw(Config config, List<String> path, Supplier<Long> defaultSupplier) {
            return config.getLongOrElse(path, () -> defaultSupplier.get());
        }

        @Override
        public long getAsLong() {
            return get();
        }
    }

    public static class DoubleValue extends ConfigValue<Double> implements DoubleSupplier {
        DoubleValue(Builder parent, List<String> path, Supplier<Double> defaultSupplier) {
            super(parent, path, defaultSupplier);
        }

        @Override
        public Double getRaw(Config config, List<String> path, Supplier<Double> defaultSupplier) {
            Number n = config.get(path);
            return n == null ? defaultSupplier.get() : n.doubleValue();
        }

        @Override
        public double getAsDouble() {
            return get();
        }
    }

    public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
        private final EnumGetMethod converter;
        private final Class<T> clazz;

        EnumValue(Builder parent, List<String> path, Supplier<T> defaultSupplier, EnumGetMethod converter, Class<T> clazz) {
            super(parent, path, defaultSupplier);
            this.converter = converter;
            this.clazz = clazz;
        }

        @Override
        public T getRaw(Config config, List<String> path, Supplier<T> defaultSupplier) {
            return config.getEnumOrElse(path, clazz, converter, defaultSupplier);
        }
    }

    public enum RestartType {
        NONE(),
        WORLD(),
        GAME(ModConfig.Type.SERVER);

        private final Set<ModConfig.Type> invalidTypes = EnumSet.noneOf(ModConfig.Type.class);

        RestartType(ModConfig.Type... invalidTypes) {
            this.invalidTypes.addAll(Arrays.asList(invalidTypes));
        }

        private boolean isValid(ModConfig.Type type) {
            return !invalidTypes.contains(type);
        }

        public RestartType with(RestartType other) {
            if (other == NONE) return this;
            if (other == GAME || this == GAME) return GAME;
            return WORLD;
        }
    }
}