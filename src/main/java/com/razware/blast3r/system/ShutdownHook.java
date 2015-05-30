/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

package com.razware.blast3r.system;

import com.esotericsoftware.minlog.Log;
import com.razware.blast3r.Blast3r;
import com.razware.blast3r.Main;
import com.turn.ttorrent.client.Client;
import org.apache.commons.io.FileUtils;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.IOException;

/**
 * Created by abreksa on 5/29/15.
 */
public class ShutdownHook extends Thread {
    private String threadName;

    public ShutdownHook(String name) {
        threadName = name;
        Log.trace("creating thread " + threadName);
    }

    public void start() {
        Log.trace("starting " + threadName);
        try {
            FileUtils.forceDeleteOnExit(new File(Main.getConfig().downloadDirectory));
        } catch (IOException e) {
            Log.error("error setting the downloads directory to delete on exit: " + e.getMessage(), e);
        }
        for (Client client : Blast3r.clients) {
            client.stop();
        }
        if (Main.getConfig().isSaveConfig()) {
            Main.saveConfig();
        }
        AnsiConsole.systemUninstall();
    }

}
