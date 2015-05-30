/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

package com.razware.blast3r.strikeapi;

import com.esotericsoftware.minlog.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.razware.blast3r.Main;
import com.sun.xml.internal.messaging.saaj.util.Base64;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.List;

public class Strike {
    private Gson gson;

    {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Strike() {

    }

    public List<Torrent> info(String[] hash) throws Exception {
        String endpoint = "torrents/info/";
        String hashes = "";
        boolean first = true;
        for (String ha : hash) {
            if (first) {
                first = false;
            } else {
                hashes += ",";
            }
            hashes += ha;
        }
        return gson.fromJson(query(endpoint + "?hashes=" + hashes), Result.class).getTorrents();
    }

    private String query(String string) throws Exception {
        URL url = new URL(Main.getConfig().apiUrl + string);
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
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            Log.error("strinke api", "unable to query API: " + e.getMessage(), e);
            throw e;
        }
    }

    public String description(String hash) throws Exception {
        String endpoint = "torrents/descriptions/";
        Description description = gson.fromJson(query(endpoint + "?hash=" + hash), Description.class);
        return Base64.base64Decode(description.message);
    }

    public List<Torrent> search(String query, String category, String subcategory) throws Exception {
        String endpoint = "torrents/search/";
        if (category != null && !category.equals("")) {
            endpoint += "?category=" + category + "&";
            if (subcategory != null && !subcategory.equals("")) {
                endpoint += "subcategory=" + subcategory + "&";
            }
        } else {
            endpoint += "?";
        }
        return gson.fromJson(query(endpoint + "phrase=" + URLEncoder.encode(query)), Result.class).getTorrents();
    }

    public List<Torrent> top(String category, String subcategory) throws Exception {
        String endpoint = "torrents/search/";
        endpoint += "?category=" + category + "&";
        if (subcategory != null && !subcategory.equals("")) {
            endpoint += "subcategory=" + subcategory;
        }
        return gson.fromJson(query(endpoint), Result.class).getTorrents();
    }

    public int countTotal() throws Exception {
        String endpoint = "torrents/count/";
        return Integer.parseInt(gson.fromJson(query(endpoint), Result.class).getMessage());
    }

    class Description {
        @Expose
        public int statuscode;
        @Expose
        public String message;
    }
}