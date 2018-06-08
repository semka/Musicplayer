package com.library.nanostudio.afmplayer.AfmPlayerExceptions;


public class AudioListNullPointerException extends NullPointerException {
    public AudioListNullPointerException() {
        super("The playlist is empty or null");
    }
}
