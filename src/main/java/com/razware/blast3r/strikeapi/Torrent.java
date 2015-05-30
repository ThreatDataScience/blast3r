/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

package com.razware.blast3r.strikeapi;

import com.razware.blast3r.system.UniqueList;

import java.util.List;

public class Torrent {
    private String torrent_category;
    private String torrent_hash;
    private String torrent_title;
    private long seeds;
    private long leeches;
    private long file_count;
    private long size;
    private long download_count;
    private String upload_date;
    private String uploader_username;
    private String magnet_uri;
    private String description;
    private String title;
    private String imdbid;
    private String[] urls;
    private List<String> peers = new UniqueList<String>();
    private FileInfo file_info = new FileInfo();

    public String getTorrent_category() {
        return torrent_category;
    }

    public void setTorrent_category(String torrent_category) {
        this.torrent_category = torrent_category;
    }

    public String getTorrent_hash() {
        return torrent_hash;
    }

    public void setTorrent_hash(String torrent_hash) {
        this.torrent_hash = torrent_hash;
    }

    public String getTorrent_title() {
        return torrent_title;
    }

    public void setTorrent_title(String torrent_title) {
        this.torrent_title = torrent_title;
    }

    public long getSeeds() {
        return seeds;
    }

    public void setSeeds(long seeds) {
        this.seeds = seeds;
    }

    public long getLeeches() {
        return leeches;
    }

    public void setLeeches(long leeches) {
        this.leeches = leeches;
    }

    public long getFile_count() {
        return file_count;
    }

    public void setFile_count(long file_count) {
        this.file_count = file_count;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDownload_count() {
        return download_count;
    }

    public void setDownload_count(long download_count) {
        this.download_count = download_count;
    }

    public String getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(String upload_date) {
        this.upload_date = upload_date;
    }

    public String getUploader_username() {
        return uploader_username;
    }

    public void setUploader_username(String uploader_username) {
        this.uploader_username = uploader_username;
    }

    public String getMagnet_uri() {
        return magnet_uri;
    }

    public void setMagnet_uri(String magnet_uri) {
        this.magnet_uri = magnet_uri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImdbid() {
        return imdbid;
    }

    public void setImdbid(String imdbid) {
        this.imdbid = imdbid;
    }

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public List<String> getPeers() {
        return peers;
    }

    public void setPeers(List<String> peers) {
        this.peers = peers;
    }

    public FileInfo getFile_info() {
        return file_info;
    }

    public void setFile_info(FileInfo file_info) {
        this.file_info = file_info;
    }
}
