package com.example.android.awesomemusicplayer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.android.awesomemusicplayer.model.AlbumObj;
import com.example.android.awesomemusicplayer.model.TrackObj;

import java.util.ArrayList;

public class TracksUtils {

    public static ArrayList<TrackObj> getTracks(Context context) {
        ArrayList<TrackObj> tracArrayList = new ArrayList<>();

        ContentResolver cr = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";


        Cursor cur = cr.query(uri, null, selection, null, sortOrder);

        if (cur != null && cur.getCount() > 1) {
            while (cur.moveToNext()) {
                String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String name = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String duration = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
                Long album_id = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));


                tracArrayList.add(new TrackObj(title, data, artist, album, album_id));
                Log.v("fuck10", String.valueOf(album_id));
            }

        }
        if (cur != null) {
            cur.close();
        }
        return tracArrayList;
    }



    public static ArrayList<AlbumObj> getAlbums(ArrayList<TrackObj> trackObjs){
        ArrayList<AlbumObj> albumObjs = new ArrayList<>();

        for (TrackObj obj : trackObjs){
            String name = obj.getSongAlbum();
            long id = obj.getAlbumId();

            albumObjs.add(new AlbumObj(name , id));
            Log.v("fuck", name + "  " + id);
        }
        return albumObjs;
    }


    public static String getAlbumart(Context context, Long album_id) {
        String currentUri = "no";

        if (album_id > -1) {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums._ID,
                            MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID + "=" + album_id,
                    null,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                String uri = cursor.getString(cursor.getColumnIndex("album_art"));

                currentUri = uri;
            }
            cursor.close();
        }


       // Log.v("fuck", currentUri);
        return currentUri;

    }


    public static ArrayList<TrackObj> getAlbumTracks(long namea, Context context){
        ArrayList<TrackObj> tracArrayList = new ArrayList<>();

        ContentResolver cr = context.getContentResolver();


        Cursor cur = cr.query( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                //new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST},
                null,
                MediaStore.Audio.Media.ALBUM_ID+ "=?",
                new String[] {String.valueOf(namea)},
                null);

        if (cur != null && cur.getCount() >= 1) {
            while (cur.moveToNext()) {
                String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String name = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String duration = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
                Long album_id = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));


                tracArrayList.add(new TrackObj(title, data, artist, album, album_id));
                Log.v("fuck1",tracArrayList.get(0).getSongName());
            }

        }
        if (cur != null) {
            cur.close();
        }
        return tracArrayList;
    }
}
