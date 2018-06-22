package com.example.android.awesomemusicplayer.service;


import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.android.awesomemusicplayer.R;
import com.example.android.awesomemusicplayer.model.TrackObj;
import com.example.android.awesomemusicplayer.utils.StorageUtil;
import com.example.android.awesomemusicplayer.utils.TimeUtils;
import com.example.android.awesomemusicplayer.utils.TracksUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.example.android.awesomemusicplayer.utils.Constraint.ACTION;
import static com.example.android.awesomemusicplayer.utils.Constraint.ACTION.NEXT_ACTION;
import static com.example.android.awesomemusicplayer.utils.Constraint.ACTION.PLAY_ACTION;
import static com.example.android.awesomemusicplayer.utils.Constraint.ACTION.PLAY_THIS_ACTION;
import static com.example.android.awesomemusicplayer.utils.Constraint.ACTION.PREV_ACTION;
import static com.example.android.awesomemusicplayer.utils.Constraint.ACTION.STOPFOREGROUND_ACTION;


public class MusicService extends Service implements
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {


    Callbacks activity;
    public MediaPlayer mp;
    private TracksUtils tracksUtils;
    private TimeUtils utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    public boolean isShuffle = false;
    public boolean isRepeat = false;
    private ArrayList<TrackObj> songsList = new ArrayList<>();
    private NotificationManagerCompat mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mRemoteViews;
    private static final int NOTIFY_MODE_NONE = 0;
    private IBinder iBinder = new MyBinder();
    public static final String CHANNEL_ID = "channel_01";
    private boolean isPaused = false;
    private int NOTIF_ID = 1000;

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    //AudioFocus
    private AudioManager audioManager;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;


    public enum PlaybackStatus {
        PLAYING,
        PAUSED
    }


    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    public interface Callbacks {
        void updateUI(String name, int id);

        void updateSongTitle(String title);

        void updatePlayButton();

        void updateRepeatButton(boolean is);

        void updateShuffledButton(boolean is);
    }


    public void registerClient(Activity activity) {
        this.activity = (Callbacks) activity;

    }


    @Override
    public void onCreate() {
        super.onCreate();


        callStateListener();
        registerBecomingNoisyReceiver();

        initMediaPlayer();

        tracksUtils = new TracksUtils();
        utils = new TimeUtils();
        songsList = TracksUtils.getTracks(getApplicationContext());
        StorageUtil.storeAudio(songsList, getApplicationContext());

    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (requestAudioFocus() == false) {

            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();

            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        if (intent != null && intent.getAction() != null) {
            handleIncomingActions(intent);
        }

        return START_STICKY;
    }


    private void initMediaPlayer() {

        if (mp == null)
            mp = new MediaPlayer();

        mp.setOnCompletionListener(this);
        mp.setOnErrorListener(this);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);


    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "name";
            String description = "description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaSession.release();
        removeNotification();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        if (mp != null) {
            mp.release();
        }
        removeAudioFocus();

        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        unregisterReceiver(becomingNoisyReceiver);

        StorageUtil.clearCachedAudioPlaylist(getApplicationContext());
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {

        if (isRepeat) {

            playSong(currentSongIndex);
        } else if (isShuffle) {

            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSong(currentSongIndex);
        } else {

            if (currentSongIndex < (songsList.size() - 1)) {
                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            } else {

                playSong(0);
                currentSongIndex = 0;
            }
        }

    }

    public void playSong(int songIndex) {

        try {
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).getSongPath());
            mp.prepare();
            mp.start();

            activity.updateUI(getSongTitile(songIndex), songIndex);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getArtistName() {
        return songsList.get(currentSongIndex).getSongArtist();
    }

    public Long getAlbumId() {
        return songsList.get(currentSongIndex).getAlbumId();
    }

    public Bitmap getAlbumIcon() {
        Bitmap bitmap = BitmapFactory.decodeFile(TracksUtils.getAlbumart(getApplicationContext(), getAlbumId()));

        if (bitmap != null) {
            return bitmap;
        } else {
            return BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art);
        }

    }

    public String getSongTitile(int index) {
        return songsList.get(index).getSongName();
    }

    public int getCurrentIndex() {
        return currentSongIndex;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setIndex(int index) {
        currentSongIndex = index;
    }

    public void seekTo(int milliSec) {
        mp.seekTo(milliSec);
    }

    public void btnPlayClicked() {
        if (mp.isPlaying()) {
            if (mp != null) {
                mp.pause();
                isPaused = true;
                activity.updatePlayButton();

                buildNotification(PlaybackStatus.PAUSED);
            }

        } else {
            if (mp != null) {
                mp.start();
                isPaused = false;
                activity.updatePlayButton();

                buildNotification(PlaybackStatus.PLAYING);
            }

        }

    }

    public void btnForwardClicked() {
        int currentPosition = mp.getCurrentPosition();
        if (currentPosition + seekForwardTime <= mp.getDuration()) {
            mp.seekTo(currentPosition + seekForwardTime);
        } else {
            mp.seekTo(mp.getDuration());
        }
    }

    public void btnBackwardClicked() {
        int currentPosition2 = mp.getCurrentPosition();
        if (currentPosition2 - seekBackwardTime >= 0) {
            mp.seekTo(currentPosition2 - seekBackwardTime);
        } else {
            mp.seekTo(0);
        }
    }

    public void btnNextClicked() {
        if (currentSongIndex < (songsList.size() - 1)) {
            playSong(currentSongIndex + 1);
            setIndex(currentSongIndex + 1);
        } else {
            playSong(0);
            setIndex(0);
        }

        buildNotification(PlaybackStatus.PLAYING);

    }

    public void btnPreviousClicked() {
        if (currentSongIndex > 0) {
            playSong(currentSongIndex - 1);
            setIndex(currentSongIndex - 1);
        } else {
            playSong(songsList.size() - 1);
            setIndex(songsList.size() - 1);
        }

        buildNotification(PlaybackStatus.PLAYING);
    }

    public void btnRepeateClicked() {
        if (isRepeat) {
            isRepeat = false;
            Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
            activity.updateRepeatButton(isRepeat);
        } else {

            isRepeat = true;
            Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();

            isShuffle = false;
            activity.updateRepeatButton(isRepeat);
            activity.updateShuffledButton(isShuffle);
        }
    }

    public void btnShuffled() {
        if (isShuffle) {
            isShuffle = false;
            Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
            activity.updateShuffledButton(isShuffle);
        } else {

            isShuffle = true;
            Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();

            isRepeat = false;
            activity.updateShuffledButton(isShuffle);
            activity.updateRepeatButton(isRepeat);
        }

    }


    // Notification ----------------------------------------------------------------------

    public void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction = null;


        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;

            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;

            play_pauseAction = playbackAction(0);
        }


        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);

        mRemoteViews.setImageViewBitmap(R.id.img_notification, getAlbumIcon());
        mRemoteViews.setTextViewText(R.id.txt_song_title, getSongTitile(currentSongIndex));
        mRemoteViews.setTextViewText(R.id.txt_song_singer, getArtistName());
        mRemoteViews.setOnClickPendingIntent(R.id.noti_play, play_pauseAction);
        mRemoteViews.setOnClickPendingIntent(R.id.noti_pause, play_pauseAction);
        if (playbackStatus == PlaybackStatus.PLAYING) {
            mRemoteViews.setViewVisibility(R.id.noti_play, View.GONE);
            mRemoteViews.setViewVisibility(R.id.noti_pause, View.VISIBLE);
        } else {
            mRemoteViews.setViewVisibility(R.id.noti_play, View.VISIBLE);
            mRemoteViews.setViewVisibility(R.id.noti_pause, View.GONE);
        }

        mRemoteViews.setOnClickPendingIntent(R.id.noti_previous, playbackAction(3));
        mRemoteViews.setOnClickPendingIntent(R.id.noti_next, playbackAction(2));

        mBuilder =  new NotificationCompat.Builder(this)
                // Hide the timestamp
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compat view
                        .setShowActionsInCompactView(0, 1, 2))
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle(getSongTitile(currentSongIndex))
                .setContentText(getArtistName())
                .setLargeIcon(getAlbumIcon())
                .setCustomBigContentView(mRemoteViews)
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorAccent))
                // Set the large and small icons
                // .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                // .setContentText(getSongTitile(currentSongIndex))
                //.setContentTitle(getArtistName())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        startForeground(NOTIF_ID, mBuilder.build());
    }

    private PendingIntent playbackAction(int actionNumber) {

        Intent playbackAction = new Intent(this, MusicService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION.PLAY_ACTION);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION.PLAY_ACTION);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(NEXT_ACTION);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(PREV_ACTION);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(PLAY_ACTION)) {
            // transportControls.play();
            // buildNotification(PlaybackStatus.PLAYING);
            btnPlayClicked();
        } else if (actionString.equalsIgnoreCase(NEXT_ACTION)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(PREV_ACTION)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(STOPFOREGROUND_ACTION)) {
            transportControls.stop();
        } else if (actionString.equalsIgnoreCase(PLAY_THIS_ACTION)) {

            int index = playbackAction.getIntExtra("songIndex", -1);
            String name = playbackAction.getStringExtra("songIndex");

            if (index != -1) {
                playSong(index);
                currentSongIndex = index;
                buildNotification(PlaybackStatus.PLAYING);
                updateNoti(PlaybackStatus.PLAYING);
            } else if (name != null) {
                playSongWithName(name);
                buildNotification(PlaybackStatus.PLAYING);
                updateNoti(PlaybackStatus.PLAYING);
            }


        }
        updateMetaData();
    }

    private void updateNoti(PlaybackStatus playbackStatus) {

        if (playbackStatus == PlaybackStatus.PLAYING) {
            mRemoteViews.setViewVisibility(R.id.noti_play, View.GONE);
            mRemoteViews.setViewVisibility(R.id.noti_pause, View.VISIBLE);
        } else {
            mRemoteViews.setViewVisibility(R.id.noti_play, View.VISIBLE);
            mRemoteViews.setViewVisibility(R.id.noti_pause, View.GONE);
        }

        mBuilder.setContentTitle(getSongTitile(currentSongIndex));
        mBuilder.setContentText(getArtistName());
        mBuilder.setLargeIcon(getAlbumIcon());
        mBuilder.setContent(mRemoteViews);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_ID, mBuilder.build());
    }

    private void playSongWithName(String name) {
        for (int i = 0; i < songsList.size(); i++) {
            if (songsList.get(i).getSongName().equalsIgnoreCase(name)) {
                playSong(i);
                currentSongIndex = i;
                break;
            }
        }
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIF_ID);
    }

    private void updateMetaData() {

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getAlbumIcon())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getArtistName())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getSongTitile(currentSongIndex))
                .build());
    }


    // Media Session ------------------------------------------------------------------------

    private void initMediaSession() {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        }
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();

                btnPlayClicked();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();

                btnPlayClicked();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

                btnNextClicked();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();

                btnPreviousClicked();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }


    // Call state listener -------------------------------------------------------------------

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            btnPlayClicked();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mp != null) {
                            btnPlayClicked();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mp != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                btnPlayClicked();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }


    // Audio focus  ----------------------------------------------------------------------------

    @Override
    public void onAudioFocusChange(int focusState) {

        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mp == null) initMediaPlayer();
                else if (!mp.isPlaying()) mp.start();
                mp.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mp.isPlaying()) mp.stop();
                mp.release();
                mp = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mp.isPlaying()) mp.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mp.isPlaying()) mp.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        //Could not gain focus
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }


}

