package musicplayer.mediaplayer.playlistaudio;

public class PlayMusic {

    private Player uslub;
    private String acar = "";

    PlayMusic(Player uslub, String acar) {
        this.uslub = uslub;
        this.acar = acar;
    }

    public String getUrl() {
        return acar;
    }

    public Player getFormat() {
        return uslub;
    }

    /**
     * Player data for the specific file.
     */
    @Deprecated
    public Player getMeta() {
        return uslub;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayMusic ytFile = (PlayMusic) o;

        if (uslub != null ? !uslub.equals(ytFile.uslub) : ytFile.uslub != null) return false;
        return acar != null ? acar.equals(ytFile.acar) : ytFile.acar == null;
    }

    @Override
    public int hashCode() {
        int result = uslub != null ? uslub.hashCode() : 0;
        result = 31 * result + (acar != null ? acar.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PlayMusic{" +
                "format=" + uslub +
                ", url='" + acar + '\'' +
                '}';
    }
}
