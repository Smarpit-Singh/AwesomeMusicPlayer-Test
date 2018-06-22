package com.example.android.awesomemusicplayer.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ImageView;

import com.example.android.awesomemusicplayer.R;
import com.example.android.awesomemusicplayer.adapter.AlbumTracksAdapter;
import com.example.android.awesomemusicplayer.model.TrackObj;
import com.example.android.awesomemusicplayer.service.MusicService;
import com.example.android.awesomemusicplayer.utils.Constraint;
import com.example.android.awesomemusicplayer.utils.TracksUtils;

import java.util.ArrayList;

public class AlbumTracksListActivity extends AppCompatActivity implements AlbumTracksAdapter.AlbumListClickListener {

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private Menu collapseMenu;
    ArrayList<TrackObj> arrayList = new ArrayList<>();
    AlbumTracksAdapter adapter;
    RecyclerView listView;
    ImageView imageView;
    long id;
    String albumName;
    private boolean appBarExpanded = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);


        id = getIntent().getLongExtra("albumId", 0);
        albumName = getIntent().getStringExtra("albumName");


        toolbar = findViewById(R.id.anim_toolbar);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        appBarLayout = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        collapsingToolbar.setTitle(albumName);
        imageView = findViewById(R.id.header);
        imageView.setImageBitmap(getAlbumArt(id));


        listView = findViewById(R.id.album_tracks_list);
        listView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlbumTracksAdapter(arrayList, this);
        listView.setAdapter(adapter);

        getAlbumTrackss(id);
        Bitmap bitmap = getAlbumArt(id);
        if (bitmap != null){
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {

                @SuppressWarnings("ResourceType")
                @Override
                public void onGenerated(Palette palette) {
                    int vibrantColor = palette.getVibrantColor(R.color.primary_500);
                    collapsingToolbar.setContentScrimColor(vibrantColor);
                    collapsingToolbar.setStatusBarScrimColor(R.color.black_trans80);
                }
            });
        }



        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                appBarExpanded = Math.abs(verticalOffset) <= 200;
                invalidateOptionsMenu();
            }
        });
    }

    private Bitmap getAlbumArt(long id) {
        Bitmap bitmap = BitmapFactory.decodeFile(TracksUtils.getAlbumart(getApplicationContext(), id));

        return bitmap;


    }

    private void getAlbumTrackss(long id) {
        ArrayList<TrackObj> arr = TracksUtils.getAlbumTracks(id, getApplicationContext());
        arrayList.addAll(arr);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(String name) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(Constraint.ACTION.PLAY_THIS_ACTION);
        intent.putExtra("songIndex", name);
        startService(intent);
        finish();
    }
}
