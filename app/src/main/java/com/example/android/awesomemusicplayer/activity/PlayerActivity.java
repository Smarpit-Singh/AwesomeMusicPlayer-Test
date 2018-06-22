package com.example.android.awesomemusicplayer.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.awesomemusicplayer.R;
import com.example.android.awesomemusicplayer.libraries.CircularSeekBar;
import com.example.android.awesomemusicplayer.model.TrackObj;
import com.example.android.awesomemusicplayer.service.MusicService;

import com.example.android.awesomemusicplayer.utils.Constraint;
import com.example.android.awesomemusicplayer.utils.TimeUtils;
import com.example.android.awesomemusicplayer.utils.TracksUtils;
import com.example.android.awesomemusicplayer.libraries.timely.TimelyView;


import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.security.InvalidParameterException;
import java.util.ArrayList;


public class PlayerActivity extends Activity
        implements View.OnClickListener, MusicService.Callbacks, CircularSeekBar.OnCircularSeekBarChangeListener {

    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageView btnRepeat;
    private ImageView btnShuffle;
    private CircularSeekBar songProgressBar;
    private TextView songTitleLabel;
    private ImageView albumImage, albumImageBack;
    private Handler mHandler = new Handler();
    private TimelyView timelyView11, timelyView12, timelyView13, timelyView14, timelyView15;
    private TextView hourColon;
    private int[] timeArr = new int[]{0, 0, 0, 0, 0};
    private Handler mElapsedTimeHandler = new Handler();
    private TimeUtils utils;
    private ArrayList<TrackObj> songsList = new ArrayList<>();
    MaterialDrawableBuilder builder;
    MusicService musicService;
    boolean isBinded = false;
    Intent intent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player2);

        initViews();
        initServices();

    }

    private void initServices() {
        intent = new Intent(this, MusicService.class);
        intent.setAction(Constraint.ACTION.STARTFOREGROUND_ACTION);
        if (isReadStorageAllowed()){
            startService(intent);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }else {
            requestStoragePermission();
        }
    }

    private void initViews() {

        btnPlay =  findViewById(R.id.btnPlay);
        btnForward =  findViewById(R.id.btnForward);
        btnBackward =  findViewById(R.id.btnBackward);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlaylist =  findViewById(R.id.btnPlaylist);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnShuffle = findViewById(R.id.btnShuffle);
        songProgressBar = findViewById(R.id.songProgressBar);
        songTitleLabel =  findViewById(R.id.songTitle);

        timelyView11 =  findViewById(R.id.timelyView11);
        timelyView12 =  findViewById(R.id.timelyView12);
        timelyView13 =  findViewById(R.id.timelyView13);
        timelyView14 =  findViewById(R.id.timelyView14);
        timelyView15 =  findViewById(R.id.timelyView15);
        hourColon = findViewById(R.id.hour_colon);


        albumImage = findViewById(R.id.albumImg);
        albumImageBack = findViewById(R.id.albumImg2);



        btnPlay.setOnClickListener(this);
        btnForward.setOnClickListener(this);
        btnBackward.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnPlaylist.setOnClickListener(this);
        btnRepeat.setOnClickListener(this);
        btnShuffle.setOnClickListener(this);

        if (isReadStorageAllowed()){
            songsList = TracksUtils.getTracks(getApplicationContext());
        }else {
            requestStoragePermission();
        }

        songProgressBar.setOnSeekBarChangeListener(this); // Important

        utils = new TimeUtils();

        initIcons();
        if (musicService == null){
            updateRepeatButton(false);
            updateShuffledButton(false);
        }else {
            updateRepeatButton(musicService.isRepeat);
            updateShuffledButton(musicService.isShuffle);
        }


    }

    private void initIcons() {
        builder = MaterialDrawableBuilder.with(getApplicationContext())
                .setSizeDp(40)
                .setColor(Color.WHITE);

        builder.setIcon(MaterialDrawableBuilder.IconValue.SKIP_NEXT);
        btnNext.setImageDrawable(builder.build());

        builder.setIcon(MaterialDrawableBuilder.IconValue.SKIP_PREVIOUS);
        btnPrevious.setImageDrawable(builder.build());

        builder.setIcon(MaterialDrawableBuilder.IconValue.PLAY)
        .setSizeDp(55);
        btnPlay.setImageDrawable(builder.build());

        builder.setSizeDp(40);
        builder.setIcon(MaterialDrawableBuilder.IconValue.SKIP_FORWARD);
        btnForward.setImageDrawable(builder.build());

        builder.setIcon(MaterialDrawableBuilder.IconValue.SKIP_BACKWARD);
        btnBackward.setImageDrawable(builder.build());


    }

    public Runnable mUpdateElapsedTime = new Runnable() {
        @Override
        public void run() {

            String time = TimeUtils.makeShortTimeString(getApplicationContext(), musicService.mp.getCurrentPosition() / 1000);
            if (time.length() < 5) {
                timelyView11.setVisibility(View.GONE);
                timelyView12.setVisibility(View.GONE);
                hourColon.setVisibility(View.GONE);
                tv13(time.charAt(0) - '0');
                tv14(time.charAt(2) - '0');
                tv15(time.charAt(3) - '0');
            } else if (time.length() == 5) {
                timelyView12.setVisibility(View.VISIBLE);
                tv12(time.charAt(0) - '0');
                tv13(time.charAt(1) - '0');
                tv14(time.charAt(3) - '0');
                tv15(time.charAt(4) - '0');
            } else {
                timelyView11.setVisibility(View.VISIBLE);
                hourColon.setVisibility(View.VISIBLE);
                tv11(time.charAt(0) - '0');
                tv12(time.charAt(2) - '0');
                tv13(time.charAt(3) - '0');
                tv14(time.charAt(5) - '0');
                tv15(time.charAt(6) - '0');
            }
            mElapsedTimeHandler.postDelayed(this, 600);
        }


    };

    public void changeDigit(TimelyView tv, int end) {
        android.animation.ObjectAnimator obja = tv.animate(end);
        obja.setDuration(400);
        obja.start();
    }

    public void changeDigit(TimelyView tv, int start, int end) {
        try {
            android.animation.ObjectAnimator obja = tv.animate(start, end);
            obja.setDuration(400);
            obja.start();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        }
    }

    public void tv11(int a) {
        if (a != timeArr[0]) {
            changeDigit(timelyView11, timeArr[0], a);
            timeArr[0] = a;
        }
    }

    public void tv12(int a) {
        if (a != timeArr[1]) {
            changeDigit(timelyView12, timeArr[1], a);
            timeArr[1] = a;
        }
    }

    public void tv13(int a) {
        if (a != timeArr[2]) {
            changeDigit(timelyView13, timeArr[2], a);
            timeArr[2] = a;
        }
    }

    public void tv14(int a) {
        if (a != timeArr[3]) {
            changeDigit(timelyView14, timeArr[3], a);
            timeArr[3] = a;
        }
    }

    public void tv15(int a) {
        if (a != timeArr[4]) {
            changeDigit(timelyView15, timeArr[4], a);
            timeArr[4] = a;
        }
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
            musicService = myBinder.getService();
            isBinded = true;
            musicService.registerClient(PlayerActivity.this);
            if (musicService.mp.isPlaying() || musicService.isPaused()) {
                int index = musicService.getCurrentIndex();
                updateUI(musicService.getSongTitile(index), index);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBinded = false;
        }
    };


    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
        if (timelyView11 != null) {
            mElapsedTimeHandler.postDelayed(mUpdateElapsedTime, 600);
        }
    }


    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (isBinded) {

                long totalDuration = musicService.mp.getDuration();
                long currentDuration = musicService.mp.getCurrentPosition();

                int progress = TimeUtils.getProgressPercentage(currentDuration, totalDuration);
                songProgressBar.setProgress(progress);

                mHandler.postDelayed(this, 50);
            }

        }
    };


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (isBinded) {
            switch (id) {
                case R.id.btnPlay: // Play button clicked

                    musicService.btnPlayClicked();
                    updatePlayPauseButon();
                    break;


                case R.id.btnForward:  // Forward button clicked

                    musicService.btnForwardClicked();
                    break;


                case R.id.btnBackward:  // Backward button clicked

                    musicService.btnBackwardClicked();
                    break;


                case R.id.btnNext:    // next button clicked
                    musicService.btnNextClicked();

                    break;


                case R.id.btnPrevious:  //previous button clicked
                    musicService.btnPreviousClicked();

                    break;


                case R.id.btnPlaylist:  // Playlist button clicked

                    Intent i = new Intent(getApplicationContext(), TabActivity.class);
                    startActivity(i);
                    break;


                case R.id.btnRepeat:   // Repeat button clicked

                    musicService.btnRepeateClicked();
                    break;


                case R.id.btnShuffle:  // Shuffle button clicked

                    musicService.btnShuffled();
                    break;

            }
        }

    }


    @Override
    public void updateUI(String name, int id) {
        songTitleLabel.setText(name);


        updatePlayPauseButon();
        updateProgressBar();

        // Album cover
        long id2 = songsList.get(id).getAlbumId();
        String path = TracksUtils.getAlbumart(getApplicationContext(), id2);
        Bitmap bitmapFactory = BitmapFactory.decodeFile(path);

        if (bitmapFactory == null) {
            bitmapFactory = BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art);
        }

        albumImage.setImageBitmap(bitmapFactory);
        Bitmap bitmap = blur(getApplicationContext(), bitmapFactory);
        albumImageBack.setImageBitmap(bitmap);

        changeTimelyText();
    }

    @Override
    public void updateSongTitle(String title) {
        songTitleLabel.setText(title);
    }

    @Override
    public void updatePlayButton() {
        updatePlayPauseButon();
    }

    @Override
    public void updateRepeatButton(boolean is) {
       MaterialDrawableBuilder builder1 = MaterialDrawableBuilder.with(getApplicationContext())
               .setSizeDp(40)
        .setIcon(MaterialDrawableBuilder.IconValue.REPEAT);

        if (is) {
            builder1.setColor(Color.MAGENTA);
        } else builder1.setColor(Color.WHITE);

        btnRepeat.setImageDrawable(builder1.build());

    }

    @Override
    public void updateShuffledButton(boolean is) {

        MaterialDrawableBuilder builder2 = MaterialDrawableBuilder.with(getApplicationContext())
                .setSizeDp(40)
        .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE);

        if (is) {
            builder2.setColor(Color.MAGENTA);
        } else builder2.setColor(Color.WHITE);

        btnShuffle.setImageDrawable(builder2.build());

    }


    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (isBinded) {

                int totalDuration = musicService.mp.getDuration();
                int currentPosition = TimeUtils.progressToTimer(circularSeekBar.getProgress(), totalDuration);

                musicService.seekTo(currentPosition);
                updateProgressBar();
                //-------------------------------- Here is News
                updatePlayPauseButon();
            }
        }
    }

    @Override
    public void onStopTrackingTouch(CircularSeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(CircularSeekBar seekBar) {

    }

    public void changeTimelyText() {
        if (timelyView11 != null) {
            String time = TimeUtils.makeShortTimeString(getApplicationContext(), musicService.mp.getCurrentPosition() / 1000);
            if (time.length() < 5) {
                timelyView11.setVisibility(View.GONE);
                timelyView12.setVisibility(View.GONE);
                hourColon.setVisibility(View.GONE);

                changeDigit(timelyView13, time.charAt(0) - '0');
                changeDigit(timelyView14, time.charAt(2) - '0');
                changeDigit(timelyView15, time.charAt(3) - '0');

            } else if (time.length() == 5) {
                timelyView12.setVisibility(View.VISIBLE);
                changeDigit(timelyView12, time.charAt(0) - '0');
                changeDigit(timelyView13, time.charAt(1) - '0');
                changeDigit(timelyView14, time.charAt(3) - '0');
                changeDigit(timelyView15, time.charAt(4) - '0');
            } else {
                timelyView11.setVisibility(View.VISIBLE);
                hourColon.setVisibility(View.VISIBLE);
                changeDigit(timelyView11, time.charAt(0) - '0');
                changeDigit(timelyView12, time.charAt(2) - '0');
                changeDigit(timelyView13, time.charAt(3) - '0');
                changeDigit(timelyView14, time.charAt(5) - '0');
                changeDigit(timelyView15, time.charAt(6) - '0');
            }
        }
    }


    private static final float BITMAP_SCALE = 0.6f;
    private static final float BLUR_RADIUS = 15f;

    public Bitmap blur(Context context, Bitmap image) {
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);

        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        intrinsicBlur.setRadius(BLUR_RADIUS);
        intrinsicBlur.setInput(tmpIn);
        intrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    public void updatePlayPauseButon() {
        MaterialDrawableBuilder builder1 = MaterialDrawableBuilder.with(getApplicationContext())
                .setColor(Color.WHITE);
        if (musicService.mp.isPlaying()) {
            builder1.setIcon(MaterialDrawableBuilder.IconValue.PAUSE)
                    .setSizeDp(55);

            btnPlay.setImageDrawable(builder1.build());
        } else {
            builder1.setIcon(MaterialDrawableBuilder.IconValue.PLAY)
                    .setSizeDp(55);

            btnPlay.setImageDrawable(builder1.build());
        }
    }



    // Permissio Stuff here

    private int STORAGE_PERMISSION_CODE = 23;

    public boolean isReadStorageAllowed() {

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);


        return result == PackageManager.PERMISSION_GRANTED;

    }


    public void requestStoragePermission(){


        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){

        }

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if(requestCode == STORAGE_PERMISSION_CODE){


            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Toast.makeText(this,"Permission granted now you can read the storage",Toast.LENGTH_LONG).show();
                initViews();
                initServices();
            }else{

                Toast.makeText(this,"Oops you just denied the permission",Toast.LENGTH_LONG).show();
                initViews();
                initServices();
            }
        }
    }


}
