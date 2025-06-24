package com.momosoftworks.coldsweat.config.spec;

import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.utils.UnmodifiableConfigWrapper;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.momosoftworks.coldsweat.common.event.ConfigPostProcessor;
import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CSConfigSpec extends UnmodifiableConfigWrapper<UnmodifiableConfig> implements IConfigSpec<CSConfigSpec>
{
    private final Map<List<String>, String> levelComments;
    private final Map<List<String>, String> levelTranslationKeys;

    private final UnmodifiableConfig values;
    private Config childConfig;

    private boolean isCorrecting = false;

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern WINDOWS_NEWLINE = Pattern.compile("\r\n");

    private CSConfigSpec(UnmodifiableConfig storage, UnmodifiableConfig values, Map<List<String>, String> levelComments, Map<List<String>, String> levelTranslationKeys) {
        super(storage);
        this.values = values;
        this.levelComments = levelComments;
        this.levelTranslationKeys = levelTranslationKeys;
    }

    public String getLevelComment(List<String> path) {
        return levelComments.get(path);
    }

    public String getLevelTranslationKey(List<String> path) {
        return levelTranslationKeys.get(path);
    }

    public void setConfig(CommentedConfig config) {
        this.childConfig = config;
        if (config != null && !isCorrect(config)) {
            String configName = config instanceof FileConfig ? ((FileConfig) config).getNioPath().toString() : config.toString();
            LOGGER.warn(Logging.CORE, "Configuration file {} is not correct. Correcting", configName);
            correct(config,
                    (action, path, incorrectValue, correctedValue) ->
                            LOGGER.warn(Logging.CORE, "Incorrect key {} was corrected from {} to its default, {}. {}", DOT_JOINER.join( path ), incorrectValue, correctedValue, incorrectValue == correctedValue ? "This seems to be an error." : ""),
                    (action, path, incorrectValue, correctedValue) ->
                            LOGGER.debug(Logging.CORE, "The comment on key {} does not match the spec. This may create a backup.", DOT_JOINER.join( path )));

            if (config instanceof FileConfig) {
                ((FileConfig) config).save();
            }
        }
        this.afterReload();
    }

    @Override
    public void acceptConfig(final CommentedConfig data) {
        setConfig(data);
    }

    public boolean isCorrecting() {
        return isCorrecting;
    }

    public boolean isLoaded() {
        return childConfig != null;
    }

    public UnmodifiableConfig getSpec() {
        return this.config;
    }

    public UnmodifiableConfig getValues() {
        return this.values;
    }

    public void afterReload() {
        this.resetCaches(getValues().valueMap().values());
    }

    private void resetCaches(final Iterable<Object> configValues) {
        configValues.forEach(value -> {
            if (value instanceof CSConfigSpec.ConfigValue<?> configValue) {
                configValue.clearCache();
            } else if (value instanceof Config innerConfig) {
                this.resetCaches(innerConfig.valueMap().values());
            }
        });
    }

    public void save()
    {
        Preconditions.checkNotNull(childConfig, "Cannot save config value without assigned Config object present");
        if (childConfig instanceof FileConfig fileConfig) {
            fileConfig.save();
        }
    }

    public synchronized boolean isCorrect(CommentedConfig config) {
        LinkedList<String> parentPath = new LinkedList<>();
        return correct(this.config, config, parentPath, Collections.unmodifiableList(parentPath), (a, b, c, d) -> {}, null, true) == 0;
    }

    public int correct(CommentedConfig config) {
        return correct(config, (action, path, incorrectValue, correctedValue) -> {}, null);
    }

    public synchronized int correct(CommentedConfig config, ConfigSpec.CorrectionListener listener) {
        return correct(config, listener, null);
    }

    public synchronized int correct(CommentedConfig config, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener) {
        LinkedList<String> parentPath = new LinkedList<>(); //Linked list for fast add/removes
        int ret = 0;
        try {
            isCorrecting = true;
            ret = correct(this.config, config, parentPath, Collections.unmodifiableList(parentPath), listener, commentListener, false);
        } finally {
            isCorrecting = false;
        }
        return ret;
    }

    private int correct(UnmodifiableConfig spec, CommentedConfig config, LinkedList<String> parentPath, List<String> parentPathUnmodifiable, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener, boolean dryRun)
    {
        int count = 0;

        Map<String, Object> specMap = spec.valueMap();
        Map<String, Object> configMap = config.valueMap();

        for (Map.Entry<String, Object> specEntry : specMap.entrySet())
        {
            final String key = specEntry.getKey();
            final Object specValue = specEntry.getValue();
            final Object configValue = configMap.get(key);
            final ConfigSpec.CorrectionAction action = configValue == null ? ConfigSpec.CorrectionAction.ADD : ConfigSpec.CorrectionAction.REPLACE;

            parentPath.addLast(key);

            if (specValue instanceof Config)
            {
                if (configValue instanceof CommentedConfig)
                {
                    count += correct((Config)specValue, (CommentedConfig)configValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                    if (count > 0 && dryRun)
                        return count;
                }
                else if (dryRun)
                {
                    return 1;
                }
                else
                {
                    CommentedConfig newValue = config.createSubConfig();
                    configMap.put(key, newValue);
                    listener.onCorrect(action, parentPathUnmodifiable, configValue, newValue);
                    if (action != ConfigSpec.CorrectionAction.ADD)
                    {   count++;
                    }
                    count += correct((Config)specValue, newValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
                }

                String newComment = levelComments.get(parentPath);
                String oldComment = config.getComment(key);

                // CUSTOM: Allow drill_down comment formatting changes
                if (!isCommentFormattingChange(oldComment, newComment))
                {
                    if (!stringsMatchIgnoringNewlines(oldComment, newComment))
                    {
                        if(commentListener != null)
                            commentListener.onCorrect(action, parentPathUnmodifiable, oldComment, newComment);

                        if (dryRun)
                            return 1;

                        config.setComment(key, newComment);
                    }
                }
            }
            else
            {
                CSConfigSpec.ValueSpec valueSpec = (CSConfigSpec.ValueSpec)specValue;

                // CUSTOM: Use formatting-aware value testing
                if (!isValueCorrectWithFormatting(valueSpec, configValue))
                {
                    if (dryRun)
                        return 1;

                    Object newValue = valueSpec.correct(configValue);
                    configMap.put(key, newValue);
                    listener.onCorrect(action, parentPathUnmodifiable, configValue, newValue);
                    if (action != ConfigSpec.CorrectionAction.ADD)
                    {   count++;
                    }
                }

                String oldComment = config.getComment(key);
                String expectedComment = valueSpec.getComment();

                // CUSTOM: Allow drill_down comment formatting changes
                if (!isCommentFormattingChange(oldComment, expectedComment))
                {
                    if (!stringsMatchIgnoringNewlines(oldComment, expectedComment))
                    {
                        if (commentListener != null)
                            commentListener.onCorrect(action, parentPathUnmodifiable, oldComment, expectedComment);

                        if (dryRun)
                            return 1;

                        config.setComment(key, expectedComment);
                    }
                }
            }

            parentPath.removeLast();
        }

        // Second step: removes the unspecified values
        for (Iterator<Map.Entry<String, Object>> ittr = configMap.entrySet().iterator(); ittr.hasNext();)
        {
            Map.Entry<String, Object> entry = ittr.next();
            if (!specMap.containsKey(entry.getKey()))
            {
                if (dryRun)
                    return 1;

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
    private boolean isCommentFormattingChange(String oldComment, String newComment)
    {
        // Normalize both comments by removing drill_down directives
        String normalizedOld = normalizeComment(oldComment);
        String normalizedNew = normalizeComment(newComment);

        // If they're the same after normalization, this is just a formatting change
        return stringsMatchIgnoringNewlines(normalizedOld, normalizedNew);
    }

    /**
     * Normalizes a comment by removing drill_down directives and extra whitespace
     */
    private String normalizeComment(String comment)
    {
        if (comment == null)
        {   return "";
        }

        // Remove //drill_down directive and clean up
        String normalized = comment.replace("//drill_down", "").trim();

        // If the comment becomes empty or just a #, return empty string
        if (normalized.equals("#") || normalized.isEmpty())
        {   return "";
        }

        return normalized;
    }

    /**
     * Enhanced value testing that considers formatting equivalence
     */
    private boolean isValueCorrectWithFormatting(CSConfigSpec.ValueSpec valueSpec, Object configValue)
    {
        // First, use the standard test
        if (valueSpec.test(configValue))
        {   return true;
        }

        // If standard test fails, check if it's a formatting difference
        if (configValue instanceof List && isListFormattingEquivalent(valueSpec, (List<?>) configValue))
        {   return true;
        }

        // Add other formatting equivalence checks as needed
        return false;
    }

    /**
     * Checks if a list value is semantically equivalent despite formatting differences
     */
    private boolean isListFormattingEquivalent(CSConfigSpec.ValueSpec valueSpec, List<?> configValue)
    {
        try
        {   // Get the expected value after correction
            Object correctedValue = valueSpec.correct(configValue);

            // If correction doesn't change the value semantically, it's just formatting
            if (correctedValue instanceof List)
            {   List<?> correctedList = (List<?>) correctedValue;

                // Compare sizes first
                if (configValue.size() != correctedList.size())
                {   return false;
                }

                // Compare elements (this handles both single-line and multi-line arrays)
                for (int i = 0; i < configValue.size(); i++)
                {   Object configElement = configValue.get(i);
                    Object correctedElement = correctedList.get(i);

                    if (!Objects.equals(configElement, correctedElement))
                    {   return false;
                    }
                }

                return true; // All elements match, this is just formatting
            }
        }
        catch (Exception e)
        {   // If anything goes wrong, fall back to standard behavior
        }

        return false;
    }

    /**
     * Enhanced string matching that's more lenient about whitespace and formatting
     */
    private boolean stringsMatchIgnoringNewlines(String str1, String str2)
    {
        if (str1 == str2) return true;
        if (str1 == null || str2 == null) return false;

        // Normalize whitespace and newlines
        String normalized1 = str1.replaceAll("\\s+", " ").trim();
        String normalized2 = str2.replaceAll("\\s+", " ").trim();

        return normalized1.equals(normalized2);
    }

    public static class Builder
    {
        private final Config storage = Config.of(LinkedHashMap::new, InMemoryFormat.withUniversalSupport()); // Use LinkedHashMap for consistent ordering
        private CSConfigSpec.BuilderContext context = new CSConfigSpec.BuilderContext();
        private final Map<List<String>, String> levelComments = new HashMap<>();
        private final Map<List<String>, String> levelTranslationKeys = new HashMap<>();
        private final List<String> currentPath = new ArrayList<>();
        private final List<CSConfigSpec.ConfigValue<?>> values = new ArrayList<>();

        //Object
        public <T> CSConfigSpec.ConfigValue<T> define(String path, T defaultValue) {
            return define(split(path), defaultValue);
        }
        public <T> CSConfigSpec.ConfigValue<T> define(List<String> path, T defaultValue) {
            return define(path, defaultValue, o -> o != null && defaultValue.getClass().isAssignableFrom(o.getClass()));
        }
        public <T> CSConfigSpec.ConfigValue<T> define(String path, T defaultValue, Predicate<Object> validator) {
            return define(split(path), defaultValue, validator);
        }
        public <T> CSConfigSpec.ConfigValue<T> define(List<String> path, T defaultValue, Predicate<Object> validator) {
            Objects.requireNonNull(defaultValue, "Default value can not be null");
            return define(path, () -> defaultValue, validator);
        }
        public <T> CSConfigSpec.ConfigValue<T> define(String path, Supplier<T> defaultSupplier, Predicate<Object> validator) {
            return define(split(path), defaultSupplier, validator);
        }
        public <T> CSConfigSpec.ConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier, Predicate<Object> validator) {
            return define(path, defaultSupplier, validator, Object.class);
        }
        public <T> CSConfigSpec.ConfigValue<T> define(List<String> path, Supplier<T> defaultSupplier, Predicate<Object> validator, Class<?> clazz) {
            context.setClazz(clazz);
            return define(path, new CSConfigSpec.ValueSpec(defaultSupplier, validator, context, path), defaultSupplier);
        }
        public <T> CSConfigSpec.ConfigValue<T> define(List<String> path, CSConfigSpec.ValueSpec value, Supplier<T> defaultSupplier) { // This is the root where everything at the end of the day ends up.
            if (!currentPath.isEmpty()) {
                List<String> tmp = new ArrayList<>(currentPath.size() + path.size());
                tmp.addAll(currentPath);
                tmp.addAll(path);
                path = tmp;
            }
            storage.set(path, value);
            context = new CSConfigSpec.BuilderContext();
            return new CSConfigSpec.ConfigValue<>(this, path, defaultSupplier);
        }
        public <V extends Comparable<? super V>> CSConfigSpec.ConfigValue<V> defineInRange(String path, V defaultValue, V min, V max, Class<V> clazz) {
            return defineInRange(split(path), defaultValue, min, max, clazz);
        }
        public <V extends Comparable<? super V>> CSConfigSpec.ConfigValue<V> defineInRange(List<String> path, V defaultValue, V min, V max, Class<V> clazz) {
            return defineInRange(path, (Supplier<V>)() -> defaultValue, min, max, clazz);
        }
        public <V extends Comparable<? super V>> CSConfigSpec.ConfigValue<V> defineInRange(String path, Supplier<V> defaultSupplier, V min, V max, Class<V> clazz) {
            return defineInRange(split(path), defaultSupplier, min, max, clazz);
        }
        public <V extends Comparable<? super V>> CSConfigSpec.ConfigValue<V> defineInRange(List<String> path, Supplier<V> defaultSupplier, V min, V max, Class<V> clazz) {
            CSConfigSpec.Range<V> range = new CSConfigSpec.Range<>(clazz, min, max);
            context.setRange(range);
            comment("Range: " + range.toString());
            if (min.compareTo(max) > 0)
                throw new IllegalArgumentException("Range min most be less then max.");
            return define(path, defaultSupplier, range);
        }
        public <T> CSConfigSpec.ConfigValue<T> defineInList(String path, T defaultValue, Collection<? extends T> acceptableValues) {
            return defineInList(split(path), defaultValue, acceptableValues);
        }
        public <T> CSConfigSpec.ConfigValue<T> defineInList(String path, Supplier<T> defaultSupplier, Collection<? extends T> acceptableValues) {
            return defineInList(split(path), defaultSupplier, acceptableValues);
        }
        public <T> CSConfigSpec.ConfigValue<T> defineInList(List<String> path, T defaultValue, Collection<? extends T> acceptableValues) {
            return defineInList(path, () -> defaultValue, acceptableValues);
        }
        public <T> CSConfigSpec.ConfigValue<T> defineInList(List<String> path, Supplier<T> defaultSupplier, Collection<? extends T> acceptableValues) {
            return define(path, defaultSupplier, o -> o != null && acceptableValues.contains(o));
        }
        public <T> CSConfigSpec.ConfigValue<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultValue, elementValidator);
        }
        public <T> CSConfigSpec.ConfigValue<List<? extends T>> defineList(String path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            return defineList(split(path), defaultSupplier, elementValidator);
        }
        public <T> CSConfigSpec.ConfigValue<List<? extends T>> defineList(List<String> path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineList(path, () -> defaultValue, elementValidator);
        }
        public <T> CSConfigSpec.ConfigValue<List<? extends T>> defineList(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            context.setClazz(List.class);
            return define(path, new CSConfigSpec.ValueSpec(defaultSupplier, x -> x instanceof List && ((List<?>) x).stream().allMatch(elementValidator ), context, path) {
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
        public <T> CSConfigSpec.ConfigValue<List<? extends T>> defineListAllowEmpty(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(split(path), defaultValue, elementValidator);
        }
        public <T> CSConfigSpec.ConfigValue<List<? extends T>> defineListAllowEmpty(String path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(split(path), defaultSupplier, elementValidator);
        }
        public <T> CSConfigSpec.ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
            return defineListAllowEmpty(path, () -> defaultValue, elementValidator);
        }
        public <T> CSConfigSpec.ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
            context.setClazz(List.class);
            return define(path, new CSConfigSpec.ValueSpec(defaultSupplier, x -> x instanceof List && ((List<?>) x).stream().allMatch(elementValidator ), context, path) {
                @Override
                public Object correct(Object value) {
                    if (value == null || !(value instanceof List)) {
                        LOGGER.debug(Logging.CORE, "List on key {} is deemed to need correction, as it is null or not a list.", path.get(path.size() - 1));
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

        //Enum
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(String path, V defaultValue) {
            return defineEnum(split(path), defaultValue);
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter) {
            return defineEnum(split(path), defaultValue, converter);
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(List<String> path, V defaultValue) {
            return defineEnum(path, defaultValue, defaultValue.getDeclaringClass().getEnumConstants());
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter) {
            return defineEnum(path, defaultValue, converter, defaultValue.getDeclaringClass().getEnumConstants());
        }
        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(String path, V defaultValue, V... acceptableValues) {
            return defineEnum(split(path), defaultValue, acceptableValues);
        }
        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, V... acceptableValues) {
            return defineEnum(split(path), defaultValue, converter, acceptableValues);
        }
        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(List<String> path, V defaultValue, V... acceptableValues) {
            return defineEnum(path, defaultValue, (Collection<V>) Arrays.asList(acceptableValues));
        }
        @SuppressWarnings("unchecked")
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, V... acceptableValues) {
            return defineEnum(path, defaultValue, converter, Arrays.asList(acceptableValues));
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(String path, V defaultValue, Collection<V> acceptableValues) {
            return defineEnum(split(path), defaultValue, acceptableValues);
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues) {
            return defineEnum(split(path), defaultValue, converter, acceptableValues);
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(List<String> path, V defaultValue, Collection<V> acceptableValues) {
            return defineEnum(path, defaultValue, EnumGetMethod.NAME_IGNORECASE, acceptableValues);
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Collection<V> acceptableValues) {
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
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(String path, V defaultValue, Predicate<Object> validator) {
            return defineEnum(split(path), defaultValue, validator);
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(String path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator) {
            return defineEnum(split(path), defaultValue, converter, validator);
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(List<String> path, V defaultValue, Predicate<Object> validator) {
            return defineEnum(path, () -> defaultValue, validator, defaultValue.getDeclaringClass());
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(List<String> path, V defaultValue, EnumGetMethod converter, Predicate<Object> validator) {
            return defineEnum(path, () -> defaultValue, converter, validator, defaultValue.getDeclaringClass());
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz) {
            return defineEnum(split(path), defaultSupplier, validator, clazz);
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(String path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz) {
            return defineEnum(split(path), defaultSupplier, converter, validator, clazz);
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, Predicate<Object> validator, Class<V> clazz) {
            return defineEnum(path, defaultSupplier, EnumGetMethod.NAME_IGNORECASE, validator, clazz);
        }
        public <V extends Enum<V>> CSConfigSpec.EnumValue<V> defineEnum(List<String> path, Supplier<V> defaultSupplier, EnumGetMethod converter, Predicate<Object> validator, Class<V> clazz) {
            context.setClazz(clazz);
            V[] allowedValues = clazz.getEnumConstants();
            comment("Allowed Values: " + Arrays.stream(allowedValues).filter(validator).map(Enum::name).collect(Collectors.joining(", ")));
            return new CSConfigSpec.EnumValue<V>(this, define(path, new CSConfigSpec.ValueSpec(defaultSupplier, validator, context, path), defaultSupplier).getPath(), defaultSupplier, converter, clazz);
        }

        //boolean
        public CSConfigSpec.BooleanValue define(String path, boolean defaultValue) {
            return define(split(path), defaultValue);
        }
        public CSConfigSpec.BooleanValue define(List<String> path, boolean defaultValue) {
            return define(path, (Supplier<Boolean>)() -> defaultValue);
        }
        public CSConfigSpec.BooleanValue define(String path, Supplier<Boolean> defaultSupplier) {
            return define(split(path), defaultSupplier);
        }
        public CSConfigSpec.BooleanValue define(List<String> path, Supplier<Boolean> defaultSupplier) {
            return new CSConfigSpec.BooleanValue(this, define(path, defaultSupplier, o -> {
                if (o instanceof String) return ((String)o).equalsIgnoreCase("true") || ((String)o).equalsIgnoreCase("false");
                return o instanceof Boolean;
            }, Boolean.class).getPath(), defaultSupplier);
        }

        //Double
        public CSConfigSpec.DoubleValue defineInRange(String path, double defaultValue, double min, double max) {
            return defineInRange(split(path), defaultValue, min, max);
        }
        public CSConfigSpec.DoubleValue defineInRange(List<String> path, double defaultValue, double min, double max) {
            return defineInRange(path, (Supplier<Double>)() -> defaultValue, min, max);
        }
        public CSConfigSpec.DoubleValue defineInRange(String path, Supplier<Double> defaultSupplier, double min, double max) {
            return defineInRange(split(path), defaultSupplier, min, max);
        }
        public CSConfigSpec.DoubleValue defineInRange(List<String> path, Supplier<Double> defaultSupplier, double min, double max) {
            return new CSConfigSpec.DoubleValue(this, defineInRange(path, defaultSupplier, min, max, Double.class).getPath(), defaultSupplier);
        }

        //Ints
        public CSConfigSpec.IntValue defineInRange(String path, int defaultValue, int min, int max) {
            return defineInRange(split(path), defaultValue, min, max);
        }
        public CSConfigSpec.IntValue defineInRange(List<String> path, int defaultValue, int min, int max) {
            return defineInRange(path, (Supplier<Integer>)() -> defaultValue, min, max);
        }
        public CSConfigSpec.IntValue defineInRange(String path, Supplier<Integer> defaultSupplier, int min, int max) {
            return defineInRange(split(path), defaultSupplier, min, max);
        }
        public CSConfigSpec.IntValue defineInRange(List<String> path, Supplier<Integer> defaultSupplier, int min, int max) {
            return new CSConfigSpec.IntValue(this, defineInRange(path, defaultSupplier, min, max, Integer.class).getPath(), defaultSupplier);
        }

        //Longs
        public CSConfigSpec.LongValue defineInRange(String path, long defaultValue, long min, long max) {
            return defineInRange(split(path), defaultValue, min, max);
        }
        public CSConfigSpec.LongValue defineInRange(List<String> path, long defaultValue, long min, long max) {
            return defineInRange(path, (Supplier<Long>)() -> defaultValue, min, max);
        }
        public CSConfigSpec.LongValue defineInRange(String path, Supplier<Long> defaultSupplier, long min, long max) {
            return defineInRange(split(path), defaultSupplier, min, max);
        }
        public CSConfigSpec.LongValue defineInRange(List<String> path, Supplier<Long> defaultSupplier, long min, long max) {
            return new CSConfigSpec.LongValue(this, defineInRange(path, defaultSupplier, min, max, Long.class).getPath(), defaultSupplier);
        }

        public CSConfigSpec.Builder comment(String comment)
        {
            context.addComment(comment);
            return this;
        }
        public CSConfigSpec.Builder comment(String... comment)
        {
            // Iterate list first, to throw meaningful errors
            // Don't add any comments until we make sure there is no nulls
            for (int i = 0; i < comment.length; i++)
                Preconditions.checkNotNull(comment[i], "Comment string at " + i + " is null.");

            for (String s : comment)
                context.addComment(s);

            return this;
        }

        public CSConfigSpec.Builder translation(String translationKey)
        {
            context.setTranslationKey(translationKey);
            return this;
        }

        public CSConfigSpec.Builder worldRestart()
        {
            context.worldRestart();
            return this;
        }

        public CSConfigSpec.Builder push(String path) {
            return push(split(path));
        }

        public CSConfigSpec.Builder push(List<String> path) {
            currentPath.addAll(path);
            if (context.hasComment()) {
                levelComments.put(new ArrayList<>(currentPath), context.buildComment(path));
                context.clearComment(); // Set to empty
            }
            if (context.getTranslationKey() != null) {
                levelTranslationKeys.put(new ArrayList<String>(currentPath), context.getTranslationKey());
                context.setTranslationKey(null);
            }
            context.ensureEmpty();
            return this;
        }

        public CSConfigSpec.Builder pop() {
            return pop(1);
        }

        public CSConfigSpec.Builder pop(int count) {
            if (count > currentPath.size())
                throw new IllegalArgumentException("Attempted to pop " + count + " elements when we only had: " + currentPath);
            for (int x = 0; x < count; x++)
                currentPath.remove(currentPath.size() - 1);
            return this;
        }

        public <T> Pair<T, CSConfigSpec> configure(Function<CSConfigSpec.Builder, T> consumer) {
            T o = consumer.apply(this);
            return Pair.of(o, this.build());
        }

        public CSConfigSpec build()
        {
            context.ensureEmpty();
            Config valueCfg = Config.of(Config.getDefaultMapCreator(true, true), InMemoryFormat.withSupport(CSConfigSpec.ConfigValue.class::isAssignableFrom));
            values.forEach(v -> valueCfg.set(v.getPath(), v));

            CSConfigSpec ret = new CSConfigSpec(storage, valueCfg, levelComments, levelTranslationKeys);
            values.forEach(v -> v.spec = ret);
            return ret;
        }

        public interface BuilderConsumer {
            void accept(CSConfigSpec.Builder builder);
        }
    }

    private static class BuilderContext
    {
        private final List<String> comment = new LinkedList<>();
        private String langKey;
        private CSConfigSpec.Range<?> range;
        private boolean worldRestart = false;
        private Class<?> clazz;

        public void addComment(String value)
        {
            // Don't use `validate` because it throws IllegalStateException, not NullPointerException
            Preconditions.checkNotNull(value, "Passed in null value for comment");

            comment.add(value);
        }

        public void clearComment() { comment.clear(); }
        public boolean hasComment() { return this.comment.size() > 0; }
        public String buildComment() { return buildComment(List.of("unknown", "unknown")); }
        public String buildComment(final List<String> path)
        {
            if (comment.stream().allMatch(String::isBlank))
            {
                if (FMLEnvironment.production)
                    LOGGER.warn(Logging.CORE, "Detected a comment that is all whitespace for config option {}, which causes obscure bugs in Forge's config system and will cause a crash in the future. Please report this to the mod author.",
                                DOT_JOINER.join(path));
                else
                    throw new IllegalStateException("Can not build comment for config option " + DOT_JOINER.join(path) + " as it comprises entirely of blank lines/whitespace. This is not allowed as it causes a \"constantly correcting config\" bug with NightConfig in Forge's config system.");

                return "A developer of this mod has defined this config option with a blank comment, which causes obscure bugs in Forge's config system and will cause a crash in the future. Please report this to the mod author.";
            }

            return LINE_JOINER.join(comment);
        }
        public void setTranslationKey(String value) { this.langKey = value; }
        public String getTranslationKey() { return this.langKey; }
        public <V extends Comparable<? super V>> void setRange(CSConfigSpec.Range<V> value)
        {
            this.range = value;
            this.setClazz(value.getClazz());
        }
        @SuppressWarnings("unchecked")
        public <V extends Comparable<? super V>> CSConfigSpec.Range<V> getRange() { return (CSConfigSpec.Range<V>)this.range; }
        public void worldRestart() { this.worldRestart = true; }
        public boolean needsWorldRestart() { return this.worldRestart; }
        public void setClazz(Class<?> clazz) { this.clazz = clazz; }
        public Class<?> getClazz(){ return this.clazz; }

        public void ensureEmpty()
        {
            validate(hasComment(), "Non-empty comment when empty expected");
            validate(langKey, "Non-null translation key when null expected");
            validate(range, "Non-null range when null expected");
            validate(worldRestart, "Dangeling world restart value set to true");
        }

        private void validate(Object value, String message)
        {
            if (value != null)
                throw new IllegalStateException(message);
        }
        private void validate(boolean value, String message)
        {
            if (value)
                throw new IllegalStateException(message);
        }
    }

    @SuppressWarnings("unused")
    public static class Range<V extends Comparable<? super V>> implements Predicate<Object>
    {
        private final Class<? extends V> clazz;
        private final V min;
        private final V max;

        private Range(Class<V> clazz, V min, V max)
        {
            this.clazz = clazz;
            this.min = min;
            this.max = max;
        }

        public Class<? extends V> getClazz() { return clazz; }
        public V getMin() { return min; }
        public V getMax() { return max; }

        private boolean isNumber(Object other)
        {
            return Number.class.isAssignableFrom(clazz) && other instanceof Number;
        }

        @Override
        public boolean test(Object t)
        {
            if (isNumber(t))
            {
                Number n = (Number) t;
                boolean result = ((Number)min).doubleValue() <= n.doubleValue() && n.doubleValue() <= ((Number)max).doubleValue();
                if(!result)
                {
                    LOGGER.debug(Logging.CORE, "Range value {} is not within its bounds {}-{}", n.doubleValue(), ((Number)min).doubleValue(), ((Number)max).doubleValue());
                }
                return result;
            }
            if (!clazz.isInstance(t)) return false;
            V c = clazz.cast(t);

            boolean result = c.compareTo(min) >= 0 && c.compareTo(max) <= 0;
            if(!result)
            {
                LOGGER.debug(Logging.CORE, "Range value {} is not within its bounds {}-{}", c, min, max);
            }
            return result;
        }

        public Object correct(Object value, Object def)
        {
            if (isNumber(value))
            {
                Number n = (Number) value;
                return n.doubleValue() < ((Number)min).doubleValue() ? min : n.doubleValue() > ((Number)max).doubleValue() ? max : value;
            }
            if (!clazz.isInstance(value)) return def;
            V c = clazz.cast(value);
            return c.compareTo(min) < 0 ? min : c.compareTo(max) > 0 ? max : value;
        }

        @Override
        public String toString()
        {
            if (clazz == Integer.class) {
                if (max.equals(Integer.MAX_VALUE)) {
                    return "> " + min;
                } else if (min.equals(Integer.MIN_VALUE)) {
                    return "< " + max;
                }
            } // TODO add more special cases?
            return min + " ~ " + max;
        }
    }

    public static class ValueSpec
    {
        private final String comment;
        private final String langKey;
        private final CSConfigSpec.Range<?> range;
        private final boolean worldRestart;
        private final Class<?> clazz;
        private final Supplier<?> supplier;
        private final Predicate<Object> validator;

        private ValueSpec(Supplier<?> supplier, Predicate<Object> validator, CSConfigSpec.BuilderContext context, List<String> path)
        {
            Objects.requireNonNull(supplier, "Default supplier can not be null");
            Objects.requireNonNull(validator, "Validator can not be null");

            this.comment = context.hasComment() ? context.buildComment(path) : null;
            this.langKey = context.getTranslationKey();
            this.range = context.getRange();
            this.worldRestart = context.needsWorldRestart();
            this.clazz = context.getClazz();
            this.supplier = supplier;
            this.validator = validator;
        }

        public String getComment() { return comment; }
        public String getTranslationKey() { return langKey; }
        @SuppressWarnings("unchecked")
        public <V extends Comparable<? super V>> CSConfigSpec.Range<V> getRange() { return (CSConfigSpec.Range<V>)this.range; }
        public boolean needsWorldRestart() { return this.worldRestart; }
        public Class<?> getClazz(){ return this.clazz; }
        public boolean test(Object value) { return validator.test(value); }
        public Object correct(Object value) { return range == null ? getDefault() : range.correct(value, getDefault()); }

        public Object getDefault() { return supplier.get(); }
    }

    public static class ConfigValue<T> implements Supplier<T>
    {
        private static boolean USE_CACHES = true;

        private final CSConfigSpec.Builder parent;
        private final List<String> path;
        private final Supplier<T> defaultSupplier;

        private T cachedValue = null;

        private CSConfigSpec spec;

        ConfigValue(CSConfigSpec.Builder parent, List<String> path, Supplier<T> defaultSupplier)
        {
            this.parent = parent;
            this.path = path;
            this.defaultSupplier = defaultSupplier;
            this.parent.values.add(this);
        }

        public List<String> getPath()
        {
            return new ArrayList<>(path);
        }

        /**
         * Returns the actual value for the configuration setting, throwing if the config has not yet been loaded.
         *
         * @return the actual value for the setting
         * @throws NullPointerException if the {@link CSConfigSpec config spec} object that will contain this has
         *                              not yet been built
         * @throws IllegalStateException if the associated config has not yet been loaded
         */
        @Override
        public T get()
        {
            Preconditions.checkNotNull(spec, "Cannot get config value before spec is built");
            // TODO: Remove this dev-time check so this errors out on both production and dev
            // This is dev-time-only in 1.19.x, to avoid breaking already published mods while forcing devs to fix their errors
            if (!FMLEnvironment.production)
            {
                // When the above if-check is removed, change message to "Cannot get config value before config is loaded"
                Preconditions.checkState(spec.childConfig != null, """
                        Cannot get config value before config is loaded.
                        This error is currently only thrown in the development environment, to avoid breaking published mods.
                        In a future version, this will also throw in the production environment.
                        """);
            }

            if (spec.childConfig == null)
                return defaultSupplier.get();

            if (USE_CACHES && cachedValue == null)
                cachedValue = getRaw(spec.childConfig, path, defaultSupplier);
            else if (!USE_CACHES)
                return getRaw(spec.childConfig, path, defaultSupplier);

            return cachedValue;
        }

        protected T getRaw(Config config, List<String> path, Supplier<T> defaultSupplier)
        {
            return config.getOrElse(path, defaultSupplier);
        }

        /**
         * {@return the default value for the configuration setting}
         */
        public T getDefault()
        {
            return defaultSupplier.get();
        }

        public CSConfigSpec.Builder next()
        {
            return parent;
        }

        public void save()
        {
            Preconditions.checkNotNull(spec, "Cannot save config value before spec is built");
            Preconditions.checkNotNull(spec.childConfig, "Cannot save config value without assigned Config object present");
            spec.save();
        }

        public void set(T value)
        {
            Preconditions.checkNotNull(spec, "Cannot set config value before spec is built");
            Preconditions.checkNotNull(spec.childConfig, "Cannot set config value without assigned Config object present");
            spec.childConfig.set(path, value);
            this.cachedValue = value;
        }

        public void clearCache() {
            this.cachedValue = null;
        }
    }

    public static class BooleanValue extends CSConfigSpec.ConfigValue<Boolean>
    {
        BooleanValue(CSConfigSpec.Builder parent, List<String> path, Supplier<Boolean> defaultSupplier)
        {
            super(parent, path, defaultSupplier);
        }
    }

    public static class IntValue extends CSConfigSpec.ConfigValue<Integer>
    {
        IntValue(CSConfigSpec.Builder parent, List<String> path, Supplier<Integer> defaultSupplier)
        {
            super(parent, path, defaultSupplier);
        }

        @Override
        protected Integer getRaw(Config config, List<String> path, Supplier<Integer> defaultSupplier)
        {
            return config.getIntOrElse(path, () -> defaultSupplier.get());
        }
    }

    public static class LongValue extends CSConfigSpec.ConfigValue<Long>
    {
        LongValue(CSConfigSpec.Builder parent, List<String> path, Supplier<Long> defaultSupplier)
        {
            super(parent, path, defaultSupplier);
        }

        @Override
        protected Long getRaw(Config config, List<String> path, Supplier<Long> defaultSupplier)
        {
            return config.getLongOrElse(path, () -> defaultSupplier.get());
        }
    }

    public static class DoubleValue extends CSConfigSpec.ConfigValue<Double>
    {
        DoubleValue(CSConfigSpec.Builder parent, List<String> path, Supplier<Double> defaultSupplier)
        {
            super(parent, path, defaultSupplier);
        }

        @Override
        protected Double getRaw(Config config, List<String> path, Supplier<Double> defaultSupplier)
        {
            Number n = config.<Number>get(path);
            return n == null ? defaultSupplier.get() : n.doubleValue();
        }
    }

    public static class EnumValue<T extends Enum<T>> extends CSConfigSpec.ConfigValue<T>
    {
        private final EnumGetMethod converter;
        private final Class<T> clazz;

        EnumValue(CSConfigSpec.Builder parent, List<String> path, Supplier<T> defaultSupplier, EnumGetMethod converter, Class<T> clazz)
        {
            super(parent, path, defaultSupplier);
            this.converter = converter;
            this.clazz = clazz;
        }

        @Override
        protected T getRaw(Config config, List<String> path, Supplier<T> defaultSupplier)
        {
            return config.getEnumOrElse(path, clazz, converter, defaultSupplier);
        }
    }

    private static final Joiner LINE_JOINER = Joiner.on("\n");
    private static final Joiner DOT_JOINER = Joiner.on(".");
    private static final Splitter DOT_SPLITTER = Splitter.on(".");
    private static List<String> split(String path)
    {
        return Lists.newArrayList(DOT_SPLITTER.split(path));
    }
}
