package com.library.nanostudio.afmplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.library.nanostudio.afmplayer.AfmPlayerExceptions.AudioListNullPointerException;

import java.util.ArrayList;
import java.util.List;

public class AfmPlayerView extends LinearLayout implements
        View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = AfmPlayerView.class.getSimpleName();

    private static final int PULSE_ANIMATION_DURATION = 200;
    private static final int TITLE_ANIMATION_DURATION = 600;

    private TextView txtCurrentMusic;
    private ImageButton btnPrev;
    private ImageButton btnPlay;
    private ProgressBar progressBarPlayer;
    private AfmAudioPlayer afmAudioPlayer;
    private TextView txtDuration;
    private ImageButton btnNext;
    private SeekBar seekBar;
    private TextView txtCurrentDuration;
    private boolean isInitialized;

    private OnInvalidPathListener onInvalidPathListener = new OnInvalidPathListener() {
        @Override
        public void onPathError(AfmAudio afmAudio) {
            dismissProgressBar();
        }
    };

    JcPlayerViewServiceListener jcPlayerViewServiceListener = new JcPlayerViewServiceListener() {

        @Override
        public void onPreparedAudio(String audioName, int duration) {
            dismissProgressBar();
            resetPlayerInfo();

            long aux = duration / 1000;
            int minute = (int) (aux / 60);
            int second = (int) (aux % 60);

            final String sDuration = // Minutes
                    (minute < 10 ? "0" + minute : minute + "")
                            + ":" +
                            // Seconds
                            (second < 10 ? "0" + second : second + "");

            seekBar.setMax(duration);

            txtDuration.post(new Runnable() {
                @Override
                public void run() {
                    txtDuration.setText(sDuration);
                }
            });
        }

        @Override
        public void onCompletedAudio() {
            resetPlayerInfo();

            try {
                afmAudioPlayer.nextAudio();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPaused() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_play_white, null));
            } else {
                btnPlay.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_play_white, null));
            }
            btnPlay.setTag(R.drawable.ic_play_white);
        }

        @Override
        public void onContinueAudio() {
            dismissProgressBar();
        }

        @Override
        public void onPlaying() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_pause_white, null));
            } else {
                btnPlay.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_pause_white, null));
            }
            btnPlay.setTag(R.drawable.ic_pause_white);
        }

        @Override
        public void onTimeChanged(long currentPosition) {
            long aux = currentPosition / 1000;
            int minutes = (int) (aux / 60);
            int seconds = (int) (aux % 60);
            final String sMinutes = minutes < 10 ? "0" + minutes : minutes + "";
            final String sSeconds = seconds < 10 ? "0" + seconds : seconds + "";

            seekBar.setProgress((int) currentPosition);
            txtCurrentDuration.post(new Runnable() {
                @Override
                public void run() {
                    txtCurrentDuration.setText(String.valueOf(sMinutes + ":" + sSeconds));
                }
            });
        }

        @Override
        public void updateTitle(final String title) {
//            final String mTitle = title;

            YoYo.with(Techniques.FadeInLeft)
                    .duration(TITLE_ANIMATION_DURATION)
                    .playOn(txtCurrentMusic);

            txtCurrentMusic.post(new Runnable() {
                @Override
                public void run() {
                    txtCurrentMusic.setText(title);
                }
            });
        }
    };

    //JcPlayerViewStatusListener jcPlayerViewStatusListener = new JcPlayerViewStatusListener() {
    //
    //    @Override public void onPausedStatus(AfmStatus jcStatus) {
    //
    //    }
    //
    //    @Override public void onContinueAudioStatus(AfmStatus jcStatus) {
    //
    //    }
    //
    //    @Override public void onPlayingStatus(AfmStatus jcStatus) {
    //
    //    }
    //
    //    @Override public void onTimeChangedStatus(AfmStatus jcStatus) {
    //        Log.d(TAG, "song id = " + jcStatus.getAfmAudio().getId() + ", position = " + jcStatus.getCurrentPosition());
    //    }
    //
    //    @Override public void onCompletedAudioStatus(AfmStatus jcStatus) {
    //
    //    }
    //
    //    @Override public void onPreparedAudioStatus(AfmStatus jcStatus) {
    //
    //    }
    //};

    public interface OnInvalidPathListener {
        void onPathError(AfmAudio afmAudio);
    }

    public interface JcPlayerViewStatusListener {
        void onPausedStatus(AfmStatus afmStatus);

        void onContinueAudioStatus(AfmStatus afmStatus);

        void onPlayingStatus(AfmStatus afmStatus);

        void onTimeChangedStatus(AfmStatus afmStatus);

        void onCompletedAudioStatus(AfmStatus afmStatus);

        void onPreparedAudioStatus(AfmStatus afmStatus);
    }

    public interface JcPlayerViewServiceListener {
        void onPreparedAudio(String audioName, int duration);

        void onCompletedAudio();

        void onPaused();

        void onContinueAudio();

        void onPlaying();

        void onTimeChanged(long currentTime);

        void updateTitle(String title);
    }

    public AfmPlayerView(Context context) {
        super(context);
        init();
    }

    public AfmPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AfmPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        inflate(getContext(), R.layout.view_jcplayer, this);

        this.progressBarPlayer = (ProgressBar) findViewById(R.id.progress_bar_player);
        this.btnNext = (ImageButton) findViewById(R.id.btn_next);
        this.btnPrev = (ImageButton) findViewById(R.id.btn_prev);
        this.btnPlay = (ImageButton) findViewById(R.id.btn_play);
        this.txtDuration = (TextView) findViewById(R.id.txt_total_duration);
        this.txtCurrentDuration = (TextView) findViewById(R.id.txt_current_duration);
        this.txtCurrentMusic = (TextView) findViewById(R.id.txt_current_music);
        this.seekBar = (SeekBar) findViewById(R.id.seek_bar);
        this.btnPlay.setTag(R.drawable.ic_play_white);

        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
    }

    /**
     * Initialize the playlist and controls.
     *
     * @param playlist List of AfmAudio objects that you want play
     */
    public void initPlaylist(List<AfmAudio> playlist) {
        // Don't sort if the playlist have position number.
        // We need to do this because there is ic_previous_black possibility that the user reload previous playlist
        // from persistence storage like sharedPreference or SQLite.
        if (!isAlreadySorted(playlist)) {
            sortPlaylist(playlist);
        }
        afmAudioPlayer = new AfmAudioPlayer(getContext(), playlist, jcPlayerViewServiceListener);
        afmAudioPlayer.registerInvalidPathListener(onInvalidPathListener);
        //afmAudioPlayer.registerStatusListener(jcPlayerViewStatusListener);
        isInitialized = true;
    }

    /**
     * Initialize an anonymous playlist with ic_previous_black default JcPlayer title for all audios
     *
     * @param playlist List of urls strings
     */
    public void initAnonPlaylist(List<AfmAudio> playlist) {
        sortPlaylist(playlist);
        generateTitleAudio(playlist, getContext().getString(R.string.track_number));
        afmAudioPlayer = new AfmAudioPlayer(getContext(), playlist, jcPlayerViewServiceListener);
        afmAudioPlayer.registerInvalidPathListener(onInvalidPathListener);
        //afmAudioPlayer.registerStatusListener(jcPlayerViewStatusListener);
        isInitialized = true;
    }

    /**
     * Initialize an anonymous playlist, but with ic_previous_black custom title for all audios
     *
     * @param playlist List of AfmAudio files.
     * @param title    Default title for all audios
     */
    public void initWithTitlePlaylist(List<AfmAudio> playlist, String title) {
        sortPlaylist(playlist);
        generateTitleAudio(playlist, title);
        afmAudioPlayer = new AfmAudioPlayer(getContext(), playlist, jcPlayerViewServiceListener);
        afmAudioPlayer.registerInvalidPathListener(onInvalidPathListener);
        //afmAudioPlayer.registerStatusListener(jcPlayerViewStatusListener);
        isInitialized = true;
    }

    //TODO: Should we expose this to user?
    // A: Yes, because the user can add files to playlist without creating ic_previous_black new List of AfmAudio
    // objects, just adding this files dynamically.

    /**
     * Add an audio for the playlist. We can track the AfmAudio by
     * its id. So here we returning its id after adding to list.
     *
     * @param afmAudio audio file generated from {@link AfmAudio}
     * @return id of afmAudio.
     */
    public long addAudio(AfmAudio afmAudio) {
        createJcAudioPlayer();
        List<AfmAudio> playlist = afmAudioPlayer.getPlaylist();
        int lastPosition = playlist.size();

        afmAudio.setId(lastPosition + 1);
        afmAudio.setPosition(lastPosition + 1);

        if (!playlist.contains(afmAudio)) {
            playlist.add(lastPosition, afmAudio);
        }
        return afmAudio.getId();
    }

    /**
     * Remove an audio for the playlist
     *
     * @param afmAudio AfmAudio object
     */
    public void removeAudio(AfmAudio afmAudio) {
        if (afmAudioPlayer != null) {
            List<AfmAudio> playlist = afmAudioPlayer.getPlaylist();

            if (playlist != null && playlist.contains(afmAudio)) {
                if (playlist.size() > 1) {
                    // play next audio when currently played audio is removed.
                    if (afmAudioPlayer.isPlaying()) {
                        if (afmAudioPlayer.getCurrentAudio().equals(afmAudio)) {
                            playlist.remove(afmAudio);
                            pause();
                            resetPlayerInfo();
                        } else {
                            playlist.remove(afmAudio);
                        }
                    } else {
                        playlist.remove(afmAudio);
                    }
                } else {
                    //TODO: Maybe we need afmAudioPlayer.stopPlay() for stopping the player
                    playlist.remove(afmAudio);
                    pause();
                    resetPlayerInfo();
                }
            }
        }
    }


    public void playAudio(AfmAudio afmAudio) {
        showProgressBar();
        createJcAudioPlayer();
        if (!afmAudioPlayer.getPlaylist().contains(afmAudio))
            afmAudioPlayer.getPlaylist().add(afmAudio);

        try {
            afmAudioPlayer.playAudio(afmAudio);
        } catch (AudioListNullPointerException e1) {
            dismissProgressBar();
            e1.printStackTrace();
        } catch (NullPointerException e2) {
            // Service is not bounded yet.
            afmAudioPlayer.lazyPlayAudio(afmAudio);
        }
    }

    public void next() {
        if (afmAudioPlayer.getCurrentAudio() == null) {
            return;
        }
        resetPlayerInfo();
        showProgressBar();

        try {
            afmAudioPlayer.nextAudio();
        } catch (AudioListNullPointerException e) {
            dismissProgressBar();
            e.printStackTrace();
        }
    }

    public void continueAudio() {
        showProgressBar();

        try {
            afmAudioPlayer.continueAudio();
        } catch (AudioListNullPointerException e) {
            dismissProgressBar();
            e.printStackTrace();
        }
    }

    public void pause() {
        afmAudioPlayer.pauseAudio();
    }

    public void previous() {
        resetPlayerInfo();
        showProgressBar();

        try {
            afmAudioPlayer.previousAudio();
        } catch (AudioListNullPointerException e) {
            dismissProgressBar();
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (isInitialized) {
            if (view.getId() == R.id.btn_play) {
                YoYo.with(Techniques.Pulse)
                        .duration(PULSE_ANIMATION_DURATION)
                        .playOn(btnPlay);

                if (btnPlay.getTag().equals(R.drawable.ic_pause_white)) {
                    pause();
                } else {
                    continueAudio();
                }
            }
        }
        if (view.getId() == R.id.btn_next) {
            YoYo.with(Techniques.Pulse)
                    .duration(PULSE_ANIMATION_DURATION)
                    .playOn(btnNext);
            next();
        }

        if (view.getId() == R.id.btn_prev) {
            YoYo.with(Techniques.Pulse)
                    .duration(PULSE_ANIMATION_DURATION)
                    .playOn(btnPrev);
            previous();
        }
    }

    /**
     * Create ic_previous_black notification player with same playlist with ic_previous_black custom icon.
     *
     * @param iconResource icon path.
     */
    public void createNotification(int iconResource) {
        if (afmAudioPlayer != null) afmAudioPlayer.createNewNotification(iconResource);
    }

    /**
     * Create ic_previous_black notification player with same playlist with ic_previous_black default icon
     */
    public void createNotification() {
        if (afmAudioPlayer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // For light theme
                afmAudioPlayer.createNewNotification(R.drawable.ic_notification_default_black);
            } else {
                // For dark theme
                afmAudioPlayer.createNewNotification(R.drawable.ic_notification_default_white);
            }
        }
    }

    public List<AfmAudio> getMyPlaylist() {
        return afmAudioPlayer.getPlaylist();
    }

    public boolean isPlaying() {
        return afmAudioPlayer.isPlaying();
    }

    public boolean isPaused() {
        return afmAudioPlayer.isPaused();
    }

    public AfmAudio getCurrentAudio() {
        return afmAudioPlayer.getCurrentAudio();
    }

    private void createJcAudioPlayer() {
        if (afmAudioPlayer == null) {
            List<AfmAudio> playlist = new ArrayList<>();
            afmAudioPlayer = new AfmAudioPlayer(getContext(), playlist, jcPlayerViewServiceListener);
        }
        afmAudioPlayer.registerInvalidPathListener(onInvalidPathListener);
        //afmAudioPlayer.registerStatusListener(jcPlayerViewStatusListener);
        isInitialized = true;
    }

    private void sortPlaylist(List<AfmAudio> playlist) {
        for (int i = 0; i < playlist.size(); i++) {
            AfmAudio afmAudio = playlist.get(i);
            afmAudio.setId(i);
            afmAudio.setPosition(i);
        }
    }

    /**
     * Check if playlist already sorted or not.
     * We need to check because there is ic_previous_black possibility that the user reload previous playlist
     * from persistence storage like sharedPreference or SQLite.
     *
     * @param playlist list of AfmAudio
     * @return true if sorted, false if not.
     */
    private boolean isAlreadySorted(List<AfmAudio> playlist) {
        // If there is position in the first audio, then playlist is already sorted.
        if (playlist != null) {
            if (playlist.get(0).getPosition() != -1) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void generateTitleAudio(List<AfmAudio> playlist, String title) {
        for (int i = 0; i < playlist.size(); i++) {
            if (title.equals(getContext().getString(R.string.track_number))) {
                playlist.get(i).setTitle(getContext().getString(R.string.track_number) + " " + String.valueOf(i + 1));
            } else {
                playlist.get(i).setTitle(title);
            }
        }
    }

    private void showProgressBar() {
        progressBarPlayer.setVisibility(ProgressBar.VISIBLE);
        btnPlay.setVisibility(Button.GONE);
        btnNext.setClickable(false);
        btnPrev.setClickable(false);
    }

    private void dismissProgressBar() {
        progressBarPlayer.setVisibility(ProgressBar.GONE);
        btnPlay.setVisibility(Button.VISIBLE);
        btnNext.setClickable(true);
        btnPrev.setClickable(true);
    }

    private void resetPlayerInfo() {
        seekBar.setProgress(0);
        txtCurrentMusic.setText("");
        txtCurrentDuration.setText(getContext().getString(R.string.play_initial_time));
        txtDuration.setText(getContext().getString(R.string.play_initial_time));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
        if (fromUser && afmAudioPlayer != null) afmAudioPlayer.seekTo(i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        showProgressBar();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        dismissProgressBar();
    }

    public void registerInvalidPathListener(OnInvalidPathListener registerInvalidPathListener) {
        if (afmAudioPlayer != null) {
            afmAudioPlayer.registerInvalidPathListener(registerInvalidPathListener);
        }
    }

    public void kill() {
        if (afmAudioPlayer != null) afmAudioPlayer.kill();
    }

    public void registerServiceListener(JcPlayerViewServiceListener jcPlayerServiceListener) {
        if (afmAudioPlayer != null) {
            afmAudioPlayer.registerServiceListener(jcPlayerServiceListener);
        }
    }

    public void registerStatusListener(JcPlayerViewStatusListener statusListener) {
        if (afmAudioPlayer != null) {
            afmAudioPlayer.registerStatusListener(statusListener);
        }
    }

}
