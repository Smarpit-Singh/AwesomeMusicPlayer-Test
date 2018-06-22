package com.example.android.awesomemusicplayer.adapter;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.example.android.awesomemusicplayer.R;
import com.example.android.awesomemusicplayer.model.TrackObj;
import com.example.android.awesomemusicplayer.utils.SerachFilter;

import java.util.ArrayList;


public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.TracksViewHolder> implements Filterable{

    int pos;
    public ArrayList<TrackObj> arrayList = new ArrayList<>();
    private SerachFilter filter;
    private CustomOnClickListener customOnClickListener;

    public interface CustomOnClickListener {
        void onClick(int position);
    }

    public TracksAdapter(ArrayList<TrackObj> arrayList, CustomOnClickListener customOnClickListener) {
        this.arrayList = arrayList;
        this.customOnClickListener = customOnClickListener;
    }

    @NonNull
    @Override
    public TracksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        return new TracksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TracksViewHolder holder, final int position) {


        holder.songName.setText(arrayList.get(position).getSongName());
        holder.singerName.setText(arrayList.get(position).getSongArtist());

    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class TracksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView songName, singerName;

        public TracksViewHolder(View itemView) {
            super(itemView);

            songName = itemView.findViewById(R.id.songPlaylistTitle);
            singerName = itemView.findViewById(R.id.songPlaylistArtist);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            customOnClickListener.onClick(getAdapterPosition());
        }


    }


    @Override
    public Filter getFilter() {
        if(filter==null)
        {
            filter=new SerachFilter(arrayList,this);
        }

        return filter;
    }


}
