package com.example.sergio.biblioteca_dropbox;

/**
 * Created by Sergio on 17/12/2015.
 */
public class Ebook {
    private String fileName;
    private String modified;
    private String path;

    public Ebook(String fileName, String modified, String path) {
        this.fileName = fileName;
        this.modified = modified;
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
