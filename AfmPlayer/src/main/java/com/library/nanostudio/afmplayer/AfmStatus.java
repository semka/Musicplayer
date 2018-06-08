package com.library.nanostudio.afmplayer;

public class AfmStatus {
  enum PlayState {
    PLAY, PAUSE, STOP, UNINTIALIZED
  }

  private AfmAudio afmAudio;
  private long duration;
  private long currentPosition;
  private PlayState playState;

  public AfmStatus() {
    this(null, 0, 0, PlayState.UNINTIALIZED);
  }

  public AfmStatus(AfmAudio afmAudio, long duration, long currentPosition, PlayState playState) {
    this.afmAudio = afmAudio;
    this.duration = duration;
    this.currentPosition = currentPosition;
    this.playState = playState;
  }

  public AfmAudio getAfmAudio() {
    return afmAudio;
  }

  public void setAfmAudio(AfmAudio afmAudio) {
    this.afmAudio = afmAudio;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public long getCurrentPosition() {
    return currentPosition;
  }

  public void setCurrentPosition(long currentPosition) {
    this.currentPosition = currentPosition;
  }

  public PlayState getPlayState() {
    return playState;
  }

  public void setPlayState(PlayState playState) {
    this.playState = playState;
  }
}
