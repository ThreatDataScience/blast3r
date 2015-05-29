package strikeapi;

import com.google.gson.annotations.Expose;

public class Result {
    @Expose
    public int statuscode;
    @Expose
    public String responsetime;
    @Expose
    public int results;
    @Expose
    public String message;
    @Expose
    public Torrent[] torrents;
    @Expose
    public String title;
    @Expose
    public String imdbID;

}
