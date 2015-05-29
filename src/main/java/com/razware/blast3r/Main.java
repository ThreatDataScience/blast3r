package com.razware.blast3r;

import com.esotericsoftware.minlog.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.razware.blast3r.models.Target;
import com.razware.blast3r.system.Config;
import org.apache.commons.io.FileUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import strikeapi.Torrent;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Main {

    public static final String NAME = "blast3r";
    public static final int VERSION = 0;
    public static final String ART = "___.   .__                   __ ________        \n" +
            "\\_ |__ |  | _____    _______/  |\\_____  \\______ \n" +
            " | __ \\|  | \\__  \\  /  ___/\\   __\\_(__  <_  __ \\\n" +
            " | \\_\\ \\  |__/ __ \\_\\___ \\  |  | /       \\  | \\/\n" +
            " |___  /____(____  /____  > |__|/______  /__|   \n" +
            "     \\/          \\/     \\/             \\/       ";
    public static OS os = OS.getOS();
    public static Blast3r blast3r;
    private static Config config = new Config();
    private static CmdLineParser cmdLineParser;
    private static String configPath = "config.json";
    private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    public static void clearScreen() {
        Ansi ansi = new Ansi();
        ansi.eraseScreen();
    }

    public static void main(String[] args) throws Exception {
        AnsiConsole.systemInstall();
        clearScreen();
        printBanner();
        Log.setLogger(new MyLog());
        Log.set(getConfig().getLogLevel().getLevel());
        Runtime.getRuntime().addShutdownHook(new ShutdownHook("shutdown-hook"));
        if (!OS.isSupported(os)) {
            System.out.println(os.name() + " is not supported as of version " + VERSION);
        }
        loadConfig();
        cmdLineParser = new CmdLineParser(getConfig());
        try {
            getCmdLineParser().parseArgument(args);
        } catch (CmdLineException e) {
            Log.error(e.getMessage());
            printUsage();
            exit(1);
        }
        try {
            getConfig().init();
        } catch (NullPointerException e) {
            Log.error(e.getMessage());
            exit(1);
        }
        Log.set(config.getLogLevel().getLevel());
        if (getConfig().isHelp()) {
            printUsage();
            exit(0);
        }
        Log.trace("starting");
        blast3r = new Blast3r();
        List<Target> targets = new ArrayList<Target>();
        if (config.loadTargetsFromFiles) {
            Log.trace("loading targets from provided file list...");
            for (String filename : config.targets) {
                try {
                    Target target = gson.fromJson(FileUtils.readFileToString(new File(config.targetDirectory + filename + ".json")), Target.class);
                    targets.add(target);
                    Log.debug("loaded target \"" + config.targetDirectory + filename + ".json" + "\":\n" + getGson().toJson(target));
                } catch (IOException e) {
                    Log.error("target file " + filename + " does not exist...");
                }
            }
        }
        for (Target target : targets) {
            File dir = new File(config.dataDirectory + target.name + "/");
            FileUtils.forceMkdir(dir);
            List<Torrent> torrents = new ArrayList<Torrent>();
            if (target.hash) {
                Log.info("looking up " + target.query);
                torrents = Arrays.asList(blast3r.info(new String[]{target.query}));
            } else {
                Log.info("searching for " + target.query);
                torrents = blast3r.search(target);
            }
            Log.info("(" + torrents.size() + ") torrents found");
            for (Torrent torrent : torrents) {
                List<String> peers = blast3r.getPeers(torrent);
                for (String peer : peers) {
                    torrent.peers.add(peer);
                    File file = new File(config.dataDirectory + target.name + "/" + torrent.torrent_hash + ".json");
                    if (file.exists()) {
                        Torrent torrent1 = gson.fromJson(FileUtils.readFileToString(file), Torrent.class);
                        for (String p : torrent1.peers) {
                            torrent.peers.add(p);
                        }
                        file.delete();
                    }
                    file.createNewFile();
                    FileUtils.writeStringToFile(file, getGson().toJson(torrent));
                }
            }
            target.torrents.addAll(torrents);
        }
    }

    public static void loadConfig() {
        File file = new File(getConfigPath());
        if (file.exists()) {
            try {
                Log.debug("loading config file (\"" + getConfigPath() + "\")");
                config = getGson().fromJson(FileUtils.readFileToString(file), Config.class);
            } catch (IOException e) {
                Log.error("failed to load config (now using defaults)", e);
                config = new Config();
            }
        } else {
            Log.debug("configuration file does not exist");
            config = new Config();
        }
    }

    public static void saveConfig() {
        File file = new File(configPath);
        if (file.exists()) {
            Log.debug("config file extists, backing up...");
            File backup = new File(configPath.concat(".bk"));
            if (backup.exists()) {
                Log.debug("config backup exists, deleting...");
                backup.delete();
            }
            Log.debug("renaming existing config to \"" + backup.getAbsolutePath() + "\"...");
            file.renameTo(backup);
        }
        Log.debug("saving config...");
        try {
            FileUtils.writeStringToFile(new File(configPath), getGson().toJson(config));
        } catch (IOException e) {
            Log.error("unable to save config at \"" + file.getAbsolutePath() + "\"", e);
        }
        Log.info("saved config at \"" + file.getAbsolutePath() + "\"");
    }

    public static void printUsage() {
        System.out.println("Options: \n");
        (new CmdLineParser(new Config())).printUsage(System.out);
    }

    public static void exit(int x) {
        Log.trace("shutting down");
        System.exit(x);
    }

    public static Gson getGson() {
        return gson;
    }

    public static Config getConfig() {
        return config;
    }

    public static CmdLineParser getCmdLineParser() {
        return cmdLineParser;
    }

    public static String getConfigPath() {
        return configPath;
    }

    public static void printBanner() {
        String banner = Main.ART + "\nversion " + Main.VERSION + "\n\t(C) 2015 Andrew Breksa [abreksa4@gmail.com]\n";
        System.out.println(banner);
    }


    public enum OS {
        Windows, Mac, Unix, Solaris;
        private static String property = System.getProperty("os.name").toLowerCase();

        public static OS getOS() {
            if (isMac()) {
                return Mac;
            }
            if (isUnix()) {
                return Unix;
            }
            if (isWindows()) {
                return Windows;
            }
            if (isSolaris()) {
                return Solaris;
            }
            return null;
        }

        public static boolean isSupported(OS os) {
            switch (os) {
                case Windows:
                    return true;
                case Mac:
                    return true;
                case Unix:
                    return true;
                case Solaris:
                    return false;
                default:
                    return false;
            }
        }

        public static boolean isWindows() {
            return (property.indexOf("win") >= 0);
        }

        public static boolean isMac() {
            return (property.indexOf("mac") >= 0);
        }

        public static boolean isUnix() {
            return (property.indexOf("nix") >= 0 || property.indexOf("nux") >= 0 || property.indexOf("aix") > 0);
        }

        public static boolean isSolaris() {
            return (property.indexOf("sunos") >= 0);
        }
    }

    static class ShutdownHook extends Thread {
        private String threadName;

        ShutdownHook(String name) {
            threadName = name;
            Log.trace("creating thread " + threadName);
        }

        public void start() {
            Log.trace("starting " + threadName);
            if (Main.getConfig().isSaveConfig()) {
                Main.saveConfig();
            }
            AnsiConsole.systemUninstall();
        }

    }

    public static class MyLog extends Log.Logger {
        public void log(int level, String category, String message, Throwable ex) {
            StringBuilder builder = new StringBuilder();
            Ansi ansi = new Ansi();
            builder.append("[" + new Date() + "]");
            switch (level) {
                case Log.LEVEL_ERROR:
                    builder.append("@|red  ERROR|@:");
                    break;
                case Log.LEVEL_WARN:
                    builder.append("@|green  WARN|@:");
                    break;
                case Log.LEVEL_INFO:
                    builder.append("@|blue  INFO|@:");
                    break;
                case Log.LEVEL_DEBUG:
                    builder.append("@|yellow  DEBUG|@:");
                    break;
                case Log.LEVEL_TRACE:
                    builder.append(" TRACE:");
                    break;
            }
            builder.append(" [" + Thread.currentThread().getName() + "]");
            builder.append(" [");
            if (category != null) {
                builder.append(category);
            } else {
                builder.append("*");
            }
            builder.append("]");
            builder.append(" " + message);
            if (ex != null) {
                StringWriter writer = new StringWriter();
                ex.printStackTrace(new PrintWriter(writer));
                builder.append('\n');
                builder.append(writer.toString().trim());
            }
            System.out.println(ansi.render(builder.toString()));
            //System.out.println(builder);
            if (Main.getConfig().isFileLog()) {
                File file = new File(Main.getConfig().getLogFile());
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        Log.error(e.getMessage() + "\n" + e.getStackTrace());
                    }
                }
                try {
                    FileUtils.writeStringToFile(file, builder.toString() + "\n", true);
                } catch (IOException e) {
                    Log.error(e.getMessage() + "\n" + e.getStackTrace());
                }
            }
        }

        public enum LogLevel {
            NONE(6),
            ERROR(5),
            WARN(4),
            INFO(3),
            DEBUG(2),
            TRACE(1);

            private int level;

            LogLevel(int level) {
                this.level = level;
            }

            public int getLevel() {
                return level;
            }
        }
    }
}
