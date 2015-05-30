/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

package com.razware.blast3r.system;

import com.esotericsoftware.minlog.Log;
import com.razware.blast3r.Main;
import org.apache.commons.io.FileUtils;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * Created by abreksa on 5/29/15.
 */
public class MyLog extends Log.Logger {
    public void log(int level, String category, String message, Throwable ex) {
        StringBuilder builder = new StringBuilder();
        Ansi ansi = new Ansi();
        builder.append("[" + new Date() + "]");
        switch (level) {
            case Log.LEVEL_ERROR:
                builder.append("@|red  ERROR|@:");
                break;
            case Log.LEVEL_WARN:
                builder.append("@|green  WARN|@:");
                break;
            case Log.LEVEL_INFO:
                builder.append("@|blue  INFO|@:");
                break;
            case Log.LEVEL_DEBUG:
                builder.append("@|yellow  DEBUG|@:");
                break;
            case Log.LEVEL_TRACE:
                builder.append(" TRACE:");
                break;
        }
        builder.append(" [" + Thread.currentThread().getName() + "]");
        builder.append(" [");
        if (category != null) {
            builder.append(category);
        } else {
            String string = Thread.currentThread().getStackTrace()[3].getClassName();
            builder.append(string.substring(string.lastIndexOf(".") + 1));
            //builder.append("*");
        }
        builder.append("]");
        builder.append(" " + message);
        if (ex != null) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            builder.append('\n');
            builder.append(writer.toString().trim());
        }
        System.out.println(ansi.render(builder.toString()));
        //System.out.println(builder);
        if (Main.getConfig().isFileLog()) {
            File file = new File(Main.getConfig().getLogFile());
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    Log.error("error creating new config file: " + e.getMessage() + "\n" + e.getStackTrace());
                }
            }
            try {
                FileUtils.writeStringToFile(file, builder.toString() + "\n", true);
            } catch (IOException e) {
                Log.error("error writing new config to config file: " + e.getMessage() + "\n" + e.getStackTrace());
            }
        }
    }

    public enum LogLevel {
        NONE(6),
        ERROR(5),
        WARN(4),
        INFO(3),
        DEBUG(2),
        TRACE(1);

        private int level;

        LogLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}
