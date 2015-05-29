package com.razware.blast3r.models;

import com.google.gson.annotations.Expose;
import strikeapi.Torrent;

import java.util.ArrayList;
import java.util.List;

public class Target {
    @Expose
    public boolean hash;
    @Expose
    public String name;
    @Expose
    public String query;
    @Expose
    public String category;
    @Expose
    public String subcategory;
    public List<Torrent> torrents = new ArrayList<Torrent>();
}
