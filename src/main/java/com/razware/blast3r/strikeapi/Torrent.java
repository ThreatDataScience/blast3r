package com.razware.blast3r.strikeapi;

import com.google.gson.annotations.Expose;
import com.razware.blast3r.system.UniqueList;

import java.util.List;

public class Torrent {
    @Expose
    public String torrent_category;
    @Expose
    public String torrent_hash;
    @Expose
    public String torrent_title;
    @Expose
    public int seeds;
    @Expose
    public int leeches;
    @Expose
    public int file_count;
    @Expose
    public String size;
    @Expose
    public int download_count;
    @Expose
    public String upload_date;
    @Expose
    public String uploader_username;
    @Expose
    public String magnet_uri;
    @Expose
    public String description;
    @Expose
    public String title;
    @Expose
    public String imdbid;
    @Expose
    public String[] urls;
    @Expose
    public List<String> peers = new UniqueList<String>();

}
