package com.example.android.awesomemusicplayer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.android.awesomemusicplayer.R;
import com.example.android.awesomemusicplayer.model.AlbumObj;
import com.example.android.awesomemusicplayer.utils.SerachFilter;
import com.example.android.awesomemusicplayer.utils.TracksUtils;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> implements Filterable {
    private SerachFilter filter;
    public ArrayList<AlbumObj> arrayList = new ArrayList<>();
    private static Context context;
    private AlbumAdapter.CustomAlbumOnClickListener customOnClickListener;


    public interface CustomAlbumOnClickListener {
        void onClick(long pos, String name);
    }

    public AlbumAdapter(ArrayList<AlbumObj> arrayList,  Context context, AlbumAdapter.CustomAlbumOnClickListener customOnClickListener) {
        this.arrayList = arrayList;
        AlbumAdapter.context = context;
        this.customOnClickListener = customOnClickListener;
    }

    @NonNull
    @Override
    public AlbumAdapter.AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_layout,parent,false);
        return new AlbumAdapter.AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapter.AlbumViewHolder holder, int position) {
        holder.album_txt.setText(arrayList.get(position).getAlbum_name());
        long id = arrayList.get(position).getAlbum_id();

        String uri = TracksUtils.getAlbumart(context,id);
        if (uri != null){
            Glide.with(context)
                    .load(uri)
                    .into(holder.album_img);
        }

    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView album_txt;
        ImageView album_img;
        public AlbumViewHolder(View itemView) {
            super(itemView);

            album_txt = itemView.findViewById(R.id.txt_album);
            album_img = itemView.findViewById(R.id.img_album);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            customOnClickListener.onClick(arrayList.get(getAdapterPosition()).getAlbum_id(),
                    arrayList.get(getAdapterPosition()).getAlbum_name());
        }
    }

    @Override
    public Filter getFilter() {
        if(filter==null)
        {
            filter=new SerachFilter(arrayList,this, false);
        }

        return filter;
    }

}
