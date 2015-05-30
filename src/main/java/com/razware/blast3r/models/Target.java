/*
 * Copyright (c) 2015 Razware Software Design
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrew Breksa (abreksa4@gmail.com) 5/29/2015
 */

package com.razware.blast3r.models;

import com.google.gson.annotations.Expose;
import com.razware.blast3r.strikeapi.Torrent;
import com.razware.blast3r.system.UniqueList;

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
    public List<Torrent> torrents = new UniqueList<Torrent>();
}
