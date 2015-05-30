/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

package com.razware.blast3r.system;

import com.google.gson.annotations.Expose;
import com.razware.blast3r.Main;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

public class Config {

    @Expose
    @Option(name = "--hash", usage = "Run a search with the specified info hashes as targets.")
    public List<String> hashes = new ArrayList<String>();
    @Expose
    @Option(name = "--query", usage = "Run a search with the specified query strings as targets.")
    public List<String> queries = new ArrayList<String>();
    @Expose
    @Option(name = "--delete-torrents-on-exit", aliases = {"-dtoe"}, usage = "If blast3r should delete the downloaded torrent files on exit")
    public boolean deleteTorrentsOnExit = false;
    @Expose
    @Option(name = "-torrent-directory", usage = "The directory to save the downloaded torrent files to")
    public String torrentDir = "torrents/";
    @Expose
    @Option(name = "--strike-api-url", usage = "The strike api base url")
    public String apiUrl = "https://getstrike.net/api/v2/";
    @Expose
    @Option(name = "--strike-download-url", usage = "The strike download url")
    public String strikeDownloadURL = "https://getstrike.net/torrents/api/download/%s.torrent";
    @Expose
    @Option(name = "--torrage-url", usage = "The torrage url")
    public String torrageURL = "http://torrage.info/download.php?h=%s";
    @Expose
    @Option(name = "--user-agent", usage = "The user agent to use while accessing strike and torrage")
    public String userAgent = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
    @Expose
    @Option(name = "--nmap-command-line", usage = "The nmap command line to use to discover peers")
    public String nmapCMD = "nmap --script bittorrent-discovery --script-args 'bittorrent-discovery.magnet=\"%s\"' | grep -E -o \"([0-9]{1,3}[\\.]){3}[0-9]{1,3}\"";
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
    @Option(name = "--info", usage = "diaplay extra information in the help output")
    public boolean info = false;
    @Expose
    @Option(name = "--disable-nmap", usage = "disables using nmap to scan for peers")
    public boolean disableNmap = false;
    @Option(name = "--help", aliases = {"-h", "--?", "-?"}, usage = "Display this help text and exit", help = true)
    private boolean help = false;
    @Option(name = "--save-config", usage = "Saves the provided config to disk")
    private boolean saveConfig = false;
    @Expose
    @Option(name = "--log-level", aliases = {"-ll"}, usage = "The log level")
    private MyLog.LogLevel logLevel = MyLog.LogLevel.INFO;
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


    public MyLog.LogLevel getLogLevel() {
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
