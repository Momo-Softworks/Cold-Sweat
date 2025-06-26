package com.momosoftworks.coldsweat.mixin;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.*;

@Mixin(ForgeConfigSpec.class)
public class MixinForgeConfigSpec
{
    @Shadow private Map<List<String>, String> levelComments;

    @Inject(method = "correct(Lcom/electronwill/nightconfig/core/UnmodifiableConfig;Lcom/electronwill/nightconfig/core/CommentedConfig;Ljava/util/LinkedList;Ljava/util/List;Lcom/electronwill/nightconfig/core/ConfigSpec$CorrectionListener;Lcom/electronwill/nightconfig/core/ConfigSpec$CorrectionListener;Z)I",
            at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void correct(UnmodifiableConfig spec, CommentedConfig config, LinkedList<String> parentPath, List<String> parentPathUnmodifiable, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener, boolean dryRun, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Integer> cir)
    {
        if (config instanceof FileConfig && ((FileConfig) config).getNioPath().toString().contains("coldsweat"))
        {
            int count = beautifulCorrect(spec, config, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
            cir.setReturnValue(count);
        }
    }

    private int beautifulCorrect(UnmodifiableConfig spec, CommentedConfig config, LinkedList<String> parentPath, List<String> parentPathUnmodifiable, ConfigSpec.CorrectionListener listener, ConfigSpec.CorrectionListener commentListener, boolean dryRun)
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
                    count += beautifulCorrect((Config)specValue, (CommentedConfig)configValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
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
                    count += beautifulCorrect((Config)specValue, newValue, parentPath, parentPathUnmodifiable, listener, commentListener, dryRun);
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
                ForgeConfigSpec.ValueSpec valueSpec = (ForgeConfigSpec.ValueSpec) specValue;

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
     * Checks if a comment change is just formatting (removing //v or similar)
     * Returns true if this is a formatting change that should be allowed
     */
    private boolean isCommentFormattingChange(String oldComment, String newComment)
    {
        // Check if either comment contains //v directive
        boolean oldHasDirective = oldComment != null && oldComment.contains("//v");
        boolean newHasDirective = newComment != null && newComment.contains("//v");

        // If only one has the directive, this could be a formatting change
        if (oldHasDirective != newHasDirective)
        {
            // Normalize both comments by removing drill_down directives for comparison
            String normalizedOld = normalizeCommentForComparison(oldComment);
            String normalizedNew = normalizeCommentForComparison(newComment);

            // If they're the same after normalization, this is just a formatting change
            return stringsMatchIgnoringNewlines(normalizedOld, normalizedNew);
        }

        // If both have or both don't have directives, use standard comparison
        return stringsMatchIgnoringNewlines(oldComment, newComment);
    }

    /**
     * Normalizes a comment by removing drill_down directives for comparison purposes only
     * This preserves the structure but allows comparison of semantic content
     */
    private String normalizeCommentForComparison(String comment)
    {
        if (comment == null)
        {   return "";
        }

        // Remove //v directive and clean up whitespace for comparison
        String normalized = comment.replace("//v", "").trim();

        // Handle the case where comment was only "# //v" or similar
        if (normalized.equals("#") || normalized.isEmpty())
        {   return "";
        }

        return normalized;
    }

    /**
     * Enhanced value testing that considers formatting equivalence
     */
    private boolean isValueCorrectWithFormatting(ForgeConfigSpec.ValueSpec valueSpec, Object configValue)
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
    private boolean isListFormattingEquivalent(ForgeConfigSpec.ValueSpec valueSpec, List<?> configValue)
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
}
