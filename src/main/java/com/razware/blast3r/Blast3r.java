/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

package com.razware.blast3r;

import com.esotericsoftware.minlog.Log;
import com.razware.blast3r.models.Target;
import com.razware.blast3r.modules.StrikeTorrentDownloader;
import com.razware.blast3r.modules.TorrageTorrentDownloader;
import com.razware.blast3r.strikeapi.Strike;
import com.razware.blast3r.strikeapi.Torrent;
import com.razware.blast3r.system.UniqueList;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Peer;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.InetAddress;
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
     */

    public static List<Client> clients = new ArrayList<Client>();
    public Strike strike = new Strike();
    Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread th, Throwable ex) {
            System.out.println("Uncaught exception: " + ex);
        }
    };

    public Blast3r() {

    }

    public List<Torrent> search(Target target) throws Exception {
        return strike.search(target.query, target.category, target.subcategory);
    }

    public List<Torrent> info(String[] hashes) throws Exception {
        return strike.info(hashes);
    }

    public File downloadTorrentFile(Torrent torrent) throws IOException {
        //create the needed directories
        FileUtils.forceMkdir(new File(Main.getConfig().torrentDir));
        //Declear the new torrent file
        File torrentFile = new File(Main.getConfig().torrentDir + torrent.getTorrent_hash() + ".torrent");
        //If we want to delete the file on exit:
        if (Main.getConfig().deleteTorrentsOnExit) {
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

    public List<String> getPeers(Torrent torrent) throws IOException {
        List<String> ips = new UniqueList<String>();
        if (Main.getConfig().disableTTorrent) {
            Log.warn("ttorrent is disabled, falling back to nmap");
            ips.addAll(getPeersWithNMap(torrent));
            return ips;
        }
        Log.info("ttorrent", "getting peers for \"" + torrent.getTorrent_hash() + "\"");
        try {
            FileUtils.forceMkdir(new File(Main.getConfig().downloadDirectory));
            //Delete the downloaded data
            FileUtils.forceDelete(new File(Main.getConfig().downloadDirectory));
            while (new File(Main.getConfig().downloadDirectory).exists()) {
                Log.debug("ttorrent", "sleeping for " + 5 + " seconds for downloaded torrent content to be deleted...");
                Thread.sleep(5000);
            }
            //create the required directories
            FileUtils.forceMkdir(new File(Main.getConfig().downloadDirectory));
            //Load the torrent file
            SharedTorrent sharedTorrent = SharedTorrent.fromFile(downloadTorrentFile(torrent), new File(Main.getConfig().downloadDirectory));
            //Download the torrent
            Client client = new Client(InetAddress.getLocalHost(), sharedTorrent);
            clients.add(client);
            client.download();
            int x = 0;
            //While we have to little ips, sleep for A x times
            while (ips.size() < Main.getConfig().ttorrentSleepPeerCount && x <= Main.getConfig().ttorrentSleepCount) {
                x++;
                Log.debug("ttorrent", "sleeping " + Main.getConfig().ttorrentSleep / 1000 + " seconds to wait for peers, have (" + client.getPeers().size() + ") of the minimum (" + Main.getConfig().ttorrentSleepPeerCount + ")");
                Thread.sleep(Main.getConfig().ttorrentSleep);
                for (Peer peer : client.getPeers()) {
                    //If it's a unique peer, log message and add IP
                    if (!ips.contains(peer.getIp())) {
                        Log.debug("ttorrent", "found peer \"" + peer.getIp() + "\" for \"" + torrent.getTorrent_title() + "\" (" + (ips.size() + 1) + ")");
                        ips.add(peer.getIp());
                    }
                }
            }
            Log.info("ttorrent", "(" + ips.size() + ") peers found");
            //Stop the download
            client.stop();
            while (!client.getState().equals(Client.ClientState.ERROR)) {
                Log.debug("ttorrent", "sleeping for 5 seconds to let ttorrent client stop...");
                Thread.sleep(5000);
            }
            //If we got too little ips
            if (ips.size() < Main.getConfig().ttorrentSleepPeerCount) {
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

    public List<String> getPeersWithNMap(Torrent torrent) throws IOException {
        List<String> peers = new ArrayList<String>();
        if (Main.getConfig().disableNmap) {
            Log.warn("nmap", "needed to use nmap, but it was disabled...");
            return peers;
        }
        Log.debug("nmap", "looking up peers for \"" + torrent.getTorrent_hash() + "\"");
        Process process = new ProcessBuilder(
                "/bin/sh", "-c", String.format(Main.getConfig().nmapCMD, torrent.getMagnet_uri())).start();
        Log.debug(String.format("command line: " + Main.getConfig().nmapCMD, torrent.getMagnet_uri()));
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            Log.debug("nmap", "found peer \"" + line + "\" for \"" + torrent.getTorrent_title() + "\"");
            peers.add(line);
        }
        Log.info("nmap", "(" + peers.size() + ") peers found");
        return peers;
    }
}
