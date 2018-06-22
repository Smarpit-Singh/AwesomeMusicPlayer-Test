package com.example.android.awesomemusicplayer.activity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.android.awesomemusicplayer.R;
import com.example.android.awesomemusicplayer.model.AlbumObj;
import com.example.android.awesomemusicplayer.model.TrackObj;
import com.example.android.awesomemusicplayer.service.MusicService;
import com.example.android.awesomemusicplayer.utils.Constraint;
import com.example.android.awesomemusicplayer.utils.StorageUtil;
import com.example.android.awesomemusicplayer.utils.TracksUtils;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class SearchActivity extends AppCompatActivity implements
        SearchView.OnQueryTextListener{

    private SectionedRecyclerViewAdapter sectionAdapter;
    private ArrayList<TrackObj> lis = new ArrayList<>();
    private ArrayList<AlbumObj> lis2 = new ArrayList<>();
    SearchManager searchManager;
    SearchView searchView;

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ex2);


        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        sectionAdapter = new SectionedRecyclerViewAdapter();

        lis = StorageUtil.loadAudio(getApplicationContext());
        lis2 = StorageUtil.loadAlbums(getApplicationContext());

        sectionAdapter.addSection(new TarckSection("Songs", lis));
        sectionAdapter.addSection(new AlbumSection("Album", lis2));


        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(sectionAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main1, menu);

        // Associate searchable configuration with the SearchView
        searchView = (SearchView) menu.findItem(R.id.action_search1)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(this);
        searchView.setIconified(false);


        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {

        for (Section section : sectionAdapter.getCopyOfSectionsMap().values()) {
            if (section instanceof FilterableSection) {
                ((FilterableSection) section).filter(query);
            }
        }
        sectionAdapter.notifyDataSetChanged();

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Search");
        }

    }


    private  class TarckSection extends StatelessSection implements FilterableSection {

        String title;
        List<TrackObj> list;
        ArrayList<TrackObj> arrayList = new ArrayList<>();

        TarckSection(String title, ArrayList<TrackObj> list) {
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.search_section_songs)
                    .headerResourceId(R.layout.search_section_header)
                    .build());

            this.title = title;
            this.list = list;
            this.setVisible(!arrayList.isEmpty());
        }


        @Override
        public int getContentItemsTotal() {
            return arrayList.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            String tit = arrayList.get(position).getSongName();
            String txt = arrayList.get(position).getSongName();

            itemHolder.title.setText(tit);
            itemHolder.text.setText(txt);

            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MusicService.class);
                    intent.setAction(Constraint.ACTION.PLAY_THIS_ACTION);
                    intent.putExtra("songIndex",arrayList.get(position).getSongName());
                    startService(intent);

                    finish();
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new  HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(title);
        }

        @Override
        public void filter(String query) {
            if (TextUtils.isEmpty(query)) {
                //  arrayList = new ArrayList<>(list);
                this.setVisible(false);
            } else {
                arrayList.clear();
                for (TrackObj value : list) {
                    if (value.getSongName().toLowerCase().contains(query.toLowerCase())) {
                        arrayList.add(value);
                    }
                }

                this.setVisible(!arrayList.isEmpty());
            }
        }


        class ItemViewHolder extends RecyclerView.ViewHolder  {

            private final View rootView;
            TextView title, text;

            ItemViewHolder(View view) {
                super(view);

                rootView = view;
                title = view.findViewById(R.id.trackTitle);
                text = view.findViewById(R.id.trackText);

            }


        }
    }


    private class AlbumSection extends StatelessSection implements FilterableSection {

        String title;
        List<AlbumObj> list;
        ArrayList<AlbumObj> arrayList1 = new ArrayList<>();

        AlbumSection(String title, ArrayList<AlbumObj> list) {
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.search_section_albums)
                    .headerResourceId(R.layout.search_section_header)
                    .build());

            this.title = title;
            this.list = list;
            this.setVisible(!arrayList1.isEmpty());

        }

        @Override
        public int getContentItemsTotal() {
            return arrayList1.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new AlbumViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final AlbumViewHolder itemHolder = (AlbumViewHolder) holder;

            String tit = arrayList1.get(position).getAlbum_name();
            long id = arrayList1.get(position).getAlbum_id();

            itemHolder.title.setText(tit);
            //itemHolder.imageView.setImageBitmap(bitmap);

            String uri = TracksUtils.getAlbumart(getApplicationContext(),id);
            if (uri != null){
                Glide.with(getApplicationContext())
                        .load(uri)
                        .into(((AlbumViewHolder) holder).imageView);
            }

            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(getApplicationContext(),
                            AlbumTracksListActivity.class);
                    // Sending songIndex to PlayerActivity
                    in.putExtra("albumId", arrayList1.get(position).getAlbum_id());
                    in.putExtra("albumName", arrayList1.get(position).getAlbum_name());

                    startActivity(in);
                }
            });
        }


        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(title);
        }

        @Override
        public void filter(String query) {
            if (TextUtils.isEmpty(query)) {
                //  arrayList = new ArrayList<>(list);
                this.setVisible(false);
            } else {
                arrayList1.clear();
                for (AlbumObj value : list) {
                    if (value.getAlbum_name().toLowerCase().contains(query.toLowerCase())) {
                        arrayList1.add(value);
                    }
                }

                this.setVisible(!arrayList1.isEmpty());
            }
        }


        class AlbumViewHolder extends RecyclerView.ViewHolder  {

            private final View rootView;
            TextView title;
            ImageView imageView;

            AlbumViewHolder(View view) {
                super(view);

                rootView = view;
                title = view.findViewById(R.id.titleAlbum);
                imageView = view.findViewById(R.id.section_img_album);

            }

        }
    }


    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;

        HeaderViewHolder(View view) {
            super(view);

            tvTitle = view.findViewById(R.id.tvTitle);
        }
    }

    interface FilterableSection {
        void filter(String query);
    }

}
