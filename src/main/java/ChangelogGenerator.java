import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ChangelogGenerator {
    private static final String COMMON_STYLE = """
            font-family: 'JetBrains Mono',monospace; 
            background-color: #212121; 
            color: #aaafb6;""";

    private static final String HEADER_STYLE = """
            color: #ffffff; 
            font-size: 20; 
            font-weight: bold;""";

    private static final String SECTION_STYLE = """
            color: #ffffff; 
            font-size: 16; 
            font-weight: bold;""";

    private static final String WARNING_STYLE = "color: orange;";
    private static final String URGENT_STYLE = "color: #e06c75; font-weight: bold;";  // Softer red color

    static class Item {
        String prefix; // "", "*", "!", "!!"
        String content;
        List<Item> children = new ArrayList<>();

        Item(String prefix, String content) {
            this.prefix = prefix;
            this.content = content;
        }
    }

    static class Section {
        String title;
        List<Item> items = new ArrayList<>();

        Section(String title) {
            this.title = title;
        }
    }

    public static void main(String[] args) {
        String inputFilePath = "changelog.txt";
        String outputFilePath = "formatted_changelog.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            List<String> allLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }

            // Find the last line of dashes
            int lastDashIndex = -1;
            for (int i = allLines.size() - 1; i >= 0; i--) {
                if (allLines.get(i).trim().matches("^-+$")) {
                    lastDashIndex = i;
                    break;
                }
            }

            // Get only the lines after the last dash line
            List<String> lines = new ArrayList<>(allLines.subList(lastDashIndex + 1, allLines.size()));

            // Find version number (assumed to be the first non-empty line)
            String version = "";
            for (String l : lines) {
                if (!l.trim().isEmpty()) {
                    version = l.trim();
                    break;
                }
            }

            if (version.isEmpty()) {
                System.err.println("No version number found in the changelog.");
                return;
            }

            // Parse sections
            List<Section> sections = parseSections(lines.subList(1, lines.size()));

            // Generate HTML
            generateHTML(writer, version, sections);

        } catch (IOException e) {
            System.err.println("Error processing the files: " + e.getMessage());
        }
    }

    private static List<Section> parseSections(List<String> lines) {
        List<Section> sections = new ArrayList<>();
        Section currentSection = new Section(""); // Default section for items before first section header
        sections.add(currentSection);

        // Stack to keep track of the current item hierarchy
        // Each entry is a pair of [Item, IndentLevel]
        List<Map.Entry<Item, Integer>> itemStack = new ArrayList<>();

        for (String line : lines) {
            // Calculate indentation level
            int indent = 0;
            while (indent < line.length() && line.charAt(indent) == ' ') {
                indent++;
            }

            String trimmed = line.trim();

            // Skip empty lines
            if (trimmed.isEmpty()) {
                continue;
            }

            // New section (ends with : and not indented and not a list item)
            if (trimmed.endsWith(":") && indent == 0 && !isListItem(trimmed)) {
                currentSection = new Section(trimmed);
                sections.add(currentSection);
                itemStack.clear(); // Reset item stack for new section
                continue;
            }

            // Parse the line into a new item
            Item newItem = parseItem(trimmed);

            // Determine where to add this item based on indentation
            if (indent == 0 || itemStack.isEmpty()) {
                // Top-level item
                currentSection.items.add(newItem);
                itemStack.clear();
                itemStack.add(new AbstractMap.SimpleEntry<>(newItem, indent));
            } else {
                // Find the parent item based on indentation
                int i = itemStack.size() - 1;
                while (i >= 0) {
                    if (itemStack.get(i).getValue() < indent) {
                        break;
                    }
                    i--;
                }

                if (i >= 0) {
                    // Found a parent
                    Item parent = itemStack.get(i).getKey();
                    parent.children.add(newItem);

                    // Remove all items in stack after the parent
                    while (itemStack.size() > i + 1) {
                        itemStack.remove(itemStack.size() - 1);
                    }
                } else {
                    // No parent found, add as top-level
                    currentSection.items.add(newItem);
                    itemStack.clear();
                }

                // Add the new item to the stack
                itemStack.add(new AbstractMap.SimpleEntry<>(newItem, indent));
            }
        }

        // Remove empty default section if it wasn't used
        if (sections.get(0).items.isEmpty()) {
            sections.remove(0);
        }

        return sections;
    }

    private static Item parseItem(String line) {
        // Identify item type by prefix
        if (line.startsWith("!!")) {
            return new Item("!!", line.substring(2).trim());
        } else if (line.startsWith("!")) {
            return new Item("!", line.substring(1).trim());
        } else if (line.startsWith("*")) {
            return new Item("*", line.substring(1).trim());
        } else if (line.startsWith("-")) {
            return new Item("-", line.substring(1).trim());
        } else {
            return new Item("", line);
        }
    }

    private static boolean isListItem(String line) {
        return line.startsWith("*") || line.startsWith("-") || line.startsWith("!!") || line.startsWith("!");
    }

    private static void generateHTML(BufferedWriter writer, String version, List<Section> sections)
    throws IOException {
        // Start document
        writer.write("<div style=\"" + COMMON_STYLE + "\">");

        // Version header
        writer.write(String.format("<span style=\"%s\">%s</span>",
                                   HEADER_STYLE, version));
        writer.write("<br><br>\n");

        // Write each section
        for (Section section : sections) {
            // Section header
            writer.write(String.format("<span style=\"%s\">%s</span>\n",
                                       SECTION_STYLE, section.title));

            writer.write("<ul style=\"font-size: 13;\">\n");

            // Write items recursively
            for (Item item : section.items) {
                writeItem(writer, item, 0);
            }

            writer.write("</ul>\n<br>\n");
        }

        // Close document
        writer.write("</div>");
    }

    private static void writeItem(BufferedWriter writer, Item item, int level) throws IOException {
        // Format the item based on its prefix
        switch (item.prefix) {
            case "*" -> writer.write(String.format("<li><span style=\"color: #ffffff; font-weight: bold;\">%s</span>", item.content));
            case "!" -> writer.write(String.format("<li style=\"%s\">! %s", WARNING_STYLE, item.content));
            case "!!" -> writer.write(String.format("<li style=\"%s\">!! %s", URGENT_STYLE, item.content));
            case "-" -> writer.write(String.format("<li>%s", item.content));
            default -> writer.write(String.format("<li>%s", item.content));
        }

        // If this item has children, start a new nested list
        if (!item.children.isEmpty()) {
            writer.write("\n<ul style=\"margin-left: " + (40 + (level * 10)) + "px;\">\n");

            for (Item child : item.children) {
                writeItem(writer, child, level + 1);
            }

            writer.write("</ul>\n");
        }

        writer.write("</li>\n");
    }
}