package com.example.android.awesomemusicplayer.utils;

public class Constraint {

    public interface ACTION {
        String MAIN_ACTION = "com.example.android.awesomemusicplayer.action.main";
        String INIT_ACTION = "com.example.android.awesomemusicplayer.action.init";
        String PREV_ACTION = "com.example.android.awesomemusicplayer.action.prev";
        String PLAY_ACTION = "com.example.android.awesomemusicplayer.action.play";
        String NEXT_ACTION = "com.example.android.awesomemusicplayer.action.next";
        String PLAY_THIS_ACTION = "com.example.android.awesomemusicplayer.action.play_this";
        String STARTFOREGROUND_ACTION = "com.example.android.awesomemusicplayer.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.example.android.awesomemusicplayer.action.stopforeground";

    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

}
