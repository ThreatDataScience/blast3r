package com.razware.blast3r.strikeapi;

import java.util.ArrayList;
import java.util.List;

public class FileInfo {
    private List<String> file_names = new ArrayList<String>();
    private List<String> file_lengths = new ArrayList<String>();

    public List<String> getFile_names() {
        return file_names;
    }

    public void setFile_names(List<String> file_names) {
        this.file_names = file_names;
    }

    public List<String> getFile_lengths() {
        return file_lengths;
    }

    public void setFile_lengths(List<String> file_lengths) {
        this.file_lengths = file_lengths;
    }
}