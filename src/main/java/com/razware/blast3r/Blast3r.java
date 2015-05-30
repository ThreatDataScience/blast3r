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
import java.util.ArrayList;
import java.util.List;

public class Blast3r {
    /*
    todo: make a plugin system for peer discovery
    todo: make a plugin system for torrent indexs, this includes things like private trackers...
    todo: make a plugin system for torrent file downloads, or find a library that supports it
    todo: stndardize log4j output with minlog
    todo: add torrentz.eu scraping to get site urls
     */

    public static List<Client> clients = new ArrayList<Client>();
    public Strike strike = new Strike();

    public Blast3r() {

    }

    public List<Torrent> search(Target target) throws Exception {
        return strike.search(target.query, target.category, target.subcategory);
    }

    public List<Torrent> info(String[] hashes) throws Exception {
        return strike.info(hashes);
    }

    public File downloadTorrentFile(Torrent torrent) throws IOException {
        FileUtils.forceMkdir(new File(Main.getConfig().torrentDir));
        File torrentFile = new File(Main.getConfig().torrentDir + torrent.getTorrent_hash() + ".torrent");
        if (Main.getConfig().deleteTorrentsOnExit) {
            FileUtils.forceDeleteOnExit(torrentFile);
        }
        if (torrentFile.exists()) {
            return torrentFile;
        }
        try {
            downloadFromStrike(torrent, torrentFile);
            return torrentFile;
        } catch (InvalidBEncodingException e) {
            Log.warn("strike api", "unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\", falling back to torrage");
            Log.debug(e.getMessage(), e);
        } catch (IOException e) {
            Log.warn("strike api", "unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\", falling back to torrage");
            Log.debug(e.getMessage(), e);
        }
        Log.debug("deleting bad torrent...");
        FileUtils.forceDelete(torrentFile);
        try {
            downloadFromTorrage(torrent, torrentFile);
            return torrentFile;
        } catch (InvalidBEncodingException e) {
            Log.warn("torrage", "unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\"");
            Log.debug(e.getMessage(), e);
            throw new IOException("unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\"", e);
        } catch (IOException e) {
            Log.warn("torrage", "unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\"");
            Log.debug(e.getMessage(), e);
            throw new IOException("unable to download a valid torrent file for \"" + torrent.getTorrent_hash() + "\"", e);
        }
    }

    private void downloadFromStrike(Torrent torrent, File torrentFile) throws InvalidBEncodingException, IOException {
        Log.info("strike api", "downloading the torrent file for \"" + torrent.getTorrent_hash() + "\"");
        URL website = new URL(String.format(Main.getConfig().strikeDownloadURL, torrent.getTorrent_hash()));
        download(website, torrentFile);
    }


    private void download(URL url, File torrentFile) throws InvalidBEncodingException, IOException {
        FileUtils.forceMkdir(new File(Main.getConfig().downloadDirectory));
        Log.debug("Downloading with URL: " + url.toString());
        java.net.URLConnection c = url.openConnection();
        c.setRequestProperty("User-Agent", Main.getConfig().userAgent);
        ReadableByteChannel rbc = Channels.newChannel(c.getInputStream());
        FileOutputStream fos = new FileOutputStream(torrentFile.getAbsoluteFile());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
        SharedTorrent sharedTorrent = SharedTorrent.fromFile(torrentFile, new File(Main.getConfig().downloadDirectory));
        sharedTorrent.close();
    }

    private void downloadFromTorrage(Torrent torrent, File torrentFile) throws InvalidBEncodingException, IOException {
        Log.info("torrage", "downloading the torrent file for \"" + torrent.getTorrent_hash() + "\"");
        URL website = new URL(String.format(Main.getConfig().torrageURL, torrent.getTorrent_hash()));
        download(website, torrentFile);
    }


    public List<String> getPeers(Torrent torrent) throws IOException {
        try {
            FileUtils.forceMkdir(new File(Main.getConfig().downloadDirectory));
            List<String> ips = new UniqueList<String>();
            SharedTorrent sharedTorrent = SharedTorrent.fromFile(downloadTorrentFile(torrent), new File(Main.getConfig().downloadDirectory));
            Client client = new Client(InetAddress.getLocalHost(), sharedTorrent);
            clients.add(client);
            client.download();
            int x = 0;
            while (ips.size() < Main.getConfig().ttorrentSleepPeerCount && x <= Main.getConfig().ttorrentSleepCount) {
                x++;
                Log.info("ttorrent", "sleeping " + Main.getConfig().ttorrentSleep / 1000 + " seconds to wait for peers, have (" + client.getPeers().size() + ") of the minimum (" + Main.getConfig().ttorrentSleepPeerCount + ")");
                Thread.currentThread().sleep(Main.getConfig().ttorrentSleep);
                for (Peer peer : client.getPeers()) {
                    if (!ips.contains(peer.getIp())) {
                        Log.debug("ttorrent", "found peer \"" + peer.getIp() + "\" for \"" + torrent.getTorrent_title() + "\" (" + (ips.size() + 1) + ")");
                    }
                    ips.add(peer.getIp());
                }

            }
            Log.info("ttorrent", "(" + ips.size() + ") peers found");
            client.getTorrent().stop();
            client.stop();
            FileUtils.forceDelete(new File(Main.getConfig().downloadDirectory));
            if (ips.size() < Main.getConfig().ttorrentSleepPeerCount) {
                Log.warn("not enough peers found via ttorrent, falling back to nmap to discover peers");
                try {
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
        Log.info("looking up peers for \"" + torrent.getTorrent_hash() + "\"");
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
