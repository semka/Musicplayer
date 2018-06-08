package musicplayer.mediaplayer.playlistaudio;

public class PlayAudio {

    private static final String IMAGE_BASE_URL = "http://i.ytimg.com/vi/";

    private String videoId;
    private String title;

    private String author;
    private String channelId;

    private long uzunlugu;
    private long baxssay;

    private boolean isLiveStream;

    protected PlayAudio(String videoId, String title, String author, String channelId, long uzunlugu, long baxssay, boolean isLiveStream) {
        this.videoId = videoId;
        this.title = title;
        this.author = author;
        this.channelId = channelId;
        this.uzunlugu = uzunlugu;
        this.baxssay = baxssay;
        this.isLiveStream = isLiveStream;
    }



    public String getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getChannelId() {
        return channelId;
    }

    public boolean isLiveStream() {
        return isLiveStream;
    }

    /**
     * The video length in seconds.
     */
    public long getVideoLength() {
        return uzunlugu;
    }

    public long getViewCount() {
        return baxssay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayAudio videoMeta = (PlayAudio) o;

        if (uzunlugu != videoMeta.uzunlugu) return false;
        if (baxssay != videoMeta.baxssay) return false;
        if (isLiveStream != videoMeta.isLiveStream) return false;
        if (videoId != null ? !videoId.equals(videoMeta.videoId) : videoMeta.videoId != null)
            return false;
        if (title != null ? !title.equals(videoMeta.title) : videoMeta.title != null) return false;
        if (author != null ? !author.equals(videoMeta.author) : videoMeta.author != null)
            return false;
        return channelId != null ? channelId.equals(videoMeta.channelId) : videoMeta.channelId == null;

    }

    @Override
    public int hashCode() {
        int result = videoId != null ? videoId.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
        result = 31 * result + (int) (uzunlugu ^ (uzunlugu >>> 32));
        result = 31 * result + (int) (baxssay ^ (baxssay >>> 32));
        result = 31 * result + (isLiveStream ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PlayAudio{" +
                "videoId='" + videoId + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", channelId='" + channelId + '\'' +
                ", height=" + uzunlugu +
                ", viewCount=" + baxssay +
                ", isLiveStream=" + isLiveStream +
                '}';
    }
}
