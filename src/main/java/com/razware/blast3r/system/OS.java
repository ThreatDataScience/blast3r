/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

package com.razware.blast3r.system;

/**
 * Created by abreksa on 5/29/15.
 */
public enum OS {
    Windows, Mac, Unix, Solaris;
    private static String property = System.getProperty("os.name").toLowerCase();

    public static OS getOS() {
        if (isMac()) {
            return Mac;
        }
        if (isUnix()) {
            return Unix;
        }
        if (isWindows()) {
            return Windows;
        }
        if (isSolaris()) {
            return Solaris;
        }
        return null;
    }

    public static boolean isSupported(OS os) {
        switch (os) {
            case Windows:
                return false;
            case Mac:
                return true;
            case Unix:
                return true;
            case Solaris:
                return false;
            default:
                return false;
        }
    }

    public static boolean isWindows() {
        return (property.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (property.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (property.indexOf("nix") >= 0 || property.indexOf("nux") >= 0 || property.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (property.indexOf("sunos") >= 0);
    }
}
