package com.person.countdowndemo.entity;

/**
 *
 **/
public class CountInfo {
    private long totalTime; // 倒计时总时长
    private long currentTime; // 当前值

    public CountInfo(long totalTime) {
        this.totalTime = totalTime;
    }

    public CountInfo(long totalTime, long currentTime) {
        this.totalTime = totalTime;
        this.currentTime = currentTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }
}
