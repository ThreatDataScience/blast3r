/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

package com.razware.blast3r;

import com.esotericsoftware.minlog.Log;
import com.razware.blast3r.models.Target;
import com.razware.blast3r.modules.PeerFinders.NMapPeerFinder;
import com.razware.blast3r.modules.PeerFinders.TTorrentPeerFinder;
import com.razware.blast3r.modules.TorrentDownloaders.ITorrentDownloader;
import com.razware.blast3r.modules.TorrentDownloaders.StrikeTorrentDownloader;
import com.razware.blast3r.modules.TorrentDownloaders.TorrageTorrentDownloader;
import com.razware.blast3r.strikeapi.Strike;
import com.razware.blast3r.strikeapi.Torrent;
import com.razware.blast3r.system.Config;
import com.razware.blast3r.system.UniqueList;
import com.turn.ttorrent.client.Client;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Blast3r {
    /*
    todo: make a plugin system for peer discovery
    todo: make a plugin system for torrent indexs, this includes things like private trackers...
    todo: make a plugin system for torrent file downloads, or find a library that supports it
    todo: stndardize log4j output with minlog
    todo: add torrentz.eu scraping to get site urls
    todo: write a way to save the target's torrents to disk and load them like th peers?
    todo: fix the unhandled
    todo: find a way to remove out ip from the peer lists
    todo: add some form of filtering base on torrent attributes
    todo: make "session" presets for different cmapaigns
     */

    public static List<Client> clients = new ArrayList<Client>();
    public Strike strike = new Strike();
    public List<Target> targets = new ArrayList<Target>();
    public Config config;

    Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread th, Throwable ex) {
            Log.error("Uncaught exception: " + ex.getMessage(), ex);
        }
    };

    public Blast3r(Config config) {
        this.config = config;
        Thread.currentThread().setUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    private void processTarget(Target target) throws Exception {
        File dir = new File(this.config.dataDirectory + target.name + "/");
        FileUtils.forceMkdir(dir);
        List<Torrent> torrents;
        if (target.hash) {
            try {
                Log.info("looking up \"" + target.query + "\"");
                torrents = this.info(new String[]{target.query});
            } catch (Exception e) {
                throw new IOException("unable to get info for " + target.query + ": " + e.getMessage(), e);
            }
        } else {
            Log.info("searching for \"" + target.query + "\"");
            torrents = this.search(target);
        }
        Log.info("(" + torrents.size() + ") torrents found");
        for (Torrent torrent : torrents) {
            processTorrent(target, torrent);
        }
    }

    private void processTorrent(Target target, Torrent torrent) throws IOException, InterruptedException {
        if (this.config.getPeers) {
            List<String> peers = this.getPeers(torrent);
            for (String peer : peers) {
                torrent.getPeers().add(peer);
            }
        }

        File file = new File(this.config.dataDirectory + target.name + "/" + torrent.getTorrent_hash() + ".json");
        if (file.exists()) {
            Torrent torrent1 = Main.getGson().fromJson(FileUtils.readFileToString(file), Torrent.class);
            int z = 0;
            if (torrent1 != null && torrent1.getPeers() != null && torrent1.getPeers().size() > 0) {
                for (String p : torrent1.getPeers()) {
                    if (torrent.getPeers().contains(p)) {
                        torrent.getPeers().add(p);
                        z++;
                    }
                }
                Log.info("loaded (" + z + ") unqiue existing peers records for \"" + torrent.getTorrent_hash() + "\"");
            }
            file.delete();
        }
        file.createNewFile();
        FileUtils.writeStringToFile(file, Main.getGson().toJson(torrent));
        target.torrents.add(torrent);
    }

    public void run() throws Exception {
        boolean x = true;
        while (x) {
            if (!this.config.loop) {
                x = false;
            }
            for (Target target : targets) {
                processTarget(target);
            }
        }
    }

    public void loadQueries(List<String> queries) {
        Log.info("loading query targets");
        if (!queries.isEmpty()) {
            for (String query : queries) {
                Target target = new Target();
                target.query = query;
                target.name = query;
                Log.debug("loaded target \"" + target.query + "\":\n" + Main.getGson().toJson(target));
                targets.add(target);
            }
        }
    }

    public void loadHashes(List<String> hashes) {
        if (!hashes.isEmpty()) {
            for (String hash : hashes) {
                Target target = new Target();
                target.query = hash;
                target.name = hash;
                target.hash = true;
                Log.debug("loaded target \"" + target.query + "\":\n" + Main.getGson().toJson(target));
                targets.add(target);
            }
        }
    }

    public void loadTargets(List<String> files) {
        if (config.loadTargetsFromFiles) {
            for (String filename : files) {
                try {
                    Target target = Main.getGson().fromJson(FileUtils.readFileToString(new File(config.targetDirectory + filename + ".json")), Target.class);
                    targets.add(target);
                    Log.debug("loaded target \"" + config.targetDirectory + filename + ".json" + "\":\n" + Main.getGson().toJson(target));
                } catch (IOException e) {
                    Log.error("target file " + filename + " does not exist: " + e.getMessage(), e);
                }
            }
        }
    }

    public List<Torrent> search(Target target) throws Exception {
        return strike.search(target.query, target.category, target.subcategory);
    }

    public List<Torrent> info(String[] hashes) throws Exception {
        return strike.info(hashes);
    }

    public File downloadTorrentFile(Torrent torrent) throws IOException {

        //Loop through each torrent file download method
        List<ITorrentDownloader> torrentDownloaders = new ArrayList<ITorrentDownloader>();
        torrentDownloaders.add(new StrikeTorrentDownloader());
        torrentDownloaders.add(new TorrageTorrentDownloader());

        FileUtils.forceMkdir(new File(this.config.torrentDir));
        File torrentFile = new File(this.config.torrentDir + torrent.getTorrent_hash() + ".torrent");
        if (this.config.deleteTorrentsOnExit) {
            FileUtils.forceDeleteOnExit(torrentFile);
        }

        if (torrentFile.exists()) {
            return torrentFile;
        }

        for (ITorrentDownloader torrentDownloader : torrentDownloaders) {
            try {
                torrentDownloader.download(torrent, torrentFile);
                Log.info("downloaded torrent file via " + torrentDownloader.getClass().getSimpleName());
                return torrentFile;
            } catch (Exception e) {
                Log.error("unable to download torrent file via " + torrentDownloader.getClass().getSimpleName());
            }
        }
        throw new IOException("unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\"");
    }

    public List<String> getPeersWithNMap(Torrent torrent) throws IOException, InterruptedException {
        return new NMapPeerFinder().getPeers(torrent);
    }

    public List<String> getPeers(Torrent torrent) throws IOException, InterruptedException {
        List<String> ips = new UniqueList<String>();
        if (this.config.disableTTorrent) {
            Log.warn("ttorrent is disabled, falling back to nmap");
            ips.addAll(getPeersWithNMap(torrent));
            return ips;
        }
        TTorrentPeerFinder tTorrentPeerFinder = new TTorrentPeerFinder();
        try {
            ips = tTorrentPeerFinder.getPeers(torrent);
            //If we got too little ips
            if (ips.size() < this.config.ttorrentSleepPeerCount) {
                Log.warn("not enough peers found via ttorrent, falling back to nmap to discover peers");
                try {
                    //Try to get peers with nmap
                    ips.addAll(getPeersWithNMap(torrent));
                } catch (IOException e) {
                    Log.error(e.getMessage(), e);
                }
            }
            return ips;
        } catch (IOException e) {
            Log.warn("ttorrent", "unable to download the torrent file, falling back to nmap to discover peers");
            Log.debug(e.getMessage(), e);
            try {
                return getPeersWithNMap(torrent);
            } catch (IOException e1) {
                Log.error("nmap", "error while getting peers with namp");
                Log.debug(e1.getMessage(), e1);
                throw new IOException("unable to get peers", e1);
            }
        } catch (InterruptedException e) {
            Log.error("ttorrent", "error while waiting for peers, falling back to nmap");
            Log.debug(e.getMessage(), e);
            throw new IOException("unable to get peers", e);
        }
    }

}
