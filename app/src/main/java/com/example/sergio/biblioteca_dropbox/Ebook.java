package com.example.sergio.biblioteca_dropbox;

/**
 * Created by Sergio on 17/12/2015.
 */
public class Ebook {
    private String fileName;
    private String modified;

    public Ebook(String fileName, String modified) {
        this.fileName = fileName;
        this.modified = modified;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ebook ebook = (Ebook) o;

        if (fileName != null ? !fileName.equals(ebook.fileName) : ebook.fileName != null)
            return false;
        return !(modified != null ? !modified.equals(ebook.modified) : ebook.modified != null);

    }

    @Override
    public int hashCode() {
        int result = fileName != null ? fileName.hashCode() : 0;
        result = 31 * result + (modified != null ? modified.hashCode() : 0);
        return result;
    }
}
