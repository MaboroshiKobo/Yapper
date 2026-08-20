package org.maboroshi.yapper.manager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.util.Log;

public class ChatLogger {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Yapper plugin;
    private final File logsFolder;
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private final BukkitTask flushTask;

    public ChatLogger(Yapper plugin) {
        this.plugin = plugin;
        this.logsFolder = new File(plugin.getDataFolder(), "logs");

        this.flushTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::flush, 100L, 100L);
    }

    public void log(Component component) {
        log(PlainTextComponentSerializer.plainText().serialize(component));
    }

    public void log(String message) {
        if (!plugin.getConfigManager().getMainConfig().logging.enabled) return;
        if (message == null || message.isBlank()) return;

        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        queue.add("[" + timestamp + "] " + message);
    }

    public void flush() {
        if (queue.isEmpty()) return;

        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }

        File logFile = new File(logsFolder, LocalDate.now().format(DATE_FORMATTER) + ".log");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, StandardCharsets.UTF_8, true))) {
            String line;
            while ((line = queue.poll()) != null) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            Log.warn("Failed to write to chat log: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (flushTask != null && !flushTask.isCancelled()) {
            flushTask.cancel();
        }
        flush();
    }
}
