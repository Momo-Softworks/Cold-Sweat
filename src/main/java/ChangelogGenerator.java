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

            boolean isFirstLine = true;
            for (int i = lastDashIndex + 1; i < lines.size(); i++)
            {
                line = lines.get(i);
                String lineText = line.trim();
                boolean isWarning = lineText.startsWith("!");
                boolean isImportant = lineText.startsWith("*");
                boolean isListElement = lineText.startsWith("-") || isImportant || isWarning;
                boolean isSectionTitle = !isListElement && lineText.endsWith(":");

                if (isFirstLine)
                {
                    line = "<span style=\"font-size: 18px; color: #ffffff; font-weight: bold;\">" + line + "</span>";
                    isFirstLine = false;
                }
                else if (isSectionTitle)
                {   line = "<u><strong style=\"font-size: 14px; color: #ffffff;\">" + line + "</strong></u>";
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

