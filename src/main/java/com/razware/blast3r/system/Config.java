package com.razware.blast3r.system;

import com.google.gson.annotations.Expose;
import com.razware.blast3r.Main;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

public class Config {

    @Expose
    @Option(name = "--data-directory", aliases = {"-dd"}, usage = "The directory that holds the data files")
    public String dataDirectory = "data/";
    @Expose
    @Option(name = "--target-directory", aliases = {"-td"}, usage = "The directory which holds the target files")
    public String targetDirectory = "targets/";
    @Expose
    @Option(name = "--hash", usage = "The hashs to look up")
    public List<String> hashes = new ArrayList<String>();
    @Expose
    @Option(name = "--target-file", aliases = {"-tf"}, usage = "The names of the target files to load")
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
