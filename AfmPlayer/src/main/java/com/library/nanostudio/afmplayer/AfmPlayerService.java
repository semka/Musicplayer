package com.library.nanostudio.afmplayer;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.library.nanostudio.afmplayer.AfmPlayerExceptions.AudioAssetsInvalidException;
import com.library.nanostudio.afmplayer.AfmPlayerExceptions.AudioFilePathInvalidException;
import com.library.nanostudio.afmplayer.AfmPlayerExceptions.AudioRawInvalidException;
import com.library.nanostudio.afmplayer.AfmPlayerExceptions.AudioUrlInvalidException;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import musicplayer.mediaplayer.playlistaudio.PlayMusic;
import musicplayer.mediaplayer.playlistaudio.PlaylistPlayer;
import musicplayer.mediaplayer.playlistaudio.PlayAudio;

public class AfmPlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnErrorListener{

    private static final String TAG = AfmPlayerService.class.getSimpleName();
    RequestQueue requestQueue, rq,equestQueue;
    private final IBinder mBinder = new JcPlayerServiceBinder();
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private int duration;
    private int currentTime;
    private AfmAudio currentAfmAudio;
    private AfmStatus afmStatus = new AfmStatus();
    private List<AfmPlayerView.JcPlayerViewServiceListener> jcPlayerServiceListeners;
    private List<AfmPlayerView.OnInvalidPathListener> invalidPathListeners;
    private List<AfmPlayerView.JcPlayerViewStatusListener> jcPlayerStatusListeners;
    private AfmPlayerView.JcPlayerViewServiceListener notificationListener;
    private AssetFileDescriptor assetFileDescriptor = null;
    public class JcPlayerServiceBinder extends Binder {
        public AfmPlayerService getService() {
            return AfmPlayerService.this;
        }
    }

    public void registerNotificationListener(AfmPlayerView.JcPlayerViewServiceListener notificationListener) {
        this.notificationListener = notificationListener;
    }

    public void registerServicePlayerListener(AfmPlayerView.JcPlayerViewServiceListener jcPlayerServiceListener) {
        if (jcPlayerServiceListeners == null) {
            jcPlayerServiceListeners = new ArrayList<>();
        }

        if (!jcPlayerServiceListeners.contains(jcPlayerServiceListener)) {
            jcPlayerServiceListeners.add(jcPlayerServiceListener);
        }
    }

    public void registerInvalidPathListener(AfmPlayerView.OnInvalidPathListener invalidPathListener) {
        if (invalidPathListeners == null) {
            invalidPathListeners = new ArrayList<>();
        }

        if (!invalidPathListeners.contains(invalidPathListener)) {
            invalidPathListeners.add(invalidPathListener);
        }
    }

