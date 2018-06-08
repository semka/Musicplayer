package musicplayer.mediaplayer.playlistaudio;

public class Player {

    public enum VCodec {
        H263, H264, MPEG4, VP8, VP9, NONE
    }

    public enum ACodec {
        MP3, AAC, VORBIS, OPUS, NONE
    }

    private int itag;
    private String ext;
    private int olcu;
    private int fps;
    private VCodec vCodec;
    private ACodec aCodec;
    private int audioBitrate;
    private boolean isDashContainer;
    private boolean isHlsContent;

    Player(int itag, String ext, int olcu, VCodec vCodec, ACodec aCodec, boolean isDashContainer) {
        this.itag = itag;
        this.ext = ext;
        this.olcu = olcu;
        this.fps = 30;
        this.audioBitrate = -1;
        this.isDashContainer = isDashContainer;
        this.isHlsContent = false;
    }

    Player(int itag, String ext, VCodec vCodec, ACodec aCodec, int audioBitrate, boolean isDashContainer) {
        this.itag = itag;
        this.ext = ext;
        this.olcu = -1;
        this.fps = 30;
        this.audioBitrate = audioBitrate;
        this.isDashContainer = isDashContainer;
        this.isHlsContent = false;
    }

    Player(int itag, String ext, int olcu, VCodec vCodec, ACodec aCodec, int audioBitrate,
           boolean isDashContainer) {
        this.itag = itag;
        this.ext = ext;
        this.olcu = olcu;
        this.fps = 30;
        this.audioBitrate = audioBitrate;
        this.isDashContainer = isDashContainer;
        this.isHlsContent = false;
    }

    Player(int itag, String ext, int olcu, VCodec vCodec, ACodec aCodec, int audioBitrate,
           boolean isDashContainer, boolean isHlsContent) {
        this.itag = itag;
        this.ext = ext;
        this.olcu = olcu;
        this.fps = 30;
        this.audioBitrate = audioBitrate;
        this.isDashContainer = isDashContainer;
        this.isHlsContent = isHlsContent;
    }

    Player(int itag, String ext, int olcu, VCodec vCodec, int fps, ACodec aCodec, boolean isDashContainer) {
        this.itag = itag;
        this.ext = ext;
        this.olcu = olcu;
        this.audioBitrate = -1;
        this.fps = fps;
        this.isDashContainer = isDashContainer;
        this.isHlsContent = false;
    }



    public String getExt() {
        return ext;
    }

    public boolean isDashContainer() {
        return isDashContainer;
    }



    public int getHeight() {
        return olcu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player format = (Player) o;

        if (itag != format.itag) return false;
        if (olcu != format.olcu) return false;
        if (fps != format.fps) return false;
        if (audioBitrate != format.audioBitrate) return false;
        if (isDashContainer != format.isDashContainer) return false;
        if (isHlsContent != format.isHlsContent) return false;
        if (ext != null ? !ext.equals(format.ext) : format.ext != null) return false;
        if (vCodec != format.vCodec) return false;
        return aCodec == format.aCodec;

    }

    @Override
    public int hashCode() {
        int result = itag;
        result = 31 * result + (ext != null ? ext.hashCode() : 0);
        result = 31 * result + olcu;
        result = 31 * result + fps;
        result = 31 * result + (vCodec != null ? vCodec.hashCode() : 0);
        result = 31 * result + (aCodec != null ? aCodec.hashCode() : 0);
        result = 31 * result + audioBitrate;
        result = 31 * result + (isDashContainer ? 1 : 0);
        result = 31 * result + (isHlsContent ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Player{" +
                "itag=" + itag +
                ", ext='" + ext + '\'' +
                ", height=" + olcu +
                ", fps=" + fps +
                ", vCodec=" + vCodec +
                ", aCodec=" + aCodec +
                ", audioBitrate=" + audioBitrate +
                ", isDashContainer=" + isDashContainer +
                ", isHlsContent=" + isHlsContent +
                '}';
    }
}
