package com.example.android.awesomemusicplayer.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.android.awesomemusicplayer.R;
import com.example.android.awesomemusicplayer.adapter.TracksAdapter;
import com.example.android.awesomemusicplayer.model.TrackObj;
import com.example.android.awesomemusicplayer.service.MusicService;
import com.example.android.awesomemusicplayer.utils.Constraint;
import com.example.android.awesomemusicplayer.utils.StorageUtil;

import java.util.ArrayList;

public class FragmentPlayList extends Fragment implements TracksAdapter.CustomOnClickListener {

	private ArrayList<TrackObj> songsList = new ArrayList<>();
	RecyclerView recyclerView;
	TracksAdapter adapter;
	ProgressBar progressBar;


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.playlist,container,false);

		progressBar = view.findViewById(R.id.progressBar);
		recyclerView = view.findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		adapter = new TracksAdapter(songsList, this);
		recyclerView.setAdapter(adapter);

		new BackgroundThread().execute();

		return view;
	}



	public void searchIt(String q) {
			adapter.getFilter().filter(q);

	}


	class BackgroundThread extends AsyncTask<Void,Void,ArrayList<TrackObj>>{

		@Override
		protected ArrayList<TrackObj> doInBackground(Void... voids) {
			return StorageUtil.loadAudio(getActivity());
		}

		@Override
		protected void onPostExecute(ArrayList<TrackObj> trackObjs) {
			super.onPostExecute(trackObjs);
			progressBar.setVisibility(View.INVISIBLE);
			songsList.addAll(trackObjs);
			adapter.notifyDataSetChanged();
		}
	}


	@Override
	public void onClick(int position) {

		Intent intent = new Intent(getActivity(), MusicService.class);
		intent.setAction(Constraint.ACTION.PLAY_THIS_ACTION);
		intent.putExtra("songIndex",position);
		getActivity().startService(intent);
		getActivity().finish();
	}




}
