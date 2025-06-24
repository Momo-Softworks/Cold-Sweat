package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigPostProcessor
{
    // Files currently being processed (prevents saves during formatting)
    public static final Set<String> WRITING_CONFIGS = Collections.synchronizedSet(new HashSet<>());

    @SubscribeEvent
    public static void onConfigLoad(ModConfig.Loading event)
    {
        if (event.getConfig().getModId().equals(ColdSweat.MOD_ID))
        {   formatConfig(event.getConfig().getFullPath(), 10);
        }
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfig.Reloading event)
    {
        if (event.getConfig().getModId().equals(ColdSweat.MOD_ID))
        {   formatConfig(event.getConfig().getFullPath(), 10);
        }
    }

    @Mod.EventBusSubscriber
    public static class CommonEvents
    {
        @SubscribeEvent
        public static void onServerStarted(FMLServerStartedEvent event)
        {   // Format all configs after server starts
            TaskScheduler.schedule(() ->
            {
                try
                {   Path configDir = FMLPaths.CONFIGDIR.get().resolve("coldsweat");
                    if (Files.exists(configDir))
                    {   Files.walk(configDir)
                            .filter(path -> path.toString().endsWith(".toml"))
                            .forEach(ConfigPostProcessor::formatConfigIfNeeded);
                    }
                }
                catch (IOException e)
                {   ColdSweat.LOGGER.error("Failed to format configs", e);
                }
            }, 20);
        }
    }

    public static void formatConfig(Path configPath, int delay)
    {
        TaskScheduler.schedule(() -> formatConfigIfNeeded(configPath), delay);
    }

    public static void formatConfigIfNeeded(Path configFile)
    {
        String filePath = configFile.toAbsolutePath().toString();

        // Prevent concurrent processing
        if (WRITING_CONFIGS.contains(filePath))
        {   return;
        }

        try
        {   // Quick check: does this file contain drill_down directives?
            List<String> lines = Files.readAllLines(configFile);
            boolean containsDrillDown = lines.stream()
                    .anyMatch(line -> line.trim().startsWith("#") && line.contains("//drill_down"));

            if (containsDrillDown)
            {   processDrillDownComments(configFile);
            }
        }
        catch (IOException e)
        {   ColdSweat.LOGGER.error("Failed to check config file: {}", configFile, e);
        }
    }

    private static void processDrillDownComments(Path configFile)
    {
        String filePath = configFile.toAbsolutePath().toString();

        // Mark as being processed to prevent saves
        WRITING_CONFIGS.add(filePath);
        TaskScheduler.schedule(() -> WRITING_CONFIGS.remove(filePath), 10);

        try
        {   List<String> lines = Files.readAllLines(configFile);
            List<String> processedLines = new ArrayList<>();
            boolean nextArrayShouldDrillDown = false;
            boolean fileModified = false;

            for (int i = 0; i < lines.size(); i++)
            {   String line = lines.get(i);
                String trimmed = line.trim();

                // Check for drill_down comment
                if (trimmed.startsWith("#") && trimmed.contains("//drill_down"))
                {   nextArrayShouldDrillDown = true;
                    fileModified = true;

                    // Remove "//drill_down" from the comment while preserving indentation
                    String originalIndent = getIndentation(line);
                    String commentContent = line.substring(originalIndent.length());
                    String cleanedContent = commentContent.replace("//drill_down", "").trim();

                    // Only add the comment if there's still content after removing //drill_down
                    if (!cleanedContent.equals("#") && !cleanedContent.isEmpty())
                    {   processedLines.add(originalIndent + cleanedContent);
                    }
                    continue;
                }

                // Skip empty lines and other comments
                if (trimmed.isEmpty() || (trimmed.startsWith("#") && !trimmed.contains("//drill_down")))
                {   processedLines.add(line);
                    continue;
                }

                // Process array formatting if we're in drill_down mode and this line contains an assignment
                if (nextArrayShouldDrillDown && line.contains("="))
                {   ArrayParseResult result = parseCompleteArray(lines, i);

                    if (result.isArray)
                    {   // Format the array with drill-down
                        List<String> formattedLines = formatArrayWithNewlines(result.keyPart, result.arrayContent, getIndentation(line));
                        processedLines.addAll(formattedLines);

                        // Skip the lines we just processed
                        i = result.endIndex;
                        nextArrayShouldDrillDown = false;
                        fileModified = true;
                    }
                    else
                    {   processedLines.add(line);
                        nextArrayShouldDrillDown = false;
                    }
                }
                else
                {   processedLines.add(line);
                }
            }

            // Only write the file if we made modifications
            if (fileModified)
            {   Files.write(configFile, processedLines);
                ColdSweat.LOGGER.debug("Formatted config file: {}", configFile.getFileName());
            }
        }
        catch (IOException e)
        {   ColdSweat.LOGGER.error("Failed to process config file: {}", configFile, e);
        }
    }

    private static class ArrayParseResult
    {
        boolean isArray;
        String keyPart;
        String arrayContent;
        int endIndex;

        ArrayParseResult(boolean isArray, String keyPart, String arrayContent, int endIndex)
        {   this.isArray = isArray;
            this.keyPart = keyPart;
            this.arrayContent = arrayContent;
            this.endIndex = endIndex;
        }
    }

    private static ArrayParseResult parseCompleteArray(List<String> lines, int startIndex)
    {
        String firstLine = lines.get(startIndex);
        int equalsIndex = firstLine.indexOf('=');

        if (equalsIndex == -1)
        {   return new ArrayParseResult(false, "", "", startIndex);
        }

        String keyPart = firstLine.substring(0, equalsIndex + 1).trim();
        String valuePart = firstLine.substring(equalsIndex + 1).trim();

        if (!valuePart.startsWith("["))
        {   return new ArrayParseResult(false, "", "", startIndex);
        }

        StringBuilder arrayContent = new StringBuilder();
        int currentIndex = startIndex;
        int bracketCount = 0;
        boolean inString = false;
        boolean escapeNext = false;

        for (int i = 0; i < valuePart.length(); i++)
        {   char c = valuePart.charAt(i);
            arrayContent.append(c);

            if (escapeNext)
            {   escapeNext = false;
                continue;
            }
            if (c == '\\')
            {   escapeNext = true;
                continue;
            }
            if (c == '"' && !escapeNext)
            {   inString = !inString;
                continue;
            }
            if (!inString)
            {   if (c == '[') bracketCount++;
                else if (c == ']') bracketCount--;
            }
        }

        if (bracketCount == 0)
        {   return new ArrayParseResult(true, keyPart, arrayContent.toString(), startIndex);
        }

        currentIndex++;
        while (currentIndex < lines.size() && bracketCount > 0)
        {   String line = lines.get(currentIndex);
            arrayContent.append(" ").append(line.trim());

            for (char c : line.toCharArray())
            {
                if (escapeNext)
                {   escapeNext = false;
                    continue;
                }
                if (c == '\\')
                {   escapeNext = true;
                    continue;
                }
                if (c == '"' && !escapeNext)
                {   inString = !inString;
                    continue;
                }
                if (!inString)
                {   if (c == '[') bracketCount++;
                else if (c == ']') bracketCount--;
                }
            }
            currentIndex++;
        }

        return new ArrayParseResult(true, keyPart, arrayContent.toString(), currentIndex - 1);
    }

    private static List<String> formatArrayWithNewlines(String keyPart, String arrayContent, String indentation)
    {
        List<String> result = new ArrayList<>();
        List<String> elements = parseTopLevelArrayElements(arrayContent);

        if (elements.isEmpty())
        {   result.add(indentation + keyPart + " " + arrayContent);
            return result;
        }

        result.add(indentation + keyPart + " [");

        for (int i = 0; i < elements.size(); i++)
        {   String element = elements.get(i).trim();
            String line = indentation + "    " + element;

            if (i < elements.size() - 1)
            {   line += ",";
            }

            result.add(line);
        }

        result.add(indentation + "]");
        return result;
    }

    private static List<String> parseTopLevelArrayElements(String arrayString)
    {
        List<String> elements = new ArrayList<>();
        String content = arrayString.trim();
        if (content.startsWith("[")) content = content.substring(1);
        if (content.endsWith("]")) content = content.substring(0, content.length() - 1);

        if (content.trim().isEmpty())
        {   return elements;
        }

        int bracketDepth = 0;
        int braceDepth = 0;
        boolean inString = false;
        boolean escapeNext = false;
        StringBuilder currentElement = new StringBuilder();

        for (int i = 0; i < content.length(); i++)
        {
            char c = content.charAt(i);

            if (escapeNext)
            {   currentElement.append(c);
                escapeNext = false;
                continue;
            }

            if (c == '\\')
            {   currentElement.append(c);
                escapeNext = true;
                continue;
            }

            if (c == '"')
            {   inString = !inString;
                currentElement.append(c);
                continue;
            }

            if (!inString)
            {
                if (c == '[')
                {   bracketDepth++;
                    currentElement.append(c);
                }
                else if (c == ']')
                {   bracketDepth--;
                    currentElement.append(c);
                }
                else if (c == '{')
                {   braceDepth++;
                    currentElement.append(c);
                }
                else if (c == '}')
                {   braceDepth--;
                    currentElement.append(c);
                }
                else if (c == ',' && bracketDepth == 0 && braceDepth == 0)
                {   elements.add(currentElement.toString());
                    currentElement = new StringBuilder();
                }
                else
                {   currentElement.append(c);
                }
            }
            else
            {   currentElement.append(c);
            }
        }

        if (currentElement.length() > 0)
        {   elements.add(currentElement.toString());
        }

        return elements;
    }

    private static String getIndentation(String line)
    {
        int firstNonSpace = 0;
        while (firstNonSpace < line.length() && Character.isWhitespace(line.charAt(firstNonSpace)))
        {   firstNonSpace++;
        }
        return line.substring(0, firstNonSpace);
    }
}