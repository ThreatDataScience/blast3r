/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/30/2015
 */

package com.razware.blast3r.modules.PeerFinders;

import com.esotericsoftware.minlog.Log;
import com.razware.blast3r.Main;
import com.razware.blast3r.strikeapi.Torrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abreksa on 5/30/15.
 */
public class NMapPeerFinder implements IPeerFinder {

    public List<String> getPeers(Torrent torrent) throws IOException, InterruptedException {
        List<String> peers = new ArrayList<String>();
        if (Main.blast3r.config.disableNmap) {
            Log.warn("nmap", "needed to use nmap, but it was disabled...");
            return peers;
        }
        Log.debug("nmap", "looking up peers for \"" + torrent.getTorrent_hash() + "\"");
        Process process;
        switch (Main.os) {
            case Windows:
                process = new ProcessBuilder(Main.getConfig().nmapPath.concat("nmap.exe"), "-c", String.format(Main.getConfig().nmapCMD, torrent.getMagnet_uri())).start();
                break;
            default:
                process = new ProcessBuilder(
                        "/bin/sh", "-c", String.format(Main.blast3r.config.nmapCMD, torrent.getMagnet_uri())).start();
                break;
        }
        Log.debug(String.format("command line: " + Main.blast3r.config.nmapCMD, torrent.getMagnet_uri()));
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        BufferedReader bre = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = br.readLine()) != null) {
            Log.debug("nmap", "found peer \"" + line + "\" for \"" + torrent.getTorrent_title() + "\"");
            peers.add(line);
        }
        while ((line = bre.readLine()) != null) {
            Log.error(line);
        }
        Log.info("nmap", "(" + peers.size() + ") peers found");
        return peers;
    }
}
