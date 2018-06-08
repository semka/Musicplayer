package com.library.nanostudio.afmplayer.AfmPlayerExceptions;

public class AudioRawInvalidException extends Exception {
    public AudioRawInvalidException(String rawId) {
        super("Not ic_previous_black valid raw file id: " + rawId);
    }
}
