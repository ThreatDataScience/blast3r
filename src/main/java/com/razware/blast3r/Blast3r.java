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
import com.razware.blast3r.modules.TorrentDownloaders.StrikeTorrentDownloader;
import com.razware.blast3r.modules.TorrentDownloaders.TorrageTorrentDownloader;
import com.razware.blast3r.strikeapi.Strike;
import com.razware.blast3r.strikeapi.Torrent;
import com.razware.blast3r.system.Config;
import com.razware.blast3r.system.UniqueList;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
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

    Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread th, Throwable ex) {
            System.out.println("Uncaught exception: " + ex);
        }
    };

    public Blast3r(Config config) {
        this.config = config;
    }

    public void proccess() throws Exception {
        boolean x = true;
        while (x) {
            if (!this.config.loop) {
                x = false;
            }
            //for each target
            for (Target target : targets) {

                //Create the target's data directory
                File dir = new File(this.config.dataDirectory + target.name + "/");
                FileUtils.forceMkdir(dir);

                //Get a list of Torrent objects
                List<Torrent> torrents;
                //If target is a hash, run a single lookup
                if (target.hash) {
                    try {
                        Log.info("looking up \"" + target.query + "\"");
                        torrents = this.info(new String[]{target.query});
                    } catch (Exception e) {
                        Log.error("unable to get info for " + target.query + ": " + e.getMessage(), e);
                        continue;
                    }
                } else {
                    //Else it's a search string, and we want to look it up
                    Log.info("searching for \"" + target.query + "\"");
                    torrents = this.search(target);
                }
                Log.info("(" + torrents.size() + ") torrents found");

                //For each of the torrents,
                for (Torrent torrent : torrents) {
                    if (this.config.getPeers) {
                        List<String> peers = this.getPeers(torrent);
                        for (String peer : peers) {
                            torrent.getPeers().add(peer);
                        }
                    }

                    //Write the torrent json to disk
                    File file = new File(this.config.dataDirectory + target.name + "/" + torrent.getTorrent_hash() + ".json");
                    //If we;ve already seen it
                    if (file.exists()) {
                        Torrent torrent1 = Main.getGson().fromJson(FileUtils.readFileToString(file), Torrent.class);
                        int z = 0;
                        //Load the peers from the exisitng json and save the new data
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
                }
                //Save all torrents to the target for single file export
                //target.torrents.addAll(torrents);
                //Need to impliment
            }
        }
    }

    public void loadTargets() {
        Log.info("loading targets");
        if (!config.queries.isEmpty()) {
            for (String query : config.queries) {
                Target target = new Target();
                target.query = query;
                target.name = query;
                Log.debug("loaded target \"" + target.query + "\":\n" + Main.getGson().toJson(target));
                targets.add(target);
            }
        }
        if (!config.hashes.isEmpty()) {
            for (String hash : config.hashes) {
                Target target = new Target();
                target.query = hash;
                target.name = hash;
                target.hash = true;
                Log.debug("loaded target \"" + target.query + "\":\n" + Main.getGson().toJson(target));
                targets.add(target);
            }
        }
        if (config.loadTargetsFromFiles) {
            for (String filename : config.targets) {
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
        //create the needed directories
        FileUtils.forceMkdir(new File(this.config.torrentDir));
        //Declear the new torrent file
        File torrentFile = new File(this.config.torrentDir + torrent.getTorrent_hash() + ".torrent");
        //If we want to delete the file on exit:
        if (this.config.deleteTorrentsOnExit) {
            FileUtils.forceDeleteOnExit(torrentFile);
        }
        //If it already exists, return the one on disk instead (this is cool, todo: get the torrent loading down so we can get non-dht torrents)
        if (torrentFile.exists()) {
            return torrentFile;
        }

        try {
            //Try to get it from strike
            StrikeTorrentDownloader strikeTorrentDownloader = new StrikeTorrentDownloader();
            strikeTorrentDownloader.download(torrent, torrentFile);
            return torrentFile;
        } catch (InvalidBEncodingException e) {
            Log.debug("strike api", "unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\", falling back to torrage");
            Log.debug(e.getMessage(), e);
        } catch (IOException e) {
            Log.debug("strike api", "unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\", falling back to torrage");
            Log.debug(e.getMessage(), e);
        }

        //Get rid of the bad file
        Log.debug("deleting bad torrent...");
        FileUtils.forceDelete(torrentFile);
        try {
            //Try to download from torrage
            TorrageTorrentDownloader torrageTorrentDownloader = new TorrageTorrentDownloader();
            torrageTorrentDownloader.download(torrent, torrentFile);
            return torrentFile;
        } catch (InvalidBEncodingException e) {
            Log.debug("torrage", "unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\"");
            Log.debug(e.getMessage(), e);
            throw new IOException("unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\"", e);
        } catch (IOException e) {
            Log.debug("torrage", "unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\"");
            Log.debug(e.getMessage(), e);
            throw new IOException("unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\"", e);
        }
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
