package com.example.android.awesomemusicplayer.utils;

import android.widget.Filter;

import com.example.android.awesomemusicplayer.adapter.AlbumAdapter;
import com.example.android.awesomemusicplayer.adapter.TracksAdapter;
import com.example.android.awesomemusicplayer.model.AlbumObj;
import com.example.android.awesomemusicplayer.model.TrackObj;

import java.util.ArrayList;

public class SerachFilter extends Filter{

    TracksAdapter adapter;
    ArrayList<TrackObj> filterList;

    AlbumAdapter adapter1;
    ArrayList<AlbumObj> filterList1;
    boolean isAlbumList = false;

    public SerachFilter(ArrayList<TrackObj> filterList,TracksAdapter adapter)
    {
        this.adapter=adapter;
        this.filterList=filterList;

    }

    public SerachFilter(ArrayList<AlbumObj> filterList, AlbumAdapter adapter, boolean isAlbumList)
    {
        this.adapter1=adapter;
        this.filterList1=filterList;
        this.isAlbumList = isAlbumList;

    }

    //FILTERING OCURS
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        if (isAlbumList){
            return filterAlbumList(constraint);
        }else {
            return filterTrackList(constraint);
        }

    }

    private FilterResults filterTrackList(CharSequence constraint) {
        FilterResults results=new FilterResults();

        //CHECK CONSTRAINT VALIDITY
        if(constraint != null && constraint.length() > 0)
        {
            //CHANGE TO UPPER
            constraint=constraint.toString().toUpperCase();

            //STORE OUR FILTERED PLAYERS
            ArrayList<TrackObj> filteredPlayers=new ArrayList<>();

            for (int i=0;i<filterList.size();i++)
            {
                //CHECK
                if(filterList.get(i).getSongName().toUpperCase().contains(constraint))
                {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredPlayers.add(filterList.get(i));
                }
            }

            results.count=filteredPlayers.size();
            results.values=filteredPlayers;
        }else
        {
            results.count=filterList.size();
            results.values=filterList;

        }

        return results;
    }

    private FilterResults filterAlbumList(CharSequence constraint) {
        FilterResults results=new FilterResults();

        //CHECK CONSTRAINT VALIDITY
        if(constraint != null && constraint.length() > 0)
        {
            //CHANGE TO UPPER
            constraint=constraint.toString().toUpperCase();

            //STORE OUR FILTERED PLAYERS
            ArrayList<AlbumObj> filteredPlayers=new ArrayList<>();

            for (int i=0;i<filterList1.size();i++)
            {
                //CHECK
                if(filterList1.get(i).getAlbum_name().toUpperCase().contains(constraint))
                {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredPlayers.add(filterList1.get(i));
                }
            }

            results.count=filteredPlayers.size();
            results.values=filteredPlayers;
        }else
        {
            results.count=filterList1.size();
            results.values=filterList1;

        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        if (isAlbumList){
            adapter1.arrayList = (ArrayList<AlbumObj>) results.values;
            adapter1.notifyDataSetChanged();
        }else {
            adapter.arrayList= (ArrayList<TrackObj>) results.values;
            adapter.notifyDataSetChanged();
        }



    }
}