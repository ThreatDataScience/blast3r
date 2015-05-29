package com.razware.blast3r;

import com.esotericsoftware.minlog.Log;
import com.razware.blast3r.models.Target;
import strikeapi.Strike;
import strikeapi.Torrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Blast3r {
    private final String nmapCMD = "nmap --script bittorrent-discovery --script-args 'bittorrent-discovery.magnet=\"%s\"' | grep -E -o \"([0-9]{1,3}[\\.]){3}[0-9]{1,3}\"";
    public Strike strike = new Strike();

    public Blast3r() {

    }

    public List<Torrent> search(Target target) throws Exception {
        List<Torrent> torrents = Arrays.asList(strike.search(target.query, target.category, target.subcategory));
        return torrents;
    }

    public Torrent[] info(String[] hashes) throws Exception {
        return strike.info(hashes);
    }

    public List<String> getPeers(Torrent torrent) throws IOException {
        Log.info("looking up peers for " + torrent.torrent_hash);
        Process process = new ProcessBuilder(
                "/bin/sh", "-c", String.format(nmapCMD, torrent.magnet_uri)).start();
        Log.debug(String.format("command line: " + nmapCMD, torrent.magnet_uri));
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        List<String> peers = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            Log.debug("nmap", "found peer \"" + line + "\" for \"" + torrent.torrent_title + "\"");
            peers.add(line);
        }
        Log.info("(" + peers.size() + ") peers found");
        return peers;
    }
}
