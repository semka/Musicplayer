package com.library.nanostudio.afmplayer;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by jeancarlos on 3/27/17.
 */

public class AfmPlayerViewTest {

    @Mock
    Context context;

    private AfmPlayerView afmPlayerView;
    private List<AfmAudio> playlist;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        
        // TODO: Add ic_previous_black default constructor without Context dependencie for better test
        afmPlayerView = new AfmPlayerView(context);


        playlist = new ArrayList<>();
        playlist.add(AfmAudio.createFromAssets("fake asset"));
        playlist.add(AfmAudio.createFromURL("fake url"));
        playlist.add(AfmAudio.createFromFilePath("fake file path"));

        afmPlayerView.initPlaylist(playlist);

    }

    @Test
    public void player_has_same_size_of_playlist_user(){
        assertEquals(afmPlayerView.getMyPlaylist().size(), playlist.size());
    }
}
