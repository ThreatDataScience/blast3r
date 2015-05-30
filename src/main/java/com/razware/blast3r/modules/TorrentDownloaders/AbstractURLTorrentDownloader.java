/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/30/2015
 */

package com.razware.blast3r.modules.TorrentDownloaders;

import com.esotericsoftware.minlog.Log;
import com.razware.blast3r.Main;
import com.turn.ttorrent.client.SharedTorrent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by abreksa on 5/30/15.
 */
public abstract class AbstractURLTorrentDownloader implements ITorrentDownloader {

    public void download(URL url, File torrentFile) throws IOException {
        FileUtils.forceMkdir(new File(Main.getConfig().downloadDirectory));
        Log.debug("downloading with URL: " + url.toString());
        URLConnection connection;
        if (Main.getConfig().proxy) {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(Main.getConfig().proxyIp, Main.getConfig().proxyPort));
            connection = url.openConnection(proxy);
        } else {
            connection = url.openConnection();
        }
        if (!Main.getConfig().disableUserAgentSpoo) {
            connection.setRequestProperty("User-Agent", Main.getConfig().userAgent);
        }
        ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
        FileOutputStream fos = new FileOutputStream(torrentFile.getAbsoluteFile());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
        SharedTorrent sharedTorrent = SharedTorrent.fromFile(torrentFile, new File(Main.getConfig().downloadDirectory));
        sharedTorrent.close();
    }

}
