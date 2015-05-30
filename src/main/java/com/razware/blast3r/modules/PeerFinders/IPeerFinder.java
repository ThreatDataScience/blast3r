/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/30/2015
 */

package com.razware.blast3r.modules.PeerFinders;

import com.razware.blast3r.strikeapi.Torrent;

import java.io.IOException;
import java.util.List;

/**
 * Created by abreksa on 5/30/15.
 */
public interface IPeerFinder {
    List<String> getPeers(Torrent torrent) throws IOException, InterruptedException;
}
