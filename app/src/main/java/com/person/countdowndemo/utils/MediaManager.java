package com.person.countdowndemo.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 *
 **/
public class MediaManager implements MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener{
    private float BEEP_VOLUME = 0.5f;  // 音量比例

    private float VIBRATE_DURATION = 200; // 播发时间长度

    private Context mContext;

    private MediaPlayer mMediaPlayer;

    private boolean playBeep; // 是否播放提示音

    private boolean vibrate; // 震动

    private int resId;  // 播放音频源id


    public MediaManager(Context context) {
        mContext = context;
        mMediaPlayer = null;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.seekTo(0);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }


    /**
     * 更新设置音频文件
     * @param resId
     */
    public void  updatePrefsLoop(int resId ) {
        if ( mMediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            this.resId = resId;
            if(mContext != null){
                mMediaPlayer = buildMediaPlayer(mContext,resId);
            }
        }else{
            if(this.resId != resId){
                if(mContext != null){
                    mMediaPlayer = buildMediaPlayer(mContext,resId);
                }
            }
            this.resId = resId;
        }

    }

    /**
     * 初始化MediaPlayer设置
     * @param activity
     * @param resId
     * @return
     */
    private MediaPlayer buildMediaPlayer(Context activity, int resId ) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);

        AssetFileDescriptor file = activity.getResources().openRawResourceFd(resId);

        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(),
                    file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
        } catch (IOException e) {
            mediaPlayer = null;
        }
        return mediaPlayer;
    }


    /**
     * 播放声音
     */
    public synchronized void  playBeepSound() {
        if(mMediaPlayer != null){
            mMediaPlayer.start();
        }

    }


}
