import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChangelogGenerator
{
    public static void main(String[] args)
    {
        String inputFilePath = "changelog.txt";
        String outputFilePath = "formatted_changelog.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath)))
        {

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null)
            {   lines.add(line);
            }

            int lastDashIndex = -1;
            for (int i = lines.size() - 1; i >= 0; i--)
            {
                if (lines.get(i).trim().matches("^-+$"))
                {   // Check for line full of dashes
                    lastDashIndex = i;
                    break;
                }
            }

            writer.write("<div style=\"background-color: #212121; color: #aaafb6; font-family: 'JetBrains Mono', monospace; font-size: 9.8pt;\">");
            writer.write("<pre>");

            for (int i = lastDashIndex + 1; i < lines.size(); i++)
            {
                line = lines.get(i);
                String lineText = line.trim();
                boolean isMajorWarning = lineText.startsWith("!!");
                boolean isWarning = !isMajorWarning && lineText.startsWith("!");
                boolean isImportant = lineText.startsWith("*");
                boolean isListElement = lineText.startsWith("-") || isImportant || isMajorWarning || isWarning;
                boolean isSectionTitle = !isListElement && lineText.endsWith(":");
                boolean isGroupTitle = !isListElement && lineText.endsWith(":-");
                boolean isTitleLine = !isListElement && !isSectionTitle && !isGroupTitle;

                if (isTitleLine)
                {   line = "<span style=\"font-size: 18px; color: #ffffff; font-weight: bold;\">" + line + "</span>";
                }
                else if (isGroupTitle)
                {   line = "<strong style=\"font-size: 13px; color: #1A32CD;\"><u>" + line.substring(0, line.length() - 2) + "</u></strong>";
                }
                else if (isSectionTitle)
                {   line = "<u><strong style=\"font-size: 14px; color: #ffffff;\">" + line + "</strong></u>";
                }
                else if (isMajorWarning)
                {   line = "<span style=\"color: #ff4d49;\">" + line + "</span>";
                }
                else if (isWarning)
                {   line = "<span style=\"color: #ff9900;\">" + line + "</span>";
                }
                else if (isImportant)
                {   line = "<strong style=\"color: #ffffff;\">" + line + "</strong>";
                }

                writer.write(line + "<br />");
            }

            writer.write("</pre></div>");
            writer.write("</body></html>");

        }
        catch (IOException e)
        {   System.err.println("Error processing the files: " + e.getMessage());
        }
    }
}

