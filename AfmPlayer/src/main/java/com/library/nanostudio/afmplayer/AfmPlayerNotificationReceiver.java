package com.library.nanostudio.afmplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.library.nanostudio.afmplayer.AfmPlayerExceptions.AudioListNullPointerException;

public class AfmPlayerNotificationReceiver extends BroadcastReceiver {
    public AfmPlayerNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AfmAudioPlayer afmAudioPlayer = AfmAudioPlayer.getInstance();
        String action = "";

        if (intent.hasExtra(AfmNotificationPlayerService.ACTION)) {
            action = intent.getStringExtra(AfmNotificationPlayerService.ACTION);
        }

        switch (action) {
            case AfmNotificationPlayerService.PLAY:
                try {
                    afmAudioPlayer.continueAudio();
                    afmAudioPlayer.updateNotification();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case AfmNotificationPlayerService.PAUSE:
                try {
                    if(afmAudioPlayer != null) {
                        afmAudioPlayer.pauseAudio();
                        afmAudioPlayer.updateNotification();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case AfmNotificationPlayerService.NEXT:
                try {
                    afmAudioPlayer.nextAudio();
                } catch (AudioListNullPointerException e) {
                    try {
                        afmAudioPlayer.continueAudio();
                    } catch (AudioListNullPointerException e1) {
                        e1.printStackTrace();
                    }
                }
                break;

            case AfmNotificationPlayerService.PREVIOUS:
                try {
                    afmAudioPlayer.previousAudio();
                } catch (Exception e) {
                    try {
                        afmAudioPlayer.continueAudio();
                    } catch (AudioListNullPointerException e1) {
                        e1.printStackTrace();
                    }
                }
                break;
        }
    }
}