    public void registerStatusListener(AfmPlayerView.JcPlayerViewStatusListener statusListener) {
        if (jcPlayerStatusListeners == null) {
            jcPlayerStatusListeners = new ArrayList<>();
        }

        if (!jcPlayerStatusListeners.contains(statusListener)) {
            jcPlayerStatusListeners.add(statusListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    public AfmPlayerService() {
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void pause(AfmAudio afmAudio) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            duration = mediaPlayer.getDuration();
            currentTime = mediaPlayer.getCurrentPosition();
            isPlaying = false;
        }

        for (AfmPlayerView.JcPlayerViewServiceListener jcPlayerServiceListener : jcPlayerServiceListeners) {
            jcPlayerServiceListener.onPaused();
        }

        if (notificationListener != null) {
            notificationListener.onPaused();
        }

        if(jcPlayerStatusListeners != null) {
            for (AfmPlayerView.JcPlayerViewStatusListener jcPlayerStatusListener : jcPlayerStatusListeners) {
                afmStatus.setAfmAudio(afmAudio);
                afmStatus.setDuration(duration);
                afmStatus.setCurrentPosition(currentTime);
                afmStatus.setPlayState(AfmStatus.PlayState.PAUSE);
                jcPlayerStatusListener.onPausedStatus(afmStatus);
            }
        }
    }

    public void destroy() {
        stop();
        stopSelf();
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        isPlaying = false;
    }
    private AfmAudio tempAfmAudio;
    public void play(final AfmAudio afmAudio) {
        tempAfmAudio = this.currentAfmAudio;
        this.currentAfmAudio = afmAudio;
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        if (isAudioFileValid(afmAudio.getPath(), afmAudio.getAfmOrigin())) {

            if (afmAudio.getAfmOrigin() == AfmOrigin.URL) {
               if(afmAudio.getPath().toLowerCase().contains("ndirco.bi")){
                    StringRequest stringReque = new StringRequest(Request.Method.GET ,
                           afmAudio.getPath(),
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {


                                    Document documente = Jsoup.parse(response);
                                    Elements elemente = documente.select(".Orta_Indir_Buton a");
                                    String uu="ht" +getResources().getString(R.string.dd) + "/m"+ getResources().getString(R.string.arites) +"indirco.biz" + elemente.attr("href");
                                    playy(afmAudio,uu);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),  " yükləmə zamanı problem yarandı!!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    requestQueue.add(stringReque);

                }
                else  if(afmAudio.getPath().toLowerCase().contains("be.co")){


                    new PlaylistPlayer(getApplicationContext()) {

                        @Override
                        protected void onExtractionComplete(SparseArray<PlayMusic> ytFiles, PlayAudio videoMeta) {

                            if (ytFiles == null) {
                                 return;
                            }
                            // Iterate over itags
                            Integer l=0;
                            for (int i = 0, itag; i < ytFiles.size(); i++) {
                                itag = ytFiles.keyAt(i);
                                PlayMusic ytFile = ytFiles.get(itag);

                                if (ytFile.getFormat().getHeight() == -1) {




                                    playy(afmAudio,ytFile.getUrl());

                                }
                            }

                        }
                    }.extract(afmAudio.getPath(), false, false);
                }
                else
                playy(afmAudio, afmAudio.getPath());
                }
        }
    }

    public void playy(AfmAudio afmAudio, String u) {


        if (isAudioFileValid(afmAudio.getPath(), afmAudio.getAfmOrigin())) {
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();

                    if (afmAudio.getAfmOrigin() == AfmOrigin.URL) {
                        mediaPlayer.setDataSource(u);
                    } else if (afmAudio.getAfmOrigin() == AfmOrigin.RAW) {
                        assetFileDescriptor = getApplicationContext().getResources().openRawResourceFd(Integer.parseInt(afmAudio.getPath()));
                        if (assetFileDescriptor == null) return; // TODO: Should throw error.
                        mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),
                                assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                        assetFileDescriptor.close();
                        assetFileDescriptor = null;
                    } else if (afmAudio.getAfmOrigin() == AfmOrigin.ASSETS) {
                        assetFileDescriptor = getApplicationContext().getAssets().openFd(afmAudio.getPath());
                        mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),
                                assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                        assetFileDescriptor.close();
                        assetFileDescriptor = null;
                    } else if (afmAudio.getAfmOrigin() == AfmOrigin.FILE_PATH) {
                        mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(afmAudio.getPath()));
                    }

                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.setOnBufferingUpdateListener(this);
                    mediaPlayer.setOnCompletionListener(this);
                    mediaPlayer.setOnErrorListener(this);

                } else {
                    if (isPlaying) {
                        stop();
                        play(afmAudio);
                    } else {
                        if(tempAfmAudio != afmAudio) {
                            stop();
                            play(afmAudio);
                        } else {
                            mediaPlayer.start();
                            isPlaying = true;

                            if (jcPlayerServiceListeners != null) {
                                for (AfmPlayerView.JcPlayerViewServiceListener jcPlayerServiceListener : jcPlayerServiceListeners) {
                                    jcPlayerServiceListener.onContinueAudio();
                                }
                            }

                            if (jcPlayerStatusListeners != null) {
                                for (AfmPlayerView.JcPlayerViewStatusListener jcPlayerViewStatusListener : jcPlayerStatusListeners) {
                                    afmStatus.setAfmAudio(afmAudio);
                                    afmStatus.setPlayState(AfmStatus.PlayState.PLAY);
                                    afmStatus.setDuration(mediaPlayer.getDuration());
                                    afmStatus.setCurrentPosition(mediaPlayer.getCurrentPosition());
                                    jcPlayerViewStatusListener.onContinueAudioStatus(afmStatus);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateTimeAudio();

            for (AfmPlayerView.JcPlayerViewServiceListener jcPlayerServiceListener : jcPlayerServiceListeners) {
                jcPlayerServiceListener.onPlaying();
            }

            if (jcPlayerStatusListeners != null) {
                for (AfmPlayerView.JcPlayerViewStatusListener jcPlayerViewStatusListener : jcPlayerStatusListeners) {
                    afmStatus.setAfmAudio(afmAudio);
                    afmStatus.setPlayState(AfmStatus.PlayState.PLAY);
                    afmStatus.setDuration(0);
                    afmStatus.setCurrentPosition(0);
                    jcPlayerViewStatusListener.onPlayingStatus(afmStatus);
                }
            }

            if (notificationListener != null) notificationListener.onPlaying();

        } else {
            throwError(afmAudio.getPath(), afmAudio.getAfmOrigin());
        }
    }

    public void seekTo(int time){
        Log.d("time = ", Integer.toString(time));
        if(mediaPlayer != null) {
            mediaPlayer.seekTo(time);
        }
    }

    private void updateTimeAudio() {
        new Thread() {
            public void run() {
                while (isPlaying) {
                    try {

                        if (jcPlayerServiceListeners != null) {
                            for (AfmPlayerView.JcPlayerViewServiceListener jcPlayerServiceListener : jcPlayerServiceListeners) {
                                jcPlayerServiceListener.onTimeChanged(mediaPlayer.getCurrentPosition());
                            }
                        }
                        if (notificationListener != null) {
                            notificationListener.onTimeChanged(mediaPlayer.getCurrentPosition());
                        }

                        if (jcPlayerStatusListeners != null) {
                            for (AfmPlayerView.JcPlayerViewStatusListener jcPlayerViewStatusListener : jcPlayerStatusListeners) {
                                afmStatus.setPlayState(AfmStatus.PlayState.PLAY);
                                afmStatus.setDuration(mediaPlayer.getDuration());
                                afmStatus.setCurrentPosition(mediaPlayer.getCurrentPosition());
                                jcPlayerViewStatusListener.onTimeChangedStatus(afmStatus);
                            }
                        }
                        Thread.sleep(200);
                    } catch (IllegalStateException | InterruptedException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (jcPlayerServiceListeners != null) {
            for (AfmPlayerView.JcPlayerViewServiceListener jcPlayerServiceListener : jcPlayerServiceListeners) {
                jcPlayerServiceListener.onCompletedAudio();
            }
        }
        if (notificationListener != null) {
            notificationListener.onCompletedAudio();
        }

        if (jcPlayerStatusListeners != null) {
            for (AfmPlayerView.JcPlayerViewStatusListener jcPlayerViewStatusListener : jcPlayerStatusListeners) {
                jcPlayerViewStatusListener.onCompletedAudioStatus(afmStatus);
            }
        }
    }

    private void throwError(String path, AfmOrigin afmOrigin) {
        if (afmOrigin == AfmOrigin.URL) {
            throw new AudioUrlInvalidException(path);
        } else if (afmOrigin == AfmOrigin.RAW) {
            try {
                throw new AudioRawInvalidException(path);
            } catch (AudioRawInvalidException e) {
                e.printStackTrace();
            }
        } else if (afmOrigin == AfmOrigin.ASSETS) {
            try {
                throw new AudioAssetsInvalidException(path);
            } catch (AudioAssetsInvalidException e) {
                e.printStackTrace();
            }
        } else if (afmOrigin == AfmOrigin.FILE_PATH) {
            try {
                throw new AudioFilePathInvalidException(path);
            } catch (AudioFilePathInvalidException e) {
                e.printStackTrace();
            }
        }

        if (invalidPathListeners != null) {
            for (AfmPlayerView.OnInvalidPathListener onInvalidPathListener : invalidPathListeners) {
                onInvalidPathListener.onPathError(currentAfmAudio);
            }
        }
    }


    private boolean isAudioFileValid(String path, AfmOrigin afmOrigin) {
        if (afmOrigin == AfmOrigin.URL) {
            return path.startsWith("http") || path.startsWith("https");
        } else if (afmOrigin == AfmOrigin.RAW) {
            assetFileDescriptor = null;
            assetFileDescriptor = getApplicationContext().getResources().openRawResourceFd(Integer.parseInt(path));
            return assetFileDescriptor != null;
        } else if (afmOrigin == AfmOrigin.ASSETS) {
            try {
                assetFileDescriptor = null;
                assetFileDescriptor = getApplicationContext().getAssets().openFd(path);
                return assetFileDescriptor != null;
            } catch (IOException e) {
                e.printStackTrace(); //TODO: need to give user more readable error.
                return false;
            }
        } else if (afmOrigin == AfmOrigin.FILE_PATH) {
            File file = new File(path);
            //TODO: find an alternative to checking if file is exist, this code is slower on average.
            //read more: http://stackoverflow.com/a/8868140
            return file.exists();
        } else {
            // We should never arrive here.
            return false; // We don't know what the afmOrigin of the Audio File
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        isPlaying = true;
        this.duration = mediaPlayer.getDuration();
        this.currentTime = mediaPlayer.getCurrentPosition();
        updateTimeAudio();

        if (jcPlayerServiceListeners != null) {
            for (AfmPlayerView.JcPlayerViewServiceListener jcPlayerServiceListener : jcPlayerServiceListeners) {
                jcPlayerServiceListener.updateTitle(currentAfmAudio.getTitle());
                jcPlayerServiceListener.onPreparedAudio(currentAfmAudio.getTitle(), mediaPlayer.getDuration());
            }
        }

        if (notificationListener != null) {
            notificationListener.updateTitle(currentAfmAudio.getTitle());
            notificationListener.onPreparedAudio(currentAfmAudio.getTitle(), mediaPlayer.getDuration());
        }

        if (jcPlayerStatusListeners != null) {
            for (AfmPlayerView.JcPlayerViewStatusListener jcPlayerViewStatusListener : jcPlayerStatusListeners) {
                afmStatus.setAfmAudio(currentAfmAudio);
                afmStatus.setPlayState(AfmStatus.PlayState.PLAY);
                afmStatus.setDuration(duration);
                afmStatus.setCurrentPosition(currentTime);
                jcPlayerViewStatusListener.onPreparedAudioStatus(afmStatus);
            }
        }
    }

    public AfmAudio getCurrentAudio() {
        return currentAfmAudio;
    }
}
