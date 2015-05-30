/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/30/2015
 */

package com.razware.blast3r.modules.PeerFinders;

import com.esotericsoftware.minlog.Log;
import com.razware.blast3r.Blast3r;
import com.razware.blast3r.Main;
import com.razware.blast3r.strikeapi.Torrent;
import com.razware.blast3r.system.UniqueList;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Peer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by abreksa on 5/30/15.
 */
public class TTorrentPeerFinder implements IPeerFinder {
    public List<String> getPeers(Torrent torrent) throws IOException, InterruptedException {
        List<String> ips = new UniqueList<String>();
        Log.info("ttorrent", "getting peers for \"" + torrent.getTorrent_hash() + "\"");
        FileUtils.forceMkdir(new File(Main.blast3r.config.downloadDirectory));
        //Delete the downloaded data
        FileUtils.forceDelete(new File(Main.blast3r.config.downloadDirectory));
        while (new File(Main.blast3r.config.downloadDirectory).exists()) {
            Log.debug("ttorrent", "sleeping for " + 5 + " seconds for downloaded torrent content to be deleted...");
            Thread.sleep(5000);
        }
        //create the required directories
        FileUtils.forceMkdir(new File(Main.blast3r.config.downloadDirectory));
        //Load the torrent file
        SharedTorrent sharedTorrent = SharedTorrent.fromFile(Main.blast3r.downloadTorrentFile(torrent), new File(Main.blast3r.config.downloadDirectory));
        //Download the torrent
        Client client = new Client(InetAddress.getLocalHost(), sharedTorrent);
        Blast3r.clients.add(client);
        client.download();
        int x = 0;
        //While we have to little ips, sleep for A x times
        while (ips.size() < Main.blast3r.config.ttorrentSleepPeerCount && x <= Main.blast3r.config.ttorrentSleepCount) {
            x++;
            Log.debug("ttorrent", "sleeping " + Main.blast3r.config.ttorrentSleep / 1000 + " seconds to wait for peers, have (" + client.getPeers().size() + ") of the minimum (" + Main.blast3r.config.ttorrentSleepPeerCount + ")");
            Thread.sleep(Main.blast3r.config.ttorrentSleep);
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
        return ips;
    }
}
