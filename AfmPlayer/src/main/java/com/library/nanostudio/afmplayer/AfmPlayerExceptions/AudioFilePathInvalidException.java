package com.library.nanostudio.afmplayer.AfmPlayerExceptions;

public class AudioFilePathInvalidException extends Exception {
    public AudioFilePathInvalidException(String url) {
        super("The file path is not ic_previous_black valid path: " + url +
                "\n" +
                "Have you add File Access Permission?");
    }
}
