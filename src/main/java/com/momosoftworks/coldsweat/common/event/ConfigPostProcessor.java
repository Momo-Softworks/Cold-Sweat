package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.vanilla.ServerConfigsLoadedEvent;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
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
    public static void onConfigLoad(ModConfigEvent.Loading event)
    {
        if (event.getConfig().getModId().equals(ColdSweat.MOD_ID))
        {   formatConfig(event.getConfig().getFullPath(), 10);
        }
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event)
    {
        if (event.getConfig().getModId().equals(ColdSweat.MOD_ID))
        {   formatConfig(event.getConfig().getFullPath(), 10);
        }
    }

    @SubscribeEvent
    public static void onConfigUnload(ModConfigEvent.Unloading event)
    {
        if (event.getConfig().getModId().equals(ColdSweat.MOD_ID))
        {   formatConfig(event.getConfig().getFullPath(), 10);
        }
    }

    @Mod.EventBusSubscriber
    public static class CommonEvents
    {
        @SubscribeEvent
        public static void onServerStarted(ServerConfigsLoadedEvent event)
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
        {   // Check if this file needs processing
            List<String> lines = Files.readAllLines(configFile);
            boolean containsDrillDown = lines.stream()
                    .anyMatch(line -> line.trim().startsWith("#") && line.contains("//v"));

            boolean containsVerticalArrays = containsVerticallyFormattedArrays(lines);

            if (containsDrillDown || containsVerticalArrays)
            {   processConfigFormatting(configFile);
            }
        }
        catch (IOException e)
        {   ColdSweat.LOGGER.error("Failed to check config file: {}", configFile, e);
        }
    }

    private static boolean containsVerticallyFormattedArrays(List<String> lines)
    {
        for (int i = 0; i < lines.size() - 2; i++)
        {
            String line = lines.get(i).trim();
            // Look for pattern: key = [
            if (line.contains("=") && line.trim().endsWith("= ["))
            {
                // Check if next lines are indented (indicating vertical format)
                if (i + 1 < lines.size())
                {
                    String nextLine = lines.get(i + 1);
                    if (nextLine.startsWith("    ") || nextLine.startsWith("\t"))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void processConfigFormatting(Path configFile)
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
                if (trimmed.startsWith("#") && trimmed.contains("//v"))
                {   nextArrayShouldDrillDown = true;
                    processedLines.add(line);
                    continue;
                }

                // Skip empty lines and other comments
                if (trimmed.isEmpty() || (trimmed.startsWith("#") && !trimmed.contains("//v")))
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
                // Check if this line starts a vertical array without //v directive (only if not in drill-down mode)
                else if (line.contains("=") && line.trim().endsWith("= ["))
                {
                    VerticalArrayParseResult verticalResult = parseVerticalArray(lines, i);
                    if (verticalResult.isVerticalArray)
                    {
                        // Convert to horizontal format since no //v directive preceded it
                        String horizontalFormat = formatArrayHorizontally(verticalResult.keyPart, verticalResult.elements, getIndentation(line));
                        processedLines.add(horizontalFormat);
                        i = verticalResult.endIndex;
                        fileModified = true;
                    }
                    else
                    {
                        processedLines.add(line);
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

    private static class VerticalArrayParseResult
    {
        boolean isVerticalArray;
        String keyPart;
        List<String> elements;
        int endIndex;

        VerticalArrayParseResult(boolean isVerticalArray, String keyPart, List<String> elements, int endIndex)
        {   this.isVerticalArray = isVerticalArray;
            this.keyPart = keyPart;
            this.elements = elements;
            this.endIndex = endIndex;
        }
    }

    private static VerticalArrayParseResult parseVerticalArray(List<String> lines, int startIndex)
    {
        String firstLine = lines.get(startIndex);
        if (!firstLine.trim().endsWith("= ["))
        {
            return new VerticalArrayParseResult(false, "", new ArrayList<>(), startIndex);
        }

        String keyPart = firstLine.substring(0, firstLine.lastIndexOf("= [")).trim() + " =";
        List<String> elements = new ArrayList<>();
        int currentIndex = startIndex + 1;

        // Parse elements until we find the closing bracket
        while (currentIndex < lines.size())
        {
            String line = lines.get(currentIndex);
            String trimmed = line.trim();

            if (trimmed.equals("]"))
            {
                // Found the end
                return new VerticalArrayParseResult(true, keyPart, elements, currentIndex);
            }
            else if (line.startsWith("    ") || line.startsWith("\t"))
            {
                // This is an indented element
                String element = trimmed;
                if (element.endsWith(","))
                {
                    element = element.substring(0, element.length() - 1);
                }
                elements.add(element);
            }
            else
            {
                // Not a vertical array format
                return new VerticalArrayParseResult(false, "", new ArrayList<>(), startIndex);
            }

            currentIndex++;
        }

        return new VerticalArrayParseResult(false, "", new ArrayList<>(), startIndex);
    }

    private static String formatArrayHorizontally(String keyPart, List<String> elements, String indentation)
    {
        StringBuilder result = new StringBuilder();
        result.append(indentation).append(keyPart).append(" [");

        for (int i = 0; i < elements.size(); i++)
        {
            result.append(elements.get(i));
            if (i < elements.size() - 1)
            {
                result.append(", ");
            }
        }

        result.append("]");
        return result.toString();
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