/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

package com.razware.blast3r.strikeapi;

import java.util.List;

public class Result {

    private int statuscode;
    private String responsetime;
    private int results;
    private String message;
    private List<Torrent> torrents;
    private String title;
    private String imdbID;

    public int getStatuscode() {
        return statuscode;
    }

    public void setStatuscode(int statuscode) {
        this.statuscode = statuscode;
    }

    public String getResponsetime() {
        return responsetime;
    }

    public void setResponsetime(String responsetime) {
        this.responsetime = responsetime;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Torrent> getTorrents() {
        return torrents;
    }

    public void setTorrents(List<Torrent> torrents) {
        this.torrents = torrents;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }
}