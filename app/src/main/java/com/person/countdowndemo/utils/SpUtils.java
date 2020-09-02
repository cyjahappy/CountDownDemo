package com.person.countdowndemo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences工具类 保存少量数据
 **/
public class SpUtils {
    public final static String spf_name="countdown";
    public final static String keyCurrentTime = "keyCurrentTime"; // 剩余倒计时时间
    public final static String keySystemTime = "keySystemTime"; // 系统时间
    public final static String keyCountTotalTime = "keyCountTotalTime";
    public final static String keyHourIndex = "keyHourIndex"; // 小时对应下标
    public final static String keyMinIndex = "keyMinIndex"; // 分钟对应下标
    public final static String keySecIndex= "keySecIndex"; // 秒钟对应下标
    public final static String keyStop = "keyStop";  // 暂停状态

    public static void saveLongData(Context context,String key,long time){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(key,time).apply();
    }

    public static long getLongData(Context context,String key){
        return getSharedPreferences(context).getLong(key,0);
    }

    public static void clearSaveData(Context context){
        getSharedPreferences(context).edit().clear();
    }

    public static void saveIntData(Context context,String key,int time){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(key,time).apply();
    }

    public static int getIntData(Context context,String key){
        return getSharedPreferences(context).getInt(key, 0);
    }


    public static void saveBooleanData(Context context,String key,boolean b){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key,b).apply();
    }

    public static Boolean getBooleanData(Context context,String key){
        return getSharedPreferences(context).getBoolean(key,false);
    }


    public static SharedPreferences getSharedPreferences(Context context){
        SharedPreferences spf=context.getSharedPreferences(spf_name, Activity.MODE_PRIVATE);
        return spf;
    }
}
