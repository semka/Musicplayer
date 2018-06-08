package com.library.nanostudio.afmplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.library.nanostudio.afmplayer.AfmPlayerExceptions.AudioListNullPointerException;

import java.io.Serializable;
import java.util.List;

class AfmAudioPlayer {
    private AfmPlayerService afmPlayerService;
    private AfmPlayerView.JcPlayerViewServiceListener listener;
    private AfmPlayerView.OnInvalidPathListener invalidPathListener;
    private AfmPlayerView.JcPlayerViewStatusListener statusListener;
    private AfmNotificationPlayerService jcNotificationPlayer;
    private List<AfmAudio> playlist;
    private AfmAudio currentAfmAudio;
    private int currentPositionList;
    private Context context;
    private static AfmAudioPlayer instance = null;
    private boolean mBound = false;
    private boolean playing;
    private boolean paused;
    private int position = 1;
    private ConnectedListener connectedListener;

    interface ConnectedListener {

        void connected();
    }

    public AfmAudioPlayer(Context context, List<AfmAudio> playlist, AfmPlayerView.JcPlayerViewServiceListener listener) {
        this.context = context;
        this.playlist = playlist;
        this.listener = listener;
        instance = AfmAudioPlayer.this;
        this.jcNotificationPlayer = new AfmNotificationPlayerService(context);

        initService();
    }

    public void setInstance(AfmAudioPlayer instance) {
        this.instance = instance;
    }

    public void registerNotificationListener(AfmPlayerView.JcPlayerViewServiceListener notificationListener) {
        this.listener = notificationListener;
        if (jcNotificationPlayer != null) {
            afmPlayerService.registerNotificationListener(notificationListener);
        }
    }

    public void registerInvalidPathListener(AfmPlayerView.OnInvalidPathListener registerInvalidPathListener) {
        this.invalidPathListener = registerInvalidPathListener;
        if (afmPlayerService != null) {
            afmPlayerService.registerInvalidPathListener(invalidPathListener);
        }
    }

    public void registerServiceListener(AfmPlayerView.JcPlayerViewServiceListener jcPlayerServiceListener) {
        this.listener = jcPlayerServiceListener;
        if (afmPlayerService != null) {
            afmPlayerService.registerServicePlayerListener(jcPlayerServiceListener);
        }
    }

    public void registerStatusListener(AfmPlayerView.JcPlayerViewStatusListener statusListener) {
        this.statusListener = statusListener;
        if (afmPlayerService != null) {
            afmPlayerService.registerStatusListener(statusListener);
        }
    }

    public static AfmAudioPlayer getInstance() {
        return instance;
    }

    public void lazyPlayAudio(final AfmAudio audio) {
        connectedListener = new ConnectedListener() {
            @Override
            public void connected() {
                playAudio(audio);
            }
        };
    }

    public void playAudio(AfmAudio AfmAudio) throws AudioListNullPointerException {
        if (playlist == null || playlist.size() == 0) {
            throw new AudioListNullPointerException();
        }
        currentAfmAudio = AfmAudio;
        afmPlayerService.play(currentAfmAudio);
        updatePositionAudioList();
        playing = true;
        paused = false;
    }

    private void initService() {
        if (!mBound) {
            startJcPlayerService();
        } else {
            mBound = true;
        }
    }

    public void nextAudio() throws AudioListNullPointerException {
        if (playlist == null || playlist.size() == 0) {
            throw new AudioListNullPointerException();
        } else {
            if (currentAfmAudio != null) {
                try {
                    AfmAudio nextAfmAudio = playlist.get(currentPositionList + position);
                    this.currentAfmAudio = nextAfmAudio;
                    afmPlayerService.stop();
                    afmPlayerService.play(nextAfmAudio);

                } catch (IndexOutOfBoundsException e) {
                    playAudio(playlist.get(0));
                    e.printStackTrace();
                }
            }

            updatePositionAudioList();
            playing = true;
            paused = false;
        }
    }

    public void previousAudio() throws AudioListNullPointerException {
        if (playlist == null || playlist.size() == 0) {
            throw new AudioListNullPointerException();
        } else {
            if (currentAfmAudio != null) {
                try {
                    AfmAudio previousAfmAudio = playlist.get(currentPositionList - position);
                    this.currentAfmAudio = previousAfmAudio;
                    afmPlayerService.stop();
                    afmPlayerService.play(previousAfmAudio);

                } catch (IndexOutOfBoundsException e) {
                    playAudio(playlist.get(0));
                    e.printStackTrace();
                }
            }

            updatePositionAudioList();
            playing = true;
            paused = false;
        }
    }

    public void pauseAudio() {
        afmPlayerService.pause(currentAfmAudio);
        paused = true;
        playing = false;
    }

    public void continueAudio() throws AudioListNullPointerException {
        if (playlist == null || playlist.size() == 0) {
            throw new AudioListNullPointerException();
        } else {
            if (currentAfmAudio == null) {
                currentAfmAudio = playlist.get(0);
            }
            playAudio(currentAfmAudio);
            playing = true;
            paused = false;
        }
    }

    public void createNewNotification(int iconResource) {
        if (currentAfmAudio != null) {
            jcNotificationPlayer.createNotificationPlayer(currentAfmAudio.getTitle(), iconResource);
        }
    }

    public void updateNotification() {
        jcNotificationPlayer.updateNotification();
    }

    public void seekTo(int time) {
        if (afmPlayerService != null) {
            afmPlayerService.seekTo(time);
        }
    }

    private void updatePositionAudioList() {
        for (int i = 0; i < playlist.size(); i++) {
            if (playlist.get(i).getId() == currentAfmAudio.getId()) {
                this.currentPositionList = i;
            }
        }
    }

    private synchronized void startJcPlayerService() {
        if (!mBound) {
            Intent intent = new Intent(context.getApplicationContext(), AfmPlayerService.class);
            intent.putExtra(AfmNotificationPlayerService.PLAYLIST, (Serializable) playlist);
            intent.putExtra(AfmNotificationPlayerService.CURRENT_AUDIO, currentAfmAudio);
            context.bindService(intent, mConnection, context.getApplicationContext().BIND_AUTO_CREATE);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            AfmPlayerService.JcPlayerServiceBinder binder = (AfmPlayerService.JcPlayerServiceBinder) service;
            afmPlayerService = binder.getService();

            if (listener != null) {
                afmPlayerService.registerServicePlayerListener(listener);
            }
            if (invalidPathListener != null) {
                afmPlayerService.registerInvalidPathListener(invalidPathListener);
            }
            if (statusListener != null) {
                afmPlayerService.registerStatusListener(statusListener);
            }

            if (connectedListener != null) {
                connectedListener.connected();
            }

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
            playing = false;
            paused = true;
        }
    };

    boolean isPaused() {
        return paused;
    }

    boolean isPlaying() {
        return playing;
    }

    public void kill() {
        if (afmPlayerService != null) {
            afmPlayerService.stop();
            afmPlayerService.destroy();
        }

        if (mBound)
            try {
                context.unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                //TODO: Add readable exception here
            }

        if (jcNotificationPlayer != null) {
            jcNotificationPlayer.destroyNotificationIfExists();
        }

        if (AfmAudioPlayer.getInstance() != null)
            AfmAudioPlayer.getInstance().setInstance(null);
    }

    public List<AfmAudio> getPlaylist() {
        return playlist;
    }

    public AfmAudio getCurrentAudio() {
        return afmPlayerService.getCurrentAudio();
    }

}
