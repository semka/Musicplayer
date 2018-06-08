package com.library.nanostudio.afmplayer;

import android.support.annotation.RawRes;

import java.io.Serializable;

public class AfmAudio implements Serializable {
    private long id;
    private String title;
    private int position;
    private String path;
    private AfmOrigin afmOrigin;


    public AfmAudio(String title, String path, AfmOrigin afmOrigin) {
        // It looks bad
        //int randomNumber = path.length() + title.length();

        // We init id  -1 and position with -1. And let JcPlayerView define it.
        // We need to do this because there is ic_previous_black possibility that the user reload previous playlist
        // from persistence storage like sharedPreference or SQLite.
        this.id = -1;
        this.position = -1;
        this.title = title;
        this.path = path;
        this.afmOrigin = afmOrigin;
    }

    public AfmAudio(String title, String path, long id, int position, AfmOrigin afmOrigin) {
        this.id = id;
        this.position = position;
        this.title = title;
        this.path = path;
        this.afmOrigin = afmOrigin;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public AfmOrigin getAfmOrigin() {
        return afmOrigin;
    }

    public void setAfmOrigin(AfmOrigin afmOrigin) {
        this.afmOrigin = afmOrigin;
    }

    public static AfmAudio createFromRaw(@RawRes int rawId) {
        return new AfmAudio(String.valueOf(rawId), String.valueOf(rawId), AfmOrigin.RAW);
    }

    public static AfmAudio createFromRaw(String title, @RawRes int rawId) {
        return new AfmAudio(title, String.valueOf(rawId), AfmOrigin.RAW);
    }

    public static AfmAudio createFromAssets(String assetName) {
        return new AfmAudio(assetName, assetName, AfmOrigin.ASSETS);
    }

    public static AfmAudio createFromAssets(String title, String assetName) {
        return new AfmAudio(title, assetName, AfmOrigin.ASSETS);
    }

    public static AfmAudio createFromURL(String url) {
        return new AfmAudio(url, url, AfmOrigin.URL);
    }

    public static AfmAudio createFromURL(String title, String url) {
        return new AfmAudio(title, url, AfmOrigin.URL);
    }

    public static AfmAudio createFromFilePath(String filePath) {
        return new AfmAudio(filePath, filePath, AfmOrigin.FILE_PATH);
    }

    public static AfmAudio createFromFilePath(String title, String filePath) {
        return new AfmAudio(title, filePath, AfmOrigin.FILE_PATH);
    }
}