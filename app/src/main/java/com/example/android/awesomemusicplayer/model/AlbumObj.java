package com.example.android.awesomemusicplayer.model;

public class AlbumObj {

    private String album_name;
    private long album_id;

    public AlbumObj(String album_name, long album_id) {
        this.album_name = album_name;
        this.album_id = album_id;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public long getAlbum_id() {
        return album_id;
    }
}
