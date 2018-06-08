package musicplayer.mediaplayer.playlistaudio;

import android.content.Context;
import android.util.SparseArray;

@Deprecated
public abstract class Playlist extends PlaylistPlayer {

    public Playlist(Context hconn) {
        super(hconn);
    }

    @Override
    protected void onExtractionComplete(SparseArray<PlayMusic> plo, PlayAudio ta) {
        onUrisAvailable(ta.getVideoId(), ta.getTitle(), plo);
    }

    public abstract void onUrisAvailable(String videoId, String videoTitle, SparseArray<PlayMusic> ytFiles);
}
