/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

package com.razware.blast3r;

import com.esotericsoftware.minlog.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.razware.blast3r.models.Target;
import com.razware.blast3r.strikeapi.Torrent;
import com.razware.blast3r.system.Config;
import com.razware.blast3r.system.MyLog;
import com.razware.blast3r.system.OS;
import com.razware.blast3r.system.ShutdownHook;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.*;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Main.
 */
public class Main {

    public static final String NAME = "blast3r";
    public static final int VERSION = 0;
    public static final String ART = "___.   .__                   __ ________        \n" +
            "\\_ |__ |  | _____    _______/  |\\_____  \\______ \n" +
            " | __ \\|  | \\__  \\  /  ___/\\   __\\_(__  <_  __ \\\n" +
            " | \\_\\ \\  |__/ __ \\_\\___ \\  |  | /       \\  | \\/\n" +
            " |___  /____(____  /____  > |__|/______  /__|   \n" +
            "     \\/          \\/     \\/             \\/       ";
    public static final String NOTES =
            "This version supports searching strike for torrent information, and uses strike \n" +
                    "and torrage to download the required torrent files in order to discover peers  \n" +
                    "with ttorrent. If that fails (either no torrent file can be downloaded or is \n" +
                    "invalid), nmap is used.";
    public static final String SUMMARY =
            "blast3r is a tool for finding torrents via json defined \"targets\" that contain \n" +
                    "a query (or info hash), and optional category and subcategory strings. The gathered\n" +
                    "information is saved in json files in the --data-directory. \n" +
                    "When blast3r looks up peers for a torrent, if a json file exists for it\n" +
                    "already, those peers are loaded, and added if unique to the new list.";
    public static final String USAGE_INFO = "Targets are defined as follows:\n" +
            "In xubuntu14.04.json (under targets/):\n" +
            "    {\n" +
            "    \"name\" : \"xubuntu 14.04\",\n" +
            "    \"query\" : \"xubuntu 14.04\",\n" +
            "    \"hash\" : \"false\",\n" +
            "    \"category\" : \"\",\n" +
            "    \"subcategory\" : \"\"\n" +
            "    }\n" +
            "\n" +
            "To look up all torrents on Strike with that query and fetch peer information from them:\n" +
            "    java -jar blast3r.jar --peers xubuntu14.04";
    public static final String DISCLAIMER = "blast3r uses various open source technologies and data sources, including:\n" +
            "\tttorrent   (http://mpetazzoni.github.io/ttorrent/),\n" +
            "\tStrike API (https://getstrike.net/api/)\n" +
            "\tand requires nmap for some optional functionality (https://nmap.org/).\n";

    public static final OS os = OS.getOS();
    public static Blast3r blast3r;
    private static Config config = new Config();
    private static String configPath = "config.json";
    private static CmdLineParser cmdLineParser;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public static void clearScreen() {
        Ansi ansi = new Ansi();
        ansi.eraseScreen();
    }

