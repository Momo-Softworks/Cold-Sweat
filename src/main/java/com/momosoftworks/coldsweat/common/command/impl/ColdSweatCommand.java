package com.momosoftworks.coldsweat.common.command.impl;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.command.BaseCommand;
import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.spec.ItemSettingsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.RegistryOps;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ColdSweatCommand extends BaseCommand
{
    public ColdSweatCommand(String name, int permissionLevel, boolean enabled) {
        super(name, permissionLevel, enabled);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> setExecution()
    {
        return builder
                .then(Commands.literal("dumpconfigs")
                    .executes(this::executeDumpConfigs)
                )
                .then(Commands.literal("reload")
                    .executes(this::executeReloadConfigs)
                );
    }

    private int executeReloadConfigs(CommandContext<CommandSourceStack> context)
    {
        try
        {
            Method openConfig = ConfigTracker.class.getDeclaredMethod("openConfig", ModConfig.class, Path.class);
            openConfig.setAccessible(true);
            for (Set<ModConfig> modConfigs : ConfigTracker.INSTANCE.configSets().values())
            {
                for (ModConfig config : modConfigs)
                {
                    if (config.getModId().equals(ColdSweat.MOD_ID))
                    {   openConfig.invoke(ConfigTracker.INSTANCE, config, FMLPaths.CONFIGDIR.get());
                    }
                }
            }
            ConfigLoadingHandler.loadConfigs(context.getSource().registryAccess());
            context.getSource().sendSuccess(() -> Component.translatable("commands.cold_sweat.reload.success"), true);
        }
        catch (Exception e)
        {
            context.getSource().sendFailure(Component.translatable("commands.cold_sweat.reload.failure", e.getMessage()));
            ColdSweat.LOGGER.error("Error reloading Cold Sweat configs", e);
        }
        return 1;
    }

    private int executeDumpConfigs(CommandContext<CommandSourceStack> context)
    {
        try
        {
            Path dumpPath = FMLPaths.CONFIGDIR.get().resolve("coldsweat").resolve("dump");
            // delete existing dump folder
            if (dumpPath.toFile().exists())
            {   FileUtils.deleteDirectory(dumpPath.toFile());
            }
            RegistryAccess registryAccess = context.getSource().registryAccess();
            File settingsFile = dumpPath.resolve("config_settings.txt").toFile();

            // Collect all non-object/array configs
            List<String> settingsLines = new ArrayList<>();

            ConfigSettings.CONFIG_SETTINGS.forEach((location, holder) ->
            {
                if (holder.getCodec() == null) return;
                JsonElement json = ((Codec<Object>) holder.getCodec()).encodeStart(RegistryOps.create(JsonOps.INSTANCE, registryAccess), holder.get(registryAccess)).result().orElse(null);
                if (json == null) return;
                if (json instanceof JsonObject || json instanceof JsonArray)
                {
                    File file = dumpPath.resolve("settings").resolve(location.getPath() + ".json").toFile();
                    file.getParentFile().mkdirs();
                    FileWriter writer;
                    try
                    {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        JsonElement je = JsonParser.parseString(json.toString());
                        String prettyJsonString = gson.toJson(je);
                        writer = new FileWriter(file);
                        writer.write(prettyJsonString);
                        writer.close();
                    }
                    catch (Exception e)
                    {   ColdSweat.LOGGER.error("Error dumping config {}", location, e);
                    }
                }
                else
                {
                    String line = location.toString() + " = " + json.getAsString();
                    settingsLines.add(line);
                }
            });

            // Write all settings lines at once
            if (!settingsLines.isEmpty())
            {
                settingsFile.getParentFile().mkdirs();
                try (FileWriter writer = new FileWriter(settingsFile))
                {
                    for (String line : settingsLines)
                    {   writer.write(line + System.lineSeparator());
                    }
                }
                catch (Exception e)
                {   ColdSweat.LOGGER.error("Error writing config settings file", e);
                }
            }

            Style fileLinkStyle = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, dumpPath.toAbsolutePath().toString()))
                                             .withColor(ChatFormatting.GRAY)
                                             .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("mco.notification.visitUrl.buttonText.default")));
            context.getSource().sendSuccess(() -> Component.translatable("commands.cold_sweat.dump.success", Component.literal(dumpPath.toString()).setStyle(fileLinkStyle)), true);
        }
        catch (Exception e)
        {
            context.getSource().sendFailure(Component.translatable("commands.cold_sweat.dump.failure", e.getMessage()));
            ColdSweat.LOGGER.error("Error dumping Cold Sweat configs", e);
        }
        return 1;
    }
}
