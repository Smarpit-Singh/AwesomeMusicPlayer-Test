package com.example.android.awesomemusicplayer.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.awesomemusicplayer.R;
import com.example.android.awesomemusicplayer.model.TrackObj;

import java.util.ArrayList;


public class AlbumTracksAdapter extends RecyclerView.Adapter<AlbumTracksAdapter.AlbumListHolder>{

    ArrayList<TrackObj> albumObjArrayList = new ArrayList<>();
    public AlbumListClickListener albumListClickListener;

    public AlbumTracksAdapter(ArrayList<TrackObj> albumObjArrayList, AlbumListClickListener albumListClickListener) {
        this.albumObjArrayList = albumObjArrayList;
        this.albumListClickListener = albumListClickListener;
    }

    @NonNull
    @Override
    public AlbumListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item,parent,false);
        return new AlbumListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumListHolder holder, int position) {
        holder.title.setText(albumObjArrayList.get(position).getSongName());
        holder.artist.setText(albumObjArrayList.get(position).getSongArtist());
    }

    @Override
    public int getItemCount() {
        return albumObjArrayList.size();
    }

    public interface AlbumListClickListener{
        void onClick(String name);
    }



    class AlbumListHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView title, artist;

        public AlbumListHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.songPlaylistTitle);
            artist = itemView.findViewById(R.id.songPlaylistArtist);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            albumListClickListener.onClick(albumObjArrayList.get(getAdapterPosition()).getSongName());
        }
    }
}