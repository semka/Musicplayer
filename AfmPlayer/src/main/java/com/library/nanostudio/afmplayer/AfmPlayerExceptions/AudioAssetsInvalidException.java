package com.library.nanostudio.afmplayer.AfmPlayerExceptions;


public class AudioAssetsInvalidException extends Exception {
    public AudioAssetsInvalidException(String path) {
        super("The file name is not ic_previous_black valid Assets file: " + path);
    }
}
