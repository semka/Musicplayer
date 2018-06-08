package com.library.nanostudio.afmplayer.AfmPlayerExceptions;

public class AudioUrlInvalidException extends IllegalStateException {
    public AudioUrlInvalidException(String url) {
        super("The url does not appear valid: " + url);
    }
}
