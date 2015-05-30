package com.razware.blast3r;

import com.esotericsoftware.minlog.Log;
import com.razware.blast3r.models.Target;
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
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Blast3r {
    /*
    todo: make a plugin system for peer discovery
    todo: make a plugin system for torrent indexs, this includes things like private trackers...
    todo: make a plugin system for torrent file downloads, or find a library that supports it
     */

    public static final String strikeDownloadURL = "https://getstrike.net/torrents/api/download/%s.torrent";
    public static final String torrageURL = "http://torrage.info/download.php?h=%s";
    private static final String userAgent = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
    private static final String nmapCMD = "nmap --script bittorrent-discovery --script-args 'bittorrent-discovery.magnet=\"%s\"' | grep -E -o \"([0-9]{1,3}[\\.]){3}[0-9]{1,3}\"";
    public static List<Client> clients = new ArrayList<Client>();
    public Strike strike = new Strike();

    public Blast3r() {

    }

    public List<Torrent> search(Target target) throws Exception {
        List<Torrent> torrents = strike.search(target.query, target.category, target.subcategory);
        return torrents;
    }

    public List<Torrent> info(String[] hashes) throws Exception {
        return strike.info(hashes);
    }

    private File downloadTorrentFile(Torrent torrent) throws IOException {
        File torrentFile = Files.createTempFile(torrent.getTorrent_hash(), ".torrent").toFile();
        FileUtils.forceDeleteOnExit(torrentFile);
        try {
            downloadFromStrike(torrent, torrentFile);
            return torrentFile;
        } catch (InvalidBEncodingException e) {
            Log.warn("strike api", "unable to download a valid torrent file for " + torrent.getTorrent_hash() + ", falling back to torrage");
            Log.debug(e.getMessage(), e);
        } catch (IOException e) {
            Log.warn("strike api", "unable to download a valid torrent file for " + torrent.getTorrent_hash() + ", falling back to torrage");
            Log.debug(e.getMessage(), e);
        }
        Log.debug("deleting bad torrent...");
        FileUtils.forceDelete(torrentFile);
        try {
            downloadFromTorrage(torrent, torrentFile);
            return torrentFile;
        } catch (InvalidBEncodingException e) {
            Log.warn("torrage", "unable to download a valid torrent file for " + torrent.getTorrent_hash());
            Log.debug(e.getMessage(), e);
            throw new IOException("unable to download a valid torrent file for " + torrent.getTorrent_hash(), e);
        } catch (IOException e) {
            Log.warn("torrage", "unable to download a valid torrent file for " + torrent.getTorrent_hash());
            Log.debug(e.getMessage(), e);
            throw new IOException("unable to download a valid torrent file for " + torrent.getTorrent_hash(), e);
        }
    }

    private void downloadFromStrike(Torrent torrent, File torrentFile) throws InvalidBEncodingException, IOException {
        Log.info("strike api", "downloading the torrent file for " + torrent.getTorrent_hash());
        URL website = new URL(String.format(strikeDownloadURL, torrent.getTorrent_hash()));
        download(website, torrentFile);
    }


    private void download(URL url, File torrentFile) throws InvalidBEncodingException, IOException {
        FileUtils.forceMkdir(new File(Main.getConfig().downloadDirectory));
        Log.debug("Downloading with URL: " + url.toString());
        java.net.URLConnection c = url.openConnection();
        c.setRequestProperty("User-Agent", userAgent);
        ReadableByteChannel rbc = Channels.newChannel(c.getInputStream());
        FileOutputStream fos = new FileOutputStream(torrentFile.getAbsoluteFile());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
        SharedTorrent sharedTorrent = SharedTorrent.fromFile(torrentFile, new File(Main.getConfig().downloadDirectory));
        sharedTorrent.close();
    }

    private void downloadFromTorrage(Torrent torrent, File torrentFile) throws InvalidBEncodingException, IOException {
        Log.info("torrage", "downloading the torrent file for " + torrent.getTorrent_hash());
        URL website = new URL(String.format(torrageURL, torrent.getTorrent_hash()));
        download(website, torrentFile);
    }


    public List<String> getPeers(Torrent torrent) throws IOException {
        try {
            List<String> ips = new UniqueList<String>();
            SharedTorrent sharedTorrent = SharedTorrent.fromFile(downloadTorrentFile(torrent), new File(Main.getConfig().downloadDirectory));
            Client client = new Client(InetAddress.getLocalHost(), sharedTorrent);
            clients.add(client);
            client.download();
            int x = 0;
            while (ips.size() < Main.getConfig().ttorrentSleepPeerCount && x <= Main.getConfig().ttorrentSleepCount) {
                x++;
                Log.info("ttorrent", "sleeping " + Main.getConfig().ttorrentSleep / 1000 + " seconds to wait for peers, have " + client.getPeers().size() + " (need " + Main.getConfig().ttorrentSleepPeerCount + ")");
                Thread.currentThread().sleep(Main.getConfig().ttorrentSleep);
                for (Peer peer : client.getPeers()) {
                    if (!ips.contains(peer.getIp())) {
                        Log.debug("ttorrent", "found peer \"" + peer.getIp() + "\" for \"" + torrent.getTorrent_title() + "\" (#" + (ips.size() + 1) + ")");
                    }
                    ips.add(peer.getIp());
                }

            }
            Log.info("ttorrent", "(" + ips.size() + ") peers found");
            client.stop();
            FileUtils.forceDelete(new File(Main.getConfig().downloadDirectory));
            if (ips.size() < Main.getConfig().ttorrentSleepPeerCount) {
                Log.warn("not enough peers found via ttorrent, falling back to nmap");
                try {
                    ips.addAll(getPeersWithNMap(torrent));
                } catch (IOException e) {
                    Log.error(e.getMessage(), e);
                }
            }
            return ips;
        } catch (IOException e) {
            Log.error("ttorrent", "error downloading torrent file, falling back to nmap");
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
        Log.info("looking up peers for " + torrent.getTorrent_hash());
        Process process = new ProcessBuilder(
                "/bin/sh", "-c", String.format(nmapCMD, torrent.getMagnet_uri())).start();
        Log.debug(String.format("command line: " + nmapCMD, torrent.getMagnet_uri()));
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        List<String> peers = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            Log.debug("nmap", "found peer \"" + line + "\" for \"" + torrent.getTorrent_title() + "\"");
            peers.add(line);
        }
        Log.info("nmap", "(" + peers.size() + ") peers found");
        return peers;
    }
}