    public static void main(String[] args) throws Exception {
        //Application setup
        AnsiConsole.systemInstall();
        clearScreen();
        printBanner();
        Log.setLogger(new MyLog());
        Log.set(getConfig().getLogLevel().getLevel());
        Runtime.getRuntime().addShutdownHook(new ShutdownHook("shutdown-hook"));

        //Detect supported OS
        if (!OS.isSupported(os)) {
            System.out.println(os.name() + " is not supported as of version " + VERSION);
        }

        //Load config from disk
        loadConfig();

        //Parse command line params
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

        //Set the log level
        Log.set(config.getLogLevel().getLevel());

        //If is help
        if (getConfig().isHelp()) {
            printUsage();
            exit(0);
        }

        //If there are no targets whatsoever
        if (config.targets.isEmpty() && config.queries.isEmpty() && config.hashes.isEmpty()) {
            Log.error("you must provide at least one hash, target, or query");
            printUsage();
            exit(1);
        }

        //Being the application
        Log.trace("starting");

        //Initialize the log4j appenders
        initLog4J();
        //Define instance
        blast3r = new Blast3r();

        //Gather targets
        List<Target> targets = new ArrayList<Target>();

        if (!config.queries.isEmpty()) {
            for (String query : config.queries) {
                Target target = new Target();
                target.query = query;
                target.name = query;
                targets.add(target);
            }
        }
        if (!config.hashes.isEmpty()) {
            for (String hash : config.hashes) {
                Target target = new Target();
                target.query = hash;
                target.name = hash;
                target.hash = true;
                targets.add(target);
            }
        }
        if (config.loadTargetsFromFiles) {
            Log.trace("loading targets from provided file list...");
            for (String filename : config.targets) {
                try {
                    Target target = gson.fromJson(FileUtils.readFileToString(new File(config.targetDirectory + filename + ".json")), Target.class);
                    targets.add(target);
                    Log.debug("loaded target \"" + config.targetDirectory + filename + ".json" + "\":\n" + getGson().toJson(target));
                } catch (IOException e) {
                    Log.error("target file " + filename + " does not exist: " + e.getMessage(), e);
                }
            }
        }
        boolean x = true;
        while (x) {
            if (!Main.config.loop) {
                x = false;
            }
            //for each target
            for (Target target : targets) {

                //Create the target's data directory
                File dir = new File(config.dataDirectory + target.name + "/");
                FileUtils.forceMkdir(dir);

                //Get a list of Torrent objects
                List<Torrent> torrents;
                //If target is a hash, run a single lookup
                if (target.hash) {
                    try {
                        Log.info("looking up \"" + target.query + "\"");
                        torrents = blast3r.info(new String[]{target.query});
                    } catch (Exception e) {
                        Log.error("unable to get info for " + target.query + ": " + e.getMessage(), e);
                        continue;
                    }
                } else {
                    //Else it's a search string, and we want to look it up
                    Log.info("searching for \"" + target.query + "\"");
                    torrents = blast3r.search(target);
                }
                Log.info("(" + torrents.size() + ") torrents found");

                //For each of the torrents,
                for (Torrent torrent : torrents) {
                    if (config.getPeers) {
                        List<String> peers = blast3r.getPeers(torrent);
                        for (String peer : peers) {
                            torrent.getPeers().add(peer);
                        }
                    }

                    //Write the torrent json to disk
                    File file = new File(config.dataDirectory + target.name + "/" + torrent.getTorrent_hash() + ".json");
                    //If we;ve already seen it
                    if (file.exists()) {
                        Torrent torrent1 = gson.fromJson(FileUtils.readFileToString(file), Torrent.class);
                        //Load the peers from the exisitng json and save the new data
                        if (torrent1 != null && torrent1.getPeers() != null && torrent1.getPeers().size() > 0) {
                            for (String p : torrent1.getPeers()) {
                                torrent.getPeers().add(p);
                            }
                            Log.info("loaded (" + torrent1.getPeers().size() + ") existing peers records for \"" + torrent.getTorrent_hash() + "\"");
                        }
                        file.delete();
                    }
                    file.createNewFile();
                    FileUtils.writeStringToFile(file, getGson().toJson(torrent));
                }
                //Save all torrents to the target for single file export
                //target.torrents.addAll(torrents);
                //Need to impliment
            }
        }
    }

    public static void loadConfig() {
        File file = new File(getConfigPath());
        if (file.exists()) {
            try {
                Log.debug("loading config file (\"" + getConfigPath() + "\")");
                config = getGson().fromJson(FileUtils.readFileToString(file), Config.class);
            } catch (IOException e) {
                Log.error("failed to load config (now using defaults): " + e.getMessage(), e);
                config = new Config();
            }
        } else {
            Log.debug("configuration file does not exist");
            config = new Config();
        }
    }

    private static void initLog4J() {
        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.ERROR);
        console.activateOptions();
        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(console);
        if (config.isFileLog()) {
            FileAppender fileLog = new FileAppender();
            fileLog.setName("FileLogger");
            fileLog.setFile(config.getLogFile());
            fileLog.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
            fileLog.setThreshold(Level.INFO);
            fileLog.setAppend(true);
            fileLog.activateOptions();

            //add appender to any Logger (here is root)
            Logger.getRootLogger().addAppender(fileLog);
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
            Log.error("unable to save config at \"" + file.getAbsolutePath() + "\": " + e.getMessage(), e);
        }
        Log.info("saved config at \"" + file.getAbsolutePath() + "\"");
    }

    /**
     * Print usage.
     */
    public static void printUsage() {
        System.out.println(SUMMARY + "\n");
        if (config.info) {
            System.out.println(USAGE_INFO + "\n");
            System.out.println("Examples: \n");
            System.out.println("\t java -jar blast3r.jar -q \"xubuntu 14.04\" --peers --proxy --proxy-ip localhost --proxy-port 1080\n");
            System.out.println("Would search for torrent with the query \"xubuntu 14.04\", and using the socks proxy provided,\n" +
                    "download the required torrent information, download the torrent file if possible, and them discover\n" +
                    "as many peers as possible via ttorrent and nmap (user defined minimum an timeout)." +
                    "(NOTE: nmap and ttorrent traffic isn't routed through the proxy, so don't use --peers)\n");
            if (config.getLogLevel().equals(MyLog.LogLevel.DEBUG)) {
                System.out.println(NOTES + "\n");
            }
        }
        System.out.println("Usage:");
        System.out.println("\t java -jar blast3r.jar [OPTIONS]\n");
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
        System.out.println(DISCLAIMER);
    }


}