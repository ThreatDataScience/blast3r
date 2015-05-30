/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/30/2015
 */

package com.razware.blast3r.modules;

import com.esotericsoftware.minlog.Log;
import com.razware.blast3r.Main;
import com.razware.blast3r.strikeapi.Torrent;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by abreksa on 5/30/15.
 */
public class StrikeTorrentDownloader extends AbstractURLTorrentDownloader {
    public void download(Torrent torrent, File torrentFile) throws IOException {
        Log.debug("strike api", "downloading the torrent file for \"" + torrent.getTorrent_hash() + "\"");
        URL website = new URL(String.format(Main.getConfig().strikeDownloadURL, torrent.getTorrent_hash()));
        super.download(website, torrentFile);
    }
}
