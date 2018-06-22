package com.example.android.awesomemusicplayer.model;

public class TrackObj {

    private String songName;
    private String songPath;
    private String songArtist;
    private String songAlbum;
    private long albumId;

    public TrackObj(String songName, String songPath, String songArtist, String songAlbum, long albumId) {
        this.songName = songName;
        this.songPath = songPath;
        this.songArtist = songArtist;
        this.songAlbum = songAlbum;
        this.albumId = albumId;
    }


    public String getSongName() {
        return songName;
    }

    public String getSongPath() {
        return songPath;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getSongAlbum() {
        return songAlbum;
    }

    public long getAlbumId() {
        return albumId;
    }
}
