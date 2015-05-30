package com.razware.blast3r.system;

import com.google.gson.annotations.Expose;
import com.razware.blast3r.Main;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

public class Config {

    @Expose
    @Option(name = "--download-directory", aliases = {"-downd"}, usage = "The directory that holds the downloaded files")
    public String downloadDirectory = "downloads/";
    @Expose
    @Option(name = "--ttorrent-sleep", aliases = {"-ts"}, usage = "The time in seconds to sleep to wait for peers")
    public int ttorrentSleep = 30000;
    @Expose
    @Option(name = "--ttorrent-sleep-count", aliases = {"-tsc"}, usage = "The number of times to let ttorrent sleep to find peers before moving on")
    public int ttorrentSleepCount = 3;
    @Expose
    @Option(name = "--ttorrent-sleep-peer-count", aliases = "-tspc", usage = "The minimum nuber of peers for ttorrent to have before not sleeping.")
    public int ttorrentSleepPeerCount = 1;
    @Expose
    @Option(name = "--data-directory", aliases = {"-datad"}, usage = "The directory that holds the data files")
    public String dataDirectory = "data/";
    @Expose
    @Option(name = "--target-directory", aliases = {"-td"}, usage = "The directory which holds the target files")
    public String targetDirectory = "targets/";
    @Expose
    @Argument(usage = "The names of the target files to load")
    public List<String> targets = new ArrayList<String>();
    public boolean loadTargetsFromFiles = false;
    @Expose
    @Option(name = "--peers", aliases = {"--get-peers"}, usage = "Get the current peers of each torrent")
    public boolean getPeers = false;
    @Option(name = "--help", aliases = {"-h", "--?", "-?"}, usage = "Display this help text and exit", help = true)
    private boolean help = false;
    @Option(name = "--save-config", usage = "Saves the provided config to disk")
    private boolean saveConfig = false;
    @Expose
    @Option(name = "--log-level", aliases = {"-ll"}, usage = "The log level")
    private Main.MyLog.LogLevel logLevel = Main.MyLog.LogLevel.INFO;
    @Expose
    @Option(name = "--log-to-file", aliases = {"-l2f"}, usage = "If " + Main.NAME + " should log to a file")
    private boolean fileLog = false;
    @Expose
    @Option(name = "--log-file", usage = "The log file")
    private String logFile = "log.log";

    public void init() {
        if (!targets.isEmpty()) {
            loadTargetsFromFiles = true;
        }
    }

    public boolean isHelp() {
        return help;
    }


    public boolean isSaveConfig() {
        return saveConfig;
    }


    public Main.MyLog.LogLevel getLogLevel() {
        return logLevel;
    }


    public boolean isFileLog() {
        return fileLog;
    }


    public String getLogFile() {
        return logFile;
    }


    public String toString() {
        return Main.getGson().toJson(this);
    }


}
